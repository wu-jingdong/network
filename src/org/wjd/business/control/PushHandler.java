package org.wjd.business.control;

import java.nio.ByteBuffer;

import org.wjd.business.base.BusiMessage;
import org.wjd.net.tcp_udp.Message;
import org.wjd.net.tcp_udp.NormalHandler;

import android.util.SparseArray;

/**
 * 推送消息处理
 * 
 * @author wjd
 * 
 */
public class PushHandler implements NormalHandler
{
	
	private SparseArray<NormalHandler> handlers = new SparseArray<NormalHandler>();

	/**
	 * 注册模块处理
	 * 
	 * @param moduleId
	 * @param handler
	 */
	public void registHandler(byte moduleId, NormalHandler handler)
	{
		handlers.append(moduleId, handler);
	}

	/**
	 * 处理推送消息
	 */
	@Override
	public void handleResponse(Message message)
	{
		if (null == message)
		{
			return;
		}
		if (message.getReceivedDataLength() < BusiMessage.HEAD_LEN)
		{
			return;
		}
		ByteBuffer buffer = ByteBuffer.wrap(message.getData());
		NormalHandler handler = handlers.get(buffer.get());
		if (null != handler)
		{
			handler.handleResponse(message);
		}
	}
}
