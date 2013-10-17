package org.wjd.net.tcp_udp.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.wjd.net.tcp_udp.BaseChannel;
import org.wjd.net.tcp_udp.Message;

/**
 * udp数据通道
 * 
 * @author wjd
 * 
 */
public class UdpChannel extends BaseChannel
{

	private DatagramSocket mSocket;

	@Override
	protected boolean initLocalConnection(String ip, int port)
	{
		int idx = 0;
		while (!initSocket(idx))
		{
			try
			{
				Thread.sleep(5l);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			idx++;
		}
		initThread();
		return true;
	}

	/**
	 * 初始化socket
	 * 
	 * @param idx
	 * @return
	 */
	private boolean initSocket(int idx)
	{
		try
		{
			mSocket = new DatagramSocket(10100 + idx);
		} catch (SocketException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected void unInitLocalConnection()
	{
		if (null != mSocket)
		{
			mSocket.close();
		}
		mSocket = null;
	}

	@Override
	protected boolean doSendImpl(Message message)
	{
		if (null == message)
		{
			return false;
		}
		InetAddress addr = null;
		try
		{
			addr = InetAddress.getByName(message.getHost());
		} catch (UnknownHostException e)
		{
			obtainMessage(NETERROR_HANDLE, message).sendToTarget();
			e.printStackTrace();
			return false;
		}
		DatagramPacket localDatagramPacket = new DatagramPacket(
				message.getData(), message.getData().length, addr,
				message.getPort());
		try
		{
			this.mSocket.send(localDatagramPacket);
		} catch (IOException e)
		{
			obtainMessage(NETERROR_HANDLE, message).sendToTarget();
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private byte[] src = new byte[2048];

	@Override
	protected void doLinsenImpl()
	{

		DatagramPacket packet = new DatagramPacket(src, src.length);
		try
		{
			mSocket.receive(packet);
			byte[] res = new byte[packet.getLength()];
			System.arraycopy(src, 0, res, 0, res.length);
			parseMessage(res);
		} catch (Exception e)
		{
			try
			{
				Thread.sleep(3000l);
			} catch (InterruptedException e1)
			{
			}
		}
	}
}
