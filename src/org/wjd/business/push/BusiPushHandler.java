package org.wjd.business.push;

import org.wjd.business.base.BusMessage;
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

	private SparseArray<NormalHandler> handlers = new SparseArray<NormalHandler>();
	
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
	 * 处理推送消息
	 */
	@Override
	public void handleResponse(byte[] pushData)
	{
		if (null == pushData || pushData.length < BusMessage.HEAD_LEN)
		{
			return;
		}

		BusMessage message = new BusMessage();
		message.parseData(pushData);
		NormalHandler handler = handlers.get(message.getModuleId());
		if (null != handler)
		{
			handler.handleResponse(message);
		}
	}
}
