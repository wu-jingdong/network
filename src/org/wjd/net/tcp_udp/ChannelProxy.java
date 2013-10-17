package org.wjd.net.tcp_udp;

import org.wjd.net.server.TcpServer;
import org.wjd.net.server.UdpServer;
import org.wjd.net.tcp_udp.tcp.TcpChannel;
import org.wjd.net.tcp_udp.tcp.TcpConnectHandler;
import org.wjd.net.tcp_udp.udp.UdpChannel;

/**
 * 消息通道代理
 * 
 * @author wjd
 * 
 */
public class ChannelProxy
{

	public enum CHANNEL_TYPE
	{
		TYPE_TCP, TYPE_UDP, TYPE_HTTP
	}

	private BaseChannel channel;

	/**
	 * 初始化消息通道代理
	 * 
	 * @param type
	 */
	public ChannelProxy(CHANNEL_TYPE type)
	{
		switch (type)
		{
			case TYPE_TCP:
				channel = new TcpChannel();
				// 测试使用
				new TcpServer().start();
				break;
			case TYPE_UDP:
				channel = new UdpChannel();
				// 测试使用
				new UdpServer().start();
				break;
			default:
				break;
		}
	}

	/**
	 * 初始化消息通道
	 * 
	 * @param ip
	 * @param port
	 */
	public void init(String ip, int port)
	{
		if (null != channel)
		{
			channel.initLocalConnection(ip, port);
		}
	}

	/**
	 * 设置推送消息的回调接口
	 * 
	 * @param pushHandler
	 */
	public void setPushHandler(NormalHandler pushHandler)
	{
		if (null != channel)
		{
			channel.setPushHandler(pushHandler);
		}
	}

	/**
	 * 设置通道状态监听（只有tcp链接生效）
	 * 
	 * @param statusChangedListener
	 */
	public void setChannelConnectionStatusChangedListener(
			TcpConnectHandler statusChangedListener)
	{
		if (null != channel && channel instanceof TcpChannel)
		{
			((TcpChannel) channel)
					.setOnConnectionStatusChanged(statusChangedListener);
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param message
	 */
	public void sendMessage(Message message)
	{
		if (null != channel)
		{
			channel.storeMessageToSend(message);
		}
	}

	/**
	 * 释放消息通道
	 */
	public void unInit()
	{
		if (null != channel)
		{
			channel.unInit();
		}
	}
}
