package org.wjd.net.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UdpServer extends Server
{

	DatagramSocket socket;

	@Override
	public void run()
	{
		try
		{
			socket = new DatagramSocket(new InetSocketAddress("127.0.0.1",
					10011));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		byte[] buff = new byte[2048];
		while (running)
		{
			try
			{
				DatagramPacket packet = new DatagramPacket(buff, buff.length);
				socket.receive(packet);
				// 将收到的数据返回
				socket.send(packet);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		closeSock();
	}

	@Override
	protected void closeSock()
	{
		if (null != socket)
		{
			socket.close();
			socket = null;
		}
	}
}
