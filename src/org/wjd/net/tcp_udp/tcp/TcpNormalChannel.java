package org.wjd.net.tcp_udp.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.wjd.net.common.Loger;
import org.wjd.net.tcp_udp.BaseChannel;
import org.wjd.net.tcp_udp.UnsyncRequest;

public class TcpNormalChannel extends BaseChannel
{

	private Socket tcpSock;

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

	@Override
	protected boolean initLocalConnection(String ip, int port)
	{
		try
		{
			tcpSock = new Socket(ip, port);
		} catch (IOException e)
		{
			Loger.print(this.getClass().getSimpleName(), e.getMessage(),
					Loger.ERROR);
			onConnectionStatusChanged(false);
			return false;
		}
		onConnectionStatusChanged(true);
		initThread();
		return true;
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

	@Override
	protected void unInitLocalConnection()
	{
		if (null != tcpSock)
		{
			try
			{
				tcpSock.close();
			} catch (IOException e)
			{
			}
		}
		tcpSock = null;
	}

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

		try
		{
			tcpSock.getOutputStream().write(data);
			tcpSock.getOutputStream().flush();
		} catch (IOException e)
		{
			Loger.print(this.getClass().getSimpleName(), e.getMessage(),
					Loger.ERROR);
			obtainMessage(NETERROR_HANDLE, request).sendToTarget();
			onConnectionStatusChanged(false);
			return false;
		}
		return true;
	}

	@Override
	protected void doSendImmediately(UnsyncRequest req, InetAddress addr)
	{
		// Nothing to do
	}

	byte[] src = new byte[20480];

	@Override
	protected boolean doLinsenImpl()
	{
		int length;
		try
		{
			length = tcpSock.getInputStream().read(src);
		} catch (IOException e)
		{
			Loger.print(this.getClass().getSimpleName(), e.getMessage(),
					Loger.ERROR);
			onConnectionStatusChanged(false);
			return false;
		}
		if (length <= 0)
		{
			// ignore invalid packet
			return true;
		}
		byte[] res = new byte[length];
		System.arraycopy(src, 0, res, 0, res.length);
		combainPacket(res);
		return true;
	}

	private TcpConnectHandler connectHandler;

	public void setOnConnectionStatusChanged(TcpConnectHandler connectHandler)
	{
		this.connectHandler = connectHandler;
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

	private static final int STATUS_HANDLER = 102;

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
