package org.wjd.net.server;

public abstract class Server extends Thread
{

	protected boolean running = true;

	public void stopThread()
	{
		running = false;
		closeSock();
	}

	protected abstract void closeSock();
}
