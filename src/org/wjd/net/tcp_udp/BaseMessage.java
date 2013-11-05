package org.wjd.net.tcp_udp;

public abstract class BaseMessage
{

	public abstract boolean match(byte[] receivedData);

	public abstract byte[] createData();

	public abstract void parseData(byte[] receivedData);
}
