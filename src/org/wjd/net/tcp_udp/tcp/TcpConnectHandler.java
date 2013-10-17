package org.wjd.net.tcp_udp.tcp;

public interface TcpConnectHandler
{
	/**
	 * tcp链接状态改变回调
	 * 
	 * @param connected
	 *            true:已连接
	 * 
	 *            false:已断开
	 */
	public void handleTcpConnectResult(boolean connected);
}
