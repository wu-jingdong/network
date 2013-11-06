package org.wjd.net.tcp_udp;

import java.nio.ByteBuffer;

public abstract class BaseMessage
{

	public abstract boolean match(ByteBuffer wrapper);

	public abstract byte[] createData();

	public abstract void parseData(byte[] receivedData);
}
