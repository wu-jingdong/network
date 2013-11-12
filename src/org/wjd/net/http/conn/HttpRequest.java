package org.wjd.net.http.conn;

public class HttpRequest
{

	/**
	 * 请求时间戳，用来匹配请求和响应
	 */
	private long timestamp;

	/**
	 * 请求地址
	 */
	private String remoteRoute;

	/**
	 * 请求方式， GET/POST
	 */
	private METHOD method = METHOD.GET;

	/**
	 * 请求消息体
	 */
	private String requestContent;

	/**
	 * 响应消息体
	 */
	private byte[] responseContent;

	/**
	 * 响应回调接口
	 */
	private ResponseCallback mResponseCallback;

	/**
	 * 网络异常回调
	 */
	private IOCallback mIOCallback;

	/**
	 * 取消标记
	 */
	private boolean isCancelled;

	/**
	 * 默认构造函数
	 * 
	 * @param timestamp
	 * @param remoteRoute
	 * @param method
	 * @param requestContent
	 * @param mResponseCallback
	 * @param mIOCallback
	 */
	public HttpRequest(long timestamp, String remoteRoute, METHOD method,
			String requestContent, ResponseCallback mResponseCallback,
			IOCallback mIOCallback)
	{
		super();
		this.timestamp = timestamp;
		this.remoteRoute = remoteRoute;
		this.method = method;
		this.requestContent = requestContent;
		this.mResponseCallback = mResponseCallback;
		this.mIOCallback = mIOCallback;
	}

	/**
	 * GET方式请求的构造函数
	 * 
	 * @param timestamp
	 * @param remoteRoute
	 * @param mResponseCallback
	 * @param mIOCallback
	 */
	public HttpRequest(long timestamp, String remoteRoute,
			ResponseCallback mResponseCallback, IOCallback mIOCallback)
	{
		super();
		this.timestamp = timestamp;
		this.remoteRoute = remoteRoute;
		this.mResponseCallback = mResponseCallback;
		this.mIOCallback = mIOCallback;
	}

	protected long getTimestamp()
	{
		return timestamp;
	}

	protected String getRemoteRoute()
	{
		return remoteRoute;
	}

	protected METHOD getMethod()
	{
		return method;
	}

	protected String getRequestContent()
	{
		return requestContent;
	}

	/**
	 * 取消请求
	 */
	public synchronized void cancel()
	{
		mIOCallback = null;
		mResponseCallback = null;
		isCancelled = true;
	}

	/**
	 * 获取取消标记
	 * 
	 * @return
	 */
	public synchronized boolean isCancelled()
	{
		return isCancelled;
	}

	/**
	 * 响应回调处理
	 */
	public void handleResponse()
	{
		if (null != mResponseCallback)
		{
			mResponseCallback.callback(timestamp, responseContent);
		}
	}

	/**
	 * 网络异常处理
	 */
	public void handleIOException()
	{
		if (null != mIOCallback)
		{
			mIOCallback.callback(timestamp);
		}
	}

	protected void setResponseContent(byte[] responseContent)
	{
		this.responseContent = responseContent;
	}

	public enum METHOD
	{
		GET, POST
	}
}
