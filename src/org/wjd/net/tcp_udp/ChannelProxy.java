package org.wjd.net.tcp_udp;

import java.net.InetAddress;

import org.wjd.net.tcp_udp.tcp.TcpChannel;
import org.wjd.net.tcp_udp.tcp.TcpConnectHandler;
import org.wjd.net.tcp_udp.tcp.TcpNormalChannel;
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
		TYPE_TCP, TYPE_UDP, TYPE_NORMAL_TCP
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
				break;
			case TYPE_UDP:
				channel = new UdpChannel();
				break;
			case TYPE_NORMAL_TCP:
				channel = new TcpNormalChannel();
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
	public void setPushHandler(PushHandler pushHandler)
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
	 * @param request
	 */
	public void sendRequest(UnsyncRequest request)
	{
		if (null != channel)
		{
			channel.storeMessageToSend(request);
		}
	}

	/**
	 * 即刻发送消息，只支持UDP消息
	 * 
	 * 消息不处理响应，主要用于音视频等大量，频繁的数据请求
	 * 
	 * @param request
	 * @param addr
	 */
	public void sendRequestImmediately(UnsyncRequest request, InetAddress addr)
	{
		if (null != channel)
		{
			channel.doSendImmediately(request, addr);
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
