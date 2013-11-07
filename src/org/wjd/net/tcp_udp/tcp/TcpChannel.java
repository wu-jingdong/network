package org.wjd.net.tcp_udp.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.wjd.net.tcp_udp.BaseChannel;
import org.wjd.net.tcp_udp.UnsyncRequest;

/**
 * tcp链接通道，采用nio
 * 
 * @author wjd
 * 
 */
public class TcpChannel extends BaseChannel
{

	private SocketChannel sockChannel;

	private Selector selector;

	private SelectionKey selectKey;

	private boolean connectedOn = false;

	@Override
	protected void storeMessageToSend(UnsyncRequest message)
	{
		if (!connectedOn)
		{
			obtainMessage(NETERROR_HANDLE, message).sendToTarget();
			return;
		}
		super.storeMessageToSend(message);
	}

	/**
	 * 初始化本地链接
	 */
	@Override
	protected boolean initLocalConnection(String ip, int port)
	{
		try
		{
			selector = Selector.open();
			sockChannel = SocketChannel.open();
			sockChannel.configureBlocking(false);
			selectKey = sockChannel.register(selector, SelectionKey.OP_CONNECT);
			sockChannel.connect(new InetSocketAddress(ip, port));
		} catch (ClosedChannelException e)
		{
			e.printStackTrace();
			obtainMessage(STATUS_HANDLER, false).sendToTarget();
			return false;
		} catch (IOException e)
		{
			e.printStackTrace();
			obtainMessage(STATUS_HANDLER, false).sendToTarget();
			return false;
		}
		initThread();
		return true;
	}

	/**
	 * 释放本地链接
	 */
	@Override
	protected void unInitLocalConnection()
	{
		if (null != sockChannel)
		{
			try
			{
				sockChannel.close();
			} catch (IOException e)
			{
			}
		}
		sockChannel = null;
		if (null != selector)
		{
			try
			{
				selector.close();
			} catch (IOException e)
			{
			}
		}
	}

	/**
	 * 消息发送实现
	 * 
	 * @param request
	 */
	@Override
	protected boolean doSendImpl(UnsyncRequest request)
	{
		byte[] data = request.createSendData();
		if (null == data)
		{
			return false;
		}
		if (!connectedOn)
		{
			obtainMessage(NETERROR_HANDLE, request).sendToTarget();
			return false;
		}

		selectKey.attach(data);
		selectKey.interestOps(selectKey.interestOps() | SelectionKey.OP_WRITE);
		selectKey.selector().wakeup();
		return true;
	}

	private int closedCount = 0;

	/**
	 * 消息接收实现
	 */
	@Override
	protected boolean doLinsenImpl()
	{
		boolean ret = true;
		try
		{
			int keyCnt = selector.select();
			if (keyCnt < 0)
			{
				return ret;
			}
			Set<SelectionKey> set = selector.selectedKeys();
			Iterator<SelectionKey> it = set.iterator();
			while (it.hasNext())
			{
				SelectionKey key = it.next();
				try
				{
					if (key.isConnectable())
					{
						doConnect(key);
					}
					if (key.isValid() && key.isReadable())
					{
						ret = doReadMessage(key);
					}
					if (key.isValid() && key.isWritable())
					{
						doWriteMessage(key);
					}
				} catch (CancelledKeyException e)
				{
					key.cancel();
					e.printStackTrace();
				}
			}
			set.clear();
		} catch (IOException e)
		{
			e.printStackTrace();
			onConnectionStatusChanged(false);
		} catch (ClosedSelectorException e1)
		{
			e1.printStackTrace();
			closedCount++;
			if (closedCount > 10)
			{
				onConnectionStatusChanged(false);
			}
		}
		return ret;
	}

	private TcpConnectHandler connectHandler;

	public void setOnConnectionStatusChanged(TcpConnectHandler connectHandler)
	{
		this.connectHandler = connectHandler;
	}

	/**
	 * 处理网络连接断开
	 */
	private void onConnectionStatusChanged(boolean connected)
	{
		if (!connected)
		{
			unInit();
		}
		obtainMessage(STATUS_HANDLER, connected).sendToTarget();
	}

	private void doConnect(SelectionKey key)
	{
		SocketChannel sc = (SocketChannel) key.channel();
		try
		{
			sc.finishConnect();
			key.interestOps(SelectionKey.OP_READ);
		} catch (Exception e)
		{
			e.printStackTrace();
			onConnectionStatusChanged(false);
			return;
		}
		onConnectionStatusChanged(true);
	}

	private void doWriteMessage(SelectionKey key) throws IOException
	{
		SocketChannel sc = (SocketChannel) key.channel();
		byte[] data = (byte[]) key.attachment();
		if (null != data && data.length > 0)
		{
			sc.write(ByteBuffer.wrap(data));
		}
		key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
	}

	private static final int BUFFER_SIZE = 1024;

	/**
	 * 接收数据缓冲区
	 */
	private byte[] container = new byte[BUFFER_SIZE * 5];

	private boolean doReadMessage(SelectionKey key) throws IOException
	{
		SocketChannel sc = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		try
		{
			int length = 0;
			int count = 0;
			while ((count = sc.read(buffer)) > 0)
			{
				buffer.flip();
				buffer.get(container, length, count);
				length += count;
			}
			if (length <= 0)
			{
				return false;
			}
			byte[] res = new byte[length];
			System.arraycopy(container, 0, res, 0, res.length);
			combainPacket(res);
		} catch (IndexOutOfBoundsException E1)
		{
			E1.printStackTrace();
		}
		return true;
	}

	private List<byte[]> tempReceiveData = new LinkedList<byte[]>();

	/**
	 * 组包
	 * 
	 * @param src
	 */
	private void combainPacket(byte[] src)
	{
		ByteBuffer buffer = ByteBuffer.wrap(src);
		int length = src.length;
		if (tempReceiveData.isEmpty())
		{
			short len = buffer.getShort();
			if (len == length - 2)
			{
				parseMessage(src);
			} else if (len < length - 2)
			{
				handleLarger(src);
			} else if (len > length - 2)
			{
				tempReceiveData.add(src);
			}
		} else
		{
			byte[] tempStore = tempReceiveData.remove(0);
			ByteBuffer tempStoreBuffer = ByteBuffer.wrap(tempStore);
			short tempMLen = tempStoreBuffer.getShort();
			int tempLength = tempStore.length;
			if (length < (tempMLen + 2) - tempLength)
			{
				// src不足以补齐temp中的消息
				byte[] temp = new byte[tempLength + length];
				ByteBuffer tempBuffer = ByteBuffer.wrap(temp);
				tempBuffer.put(tempStore);
				tempBuffer.put(src);
				tempReceiveData.add(temp);
			} else if (length == (tempMLen + 2) - tempLength)
			{
				// src正好补齐temp中的消息
				byte[] temp = new byte[tempLength + length];
				ByteBuffer tempBuffer = ByteBuffer.wrap(temp);
				tempBuffer.put(tempStore);
				tempBuffer.put(src);
				parseMessage(temp);
			} else if (length > (tempMLen + 2) - tempLength)
			{
				// src补齐temp中的消息后还有剩余
				byte[] temp = new byte[tempMLen + 2];
				ByteBuffer tempBuffer = ByteBuffer.wrap(temp);
				tempBuffer.put(tempStore);
				int sliceLength = temp.length - tempLength;
				tempBuffer.put(src, 0, sliceLength);
				parseMessage(temp);
				byte[] remain = new byte[length - sliceLength];
				buffer.get(remain);
				combainPacket(remain);
			}
		}
	}

	private void handleLarger(byte[] src)
	{
		ByteBuffer buffer = ByteBuffer.wrap(src);
		int length = src.length;
		// ---1---- 处理第一个完整的消息
		buffer.mark();
		int len = buffer.getShort();
		buffer.reset();
		byte[] dest = new byte[len + 2];
		buffer.get(dest, 0, len + 2);
		parseMessage(dest);
		// ---2--- 处理后半部分消息
		buffer.mark();
		int lLen = buffer.getShort();
		buffer.reset();
		byte[] remain = new byte[length - len - 2];
		buffer.get(remain, 0, length - len - 2);
		if (lLen < length - len - 4)
		{
			handleLarger(remain);
		} else if (lLen == length - len - 4)
		{
			parseMessage(remain);
		} else if (lLen > length - len - 4)
		{
			tempReceiveData.add(remain);
		}
	}

	private static final int STATUS_HANDLER = 101;

	@Override
	public void handleMessage(android.os.Message msg)
	{
		if (msg.what == STATUS_HANDLER)
		{
			connectedOn = (Boolean) msg.obj;
			if (null != connectHandler)
			{
				connectHandler.handleTcpConnectResult(connectedOn);
			}
			return;
		}
		super.handleMessage(msg);
	}
}
