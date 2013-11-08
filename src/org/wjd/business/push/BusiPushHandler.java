package org.wjd.business.push;

import java.nio.ByteBuffer;

import org.wjd.business.audio.AudioMessage;
import org.wjd.business.base.BusMessage;
import org.wjd.business.base.Module;
import org.wjd.net.tcp_udp.BaseMessage;
import org.wjd.net.tcp_udp.NormalHandler;
import org.wjd.net.tcp_udp.PushHandler;

import android.util.SparseArray;

/**
 * 推送消息处理
 * 
 * @author wjd
 * 
 */
public class BusiPushHandler implements PushHandler
{

	public static BusiPushHandler instance = new BusiPushHandler();

	private SparseArray<NormalHandler> handlers = new SparseArray<NormalHandler>();

	private BusiPushHandler()
	{

	}

	/**
	 * 注册处理模块
	 * 
	 * @param moduleId
	 * @param handler
	 */
	public void registHandler(byte moduleId, NormalHandler handler)
	{
		handlers.append(moduleId, handler);
	}

	/**
	 * 注销处理模块
	 * 
	 * @param moduleId
	 */
	public void unRegistHandler(byte moduleId)
	{
		handlers.remove(moduleId);
	}

	/**
	 * 处理推送消息
	 */
	@Override
	public void handleResponse(byte[] pushData)
	{
		if (null == pushData || pushData.length < BusMessage.HEAD_LEN)
		{
			return;
		}
		ByteBuffer buffer = ByteBuffer.wrap(pushData);
		byte moduleId = buffer.get();
		NormalHandler handler = handlers.get(moduleId);
		if (null == handler)
		{
			return;
		}
		BaseMessage message = null;
		if (moduleId == Module.M_AUDIO)
		{
			message = new AudioMessage();
			message.parseData(pushData);
		} else
		{
			message = new BusMessage();
			message.parseData(pushData);
		}
		if (null != message)
		{
			handler.handleResponse(message);
		}
	}
}
