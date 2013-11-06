package org.wjd.net.http.file;

import java.util.ArrayList;
import java.util.List;

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
	 * 默认的上传/下载线程数量
	 */
	private static final int POOL_SIZE = 2;

	/**
	 * 最大的上传/下载线程数量
	 */
	private static final int MAX_SIZE = 5;

	/**
	 * 上传/下载线程组
	 */
	private ArrayList<Executor> threads = new ArrayList<Executor>();

	/**
	 * 上传/下载请求队列
	 */
	private List<UDRequest> reqQueue = new ArrayList<UDRequest>();

	/**
	 * 正在执行的上传/下载任务
	 */
	protected List<UDRequest> doQueue = new ArrayList<UDRequest>();

	/**
	 * 发送上传/下载请求
	 * 
	 * @param request
	 */
	public synchronized void request(UDRequest request)
	{

		if (TextUtils.isEmpty(request.getLocalRoute())
				|| TextUtils.isEmpty(request.getRemoteRoute()))
		{
			return;
		}

		if (reqQueue.contains(request) || doQueue.contains(request))
		{
			return;
		}
		reqQueue.add(request);
		executeThread();
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

	/**
	 * 启动发送线程，发送上传/下载请求
	 */
	private void executeThread()
	{
		int size = threads.size();
		for (int i = 0; i < size; ++i)
		{
			if (threads.get(i).waiting)
			{
				synchronized (threads.get(i))
				{
					threads.get(i).notify();
				}
				return;
			}
		}
		// 如果在线程池中没找到空闲的线程
		if (size == MAX_SIZE)
		{
			// 线程池数量已饱和，此请求暂时等待
			return;
		}
		// 生成新的线程
		Executor th = new Executor("http thread " + size);
		if (size < POOL_SIZE)
		{
			th.addflag = true;
		}
		threads.add(th);
		th.start();
	}

	public class Executor extends Thread
	{

		/**
		 * 标记线程是否为等待状态
		 */
		boolean waiting = false;

		/**
		 * 标记线程执行完上传/下载任务以后是否添加到线程池中
		 */
		boolean addflag = false;

		public Executor()
		{
			super();
		}

		public Executor(String threadName)
		{
			super(threadName);
		}

		@Override
		public void run()
		{
			do
			{
				execute(holder);
				if (addflag)
				{
					synchronized (this)
					{
						try
						{
							waiting = true;
							this.wait();
						} catch (InterruptedException e)
						{
						}
					}
				}
				waiting = false;
			} while (addflag);
			threads.remove(this);
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

	protected abstract void execute(ProgressHolder holder);

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
}
