package org.wjd.net.http.file;

import android.text.TextUtils;

/**
 * 文件上传/下载请求
 * 
 * @author wjd
 * 
 */
public class UDRequest
{

	/**
	 * 上传/下载文件的本地存储路径
	 */
	private String localRoute;

	/**
	 * 上传/下载文件的远程地址
	 */
	private String remoteRoute;

	/**
	 * 请求和响应的对应标记，由应用程序自行设置
	 */
	private String matchIndicator;

	/**
	 * 文件上传/下载完成的回调
	 */
	private UDCallback mDownloadCallback;

	/**
	 * 文件上传/下载进度回调
	 */
	private ProgressCallback mProgressCallback;

	/**
	 * 上传/下载结果
	 */
	private int downloadResult = UDCallback.FAIL;

	/**
	 * 取消标记
	 */
	private boolean isCancelled;

	/**
	 * 服务器响应数据
	 */
	private byte[] response;

	/**
	 * 全参数构造方法
	 * 
	 * @param localRoute
	 * @param remoteRoute
	 * @param matchIndicator
	 * @param mDownloadCallback
	 * @param mProgressCallback
	 */
	public UDRequest(String localRoute, String remoteRoute,
			String matchIndicator, UDCallback mDownloadCallback,
			ProgressCallback mProgressCallback)
	{
		super();
		this.localRoute = localRoute;
		this.remoteRoute = remoteRoute;
		this.matchIndicator = matchIndicator;
		this.mDownloadCallback = mDownloadCallback;
		this.mProgressCallback = mProgressCallback;
	}

	/**
	 * 缺少进度回调的构造方法
	 * 
	 * @param localRoute
	 * @param remoteRoute
	 * @param matchIndicator
	 * @param mDownloadCallback
	 */
	public UDRequest(String localRoute, String remoteRoute,
			String matchIndicator, UDCallback mDownloadCallback)
	{
		super();
		this.localRoute = localRoute;
		this.remoteRoute = remoteRoute;
		this.matchIndicator = matchIndicator;
		this.mDownloadCallback = mDownloadCallback;
	}

	public String getLocalRoute()
	{
		return localRoute;
	}

	public String getRemoteRoute()
	{
		return remoteRoute;
	}

	public String getMatchIndicator()
	{
		return matchIndicator;
	}

	public UDCallback getmDownloadCallback()
	{
		return mDownloadCallback;
	}

	public ProgressCallback getmProgressCallback()
	{
		return mProgressCallback;
	}

	public int getDownloadResult()
	{
		return downloadResult;
	}

	public void setDownloadResult(int downloadResult)
	{
		this.downloadResult = downloadResult;
	}

	/**
	 * 处理上传/下载结果
	 */
	public void handleCallback()
	{
		if (null != mDownloadCallback)
		{
			mDownloadCallback.callback(matchIndicator, downloadResult);
		}
	}

	/**
	 * 取消上传/下载请求
	 */
	public synchronized void cancelRequest()
	{
		mDownloadCallback = null;
		mProgressCallback = null;
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

	public byte[] getResponse()
	{
		return response;
	}

	public void setResponse(byte[] response)
	{
		this.response = response;
	}

	@Override
	public boolean equals(Object o)
	{
		if (TextUtils.isEmpty(this.localRoute))
		{
			return false;
		}
		return this.localRoute.equals(((UDRequest) o).localRoute);
	}
}
