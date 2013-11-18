package org.wjd.net.tcp_udp.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

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
		byte[] res = new byte[length];
		System.arraycopy(src, 0, res, 0, res.length);
		parseMessage(res);
		return true;
	}

	private TcpConnectHandler connectHandler;

	public void setOnConnectionStatusChanged(TcpConnectHandler connectHandler)
	{
		this.connectHandler = connectHandler;
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
