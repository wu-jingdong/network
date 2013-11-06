package org.wjd.net.http.conn;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.wjd.net.common.Loger;
import org.wjd.net.http.conn.HttpRequest.METHOD;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

/**
 * 请求通道
 * 
 * @author wjd
 * 
 */
public abstract class HttpChannel extends Handler
{

	/**
	 * 默认的请求线程数量
	 */
	private static final int POOL_SIZE = 2;

	/**
	 * 最大的请求线程数量
	 */
	private static final int MAX_SIZE = 5;

	/**
	 * 请求线程组
	 */
	private ArrayList<Executor> threads = new ArrayList<Executor>();

	/**
	 * 请求队列
	 */
	private List<HttpRequest> reqQueue = new ArrayList<HttpRequest>();

	/**
	 * 正在执行的请求任务
	 */
	protected List<HttpRequest> doQueue = new ArrayList<HttpRequest>();

	/**
	 * 发送请求
	 * 
	 * @param request
	 */
	public synchronized void request(HttpRequest request)
	{
		if (TextUtils.isEmpty(request.getRemoteRoute()))
		{
			return;
		}

		reqQueue.add(request);
		executeThread();
	}

	/**
	 * @return
	 */
	protected synchronized HttpRequest getRequest()
	{
		HttpRequest ret = null;
		if (!reqQueue.isEmpty())
		{
			ret = reqQueue.remove(0);
		}
		return ret;
	}

	/**
	 * 启动发送线程，发送请求
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
			// 线程池数量已饱和，此暂时等待
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
		 * 标记线程执行完请求任务以后是否添加到线程池中
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
				try
				{
					Thread.sleep(1000l);
				} catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
				execute();
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
	}

	protected void execute()
	{
		HttpRequest request = getRequest();
		if (null == request)
		{
			return;
		}
		if (request.isCancelled())
		{
			return;
		}
		doQueue.add(request);
		HttpParams params = new BasicHttpParams();

		// set timeout
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);

		// set message body
		HttpClient httpClient = new DefaultHttpClient(params);

		HttpRequestBase base = null;
		if (request.getMethod().ordinal() == METHOD.GET.ordinal())
		{
			base = new HttpGet(request.getRemoteRoute());
		} else
		{
			base = new HttpPost(request.getRemoteRoute());
			try
			{
				((HttpPost) base).setEntity(new StringEntity(request
						.getRequestContent(), HTTP.UTF_8));
			} catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}

		// send message
		HttpResponse response = null;
		try
		{
			response = httpClient.execute(base);
			boolean success = response.getStatusLine().getStatusCode() == 200;
			if (success)
			{
				HttpEntity rspEntity = response.getEntity();
				InputStream is = rspEntity.getContent();
				int len = is.available();
				if (len > 0)
				{
					byte[] buffer = new byte[len];
					is.read(buffer);
					Loger.print(this.getClass().getSimpleName(), new String(
							buffer), Loger.INFO);
					request.setResponseContent(buffer);
				}
				is.close();
			}
			if (success)
			{
				obtainMessage(WHAT_IO, request).sendToTarget();
			} else
			{
				obtainMessage(WHAT_RESULT, request).sendToTarget();
			}
		} catch (IOException e)
		{
			obtainMessage(WHAT_IO, request).sendToTarget();
			e.printStackTrace();
		} finally
		{
			doQueue.remove(request);
		}
	}

	protected static final int WHAT_IO = 1;

	protected static final int WHAT_RESULT = 2;

	@Override
	public void handleMessage(Message msg)
	{
		if (msg.what == WHAT_IO)
		{
			HttpRequest request = (HttpRequest) msg.obj;
			if (null != request)
			{
				request.handleIOException();
			}
		} else if (msg.what == WHAT_RESULT)
		{
			HttpRequest request = (HttpRequest) msg.obj;
			if (null != request)
			{
				request.handleResponse();
			}
		}
		super.handleMessage(msg);
	}
}
