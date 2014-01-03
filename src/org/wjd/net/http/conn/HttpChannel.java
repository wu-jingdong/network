package org.wjd.net.http.conn;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.apache.http.util.EntityUtils;
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
public class HttpChannel extends Handler
{

	/**
	 * 请求队列
	 */
	private List<HttpRequest> reqQueue = new ArrayList<HttpRequest>();

	/**
	 * 线程池
	 */
	private ExecutorService executors = null;

	/**
	 * 构造方法
	 * 
	 * @param poolSize
	 *            线程池大小
	 */
	public HttpChannel(int poolSize)
	{
		executors = Executors.newFixedThreadPool(poolSize);
	}

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
		executors.execute(new Executor());
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

	public class Executor implements Runnable
	{
		@Override
		public void run()
		{
			execute();
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
				request.setResponseContent(EntityUtils.toByteArray(response
						.getEntity()));
			}
			if (success)
			{
				obtainMessage(WHAT_RESULT, request).sendToTarget();
			} else
			{
				obtainMessage(WHAT_IO, request).sendToTarget();
			}
		} catch (Exception e)
		{
			obtainMessage(WHAT_IO, request).sendToTarget();
			e.printStackTrace();
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
	}

	/**
	 * 释放线程池
	 */
	public void release()
	{
		if (null != executors)
		{
			executors.shutdown();
			executors = null;
		}
	}
}
