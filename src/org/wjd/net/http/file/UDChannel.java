package org.wjd.net.http.file;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.wjd.net.http.file.UDChannel.Executor.ProgressHolder;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

/**
 * 文件上传/下载通道
 * 
 * @author wjd
 * 
 */
public abstract class UDChannel extends Handler
{

	/**
	 * 上传/下载请求队列
	 */
	private List<UDRequest> reqQueue = new ArrayList<UDRequest>();

	/**
	 * 线程池
	 */
	protected ExecutorService executors = null;

	/**
	 * 线程池大小
	 */
	protected int poolSize = 5;

	/**
	 * 发送上传/下载请求
	 * 
	 * @param request
	 */
	public synchronized void request(UDRequest request)
	{
		if (null == executors)
		{
			executors = Executors.newFixedThreadPool(poolSize);
		}
		if (TextUtils.isEmpty(request.getLocalRoute())
				|| TextUtils.isEmpty(request.getRemoteRoute()))
		{
			return;
		}

		if (reqQueue.contains(request))
		{
			return;
		}
		reqQueue.add(request);
		executors.execute(new Executor());
	}

	/**
	 * @return
	 */
	protected synchronized UDRequest getRequest()
	{
		UDRequest ret = null;
		if (!reqQueue.isEmpty())
		{
			ret = reqQueue.remove(0);
		}
		return ret;
	}

	public class Executor implements Runnable
	{
		@Override
		public void run()
		{
			UDRequest request = getRequest();
			if (null == request)
			{
				return;
			}
			if (request.isCancelled())
			{
				return;
			}
			execute(holder, request);
		}

		private ProgressHolder holder = new ProgressHolder();

		public class ProgressHolder
		{
			public int progress;
			public String indicator;
			public ProgressCallback callback;

			public void callback()
			{
				if (null != callback)
				{
					callback.publishProgress(indicator, progress);
				}
			}
		}
	}

	protected abstract void execute(ProgressHolder holder, UDRequest request);

	protected static final int WHAT_PROGRESS = 1;

	protected static final int WHAT_RESULT = 2;

	@Override
	public void handleMessage(Message msg)
	{
		if (msg.what == WHAT_PROGRESS)
		{
			ProgressHolder holder = (ProgressHolder) msg.obj;
			if (null != holder)
			{
				holder.callback();
			}
		} else if (msg.what == WHAT_RESULT)
		{
			UDRequest request = (UDRequest) msg.obj;
			if (null != request)
			{
				request.handleCallback();
			}
		}
		super.handleMessage(msg);
	}

	public void release()
	{
		if (null != executors)
		{
			executors.shutdown();
			executors = null;
		}
	}
}
