package org.wjd.net.http.file.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.wjd.net.common.FileUtil;
import org.wjd.net.common.Loger;
import org.wjd.net.http.file.UDCallback;
import org.wjd.net.http.file.UDRequest;
import org.wjd.net.http.file.UDChannel;
import org.wjd.net.http.file.UDChannel.Executor.ProgressHolder;

/**
 * 文件下载通道
 * 
 * @author wjd
 * 
 */
public class DownloadChannel extends UDChannel
{

	private static DownloadChannel instance = null;

	public synchronized static UDChannel getInstance()
	{
		if (null == instance)
		{
			instance = new DownloadChannel();
		}
		return instance;
	}

	private DownloadChannel()
	{

	}

	protected void execute(ProgressHolder holder)
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
		doQueue.add(request);
		HttpParams params = new BasicHttpParams();

		// set timeout
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);

		// set message body
		HttpClient httpClient = new DefaultHttpClient(params);

		HttpGet get = new HttpGet(request.getRemoteRoute());

		// send message
		HttpResponse response = null;
		try
		{
			response = httpClient.execute(get);
			boolean success = response.getStatusLine().getStatusCode() == 200;
			if (success)
			{
				int totalLen = Integer.parseInt(response.getFirstHeader(
						"Content-Length").getValue());
				success = storeFile(get, holder, response.getEntity()
						.getContent(), request, totalLen);
			}
			if (null != request.getmDownloadCallback())
			{
				request.setDownloadResult(success ? UDCallback.SUCCESS
						: UDCallback.FAIL);
				obtainMessage(WHAT_RESULT, request).sendToTarget();
			}
		} catch (IOException e)
		{
			if (null != request.getmDownloadCallback())
			{
				obtainMessage(WHAT_RESULT, request).sendToTarget();
			}
			e.printStackTrace();
		} finally
		{
			doQueue.remove(request);
		}
	}

	/**
	 * 保存文件
	 * 
	 * @param is
	 * @param request
	 * @return
	 */
	private boolean storeFile(HttpGet get, ProgressHolder holder,
			InputStream is, UDRequest request, int totalLen)
	{
		holder.indicator = request.getMatchIndicator();
		holder.callback = request.getmProgressCallback();

		boolean flag = false;
		if (null == is)
			return flag;
		File file = null;
		FileOutputStream fos = null;
		try
		{
			file = FileUtil.newInstance()
					.getFile(request.getLocalRoute(), true);
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length = 0;
			int count = 0;
			while ((length = is.read(buffer)) > 0)
			{
				fos.write(buffer, 0, length);
				count += length;
				if (request.isCancelled())
				{
					get.abort();
					return false;
				}
				if (null != request.getmProgressCallback())
				{
					holder.progress = 100 * count / totalLen;
					obtainMessage(WHAT_PROGRESS, holder).sendToTarget();
				}
			}
			flag = true;
		} catch (Exception e)
		{
			Loger.print("FileUtil", e.getMessage(), Loger.ERROR);
			flag = false;
		} finally
		{
			if (null != fos)
			{
				try
				{
					fos.flush();
					fos.close();
				} catch (IOException e)
				{
					Loger.print("FileUtil", e.getMessage(), Loger.ERROR);
					flag = false;
				}
			}
			if (null != is)
			{
				try
				{
					is.close();
				} catch (IOException e)
				{
					flag = false;
				}
			}
		}
		return flag;
	}
}
