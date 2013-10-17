package org.wjd.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer extends Thread
{

	private ServerSocket socket;

	@Override
	public void run()
	{
		try
		{
			socket = new ServerSocket(10011);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		byte[] buff = new byte[2048];
		Socket sock = null;
		try
		{
			sock = socket.accept();
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
		while (null != sock)
		{
			try
			{
				int len = sock.getInputStream().read(buff);
				byte[] data = new byte[len];
				System.arraycopy(buff, 0, data, 0, data.length);
				sock.getOutputStream().write(data);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
