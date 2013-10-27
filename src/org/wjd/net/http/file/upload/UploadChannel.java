package org.wjd.net.http.file.upload;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.wjd.net.common.FileUtil;
import org.wjd.net.common.Loger;
import org.wjd.net.http.file.UDCallback;
import org.wjd.net.http.file.UDChannel;
import org.wjd.net.http.file.UDChannel.Executor.ProgressHolder;
import org.wjd.net.http.file.UDRequest;

public class UploadChannel extends UDChannel
{

	private static UploadChannel instance = null;

	public synchronized static UploadChannel getInstance()
	{
		if (null == instance)
		{
			instance = new UploadChannel();
		}
		return instance;
	}

	private UploadChannel()
	{
	}

	@Override
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
		holder.indicator = request.getMatchIndicator();
		holder.callback = request.getmProgressCallback();

		String end = "\r\n";
		String twoHen = "--";
		String boundary = "wjd_" + java.util.UUID.randomUUID().toString()
				+ "_wjd";
		URL url = null;
		try
		{
			url = new URL(request.getRemoteRoute());
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		if (null == url)
		{
			return;
		}
		HttpURLConnection conn = null;
		try
		{
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e)
		{
			obtainMessage(WHAT_RESULT, request).sendToTarget();
			e.printStackTrace();
		}
		conn.setConnectTimeout(5000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);

		try
		{
			conn.setRequestMethod("POST");
		} catch (ProtocolException e)
		{
			e.printStackTrace();
		}
		conn.setRequestProperty("Accept",
				"text/html, application/xhtml+xml, image/jpeg,*/*");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="
				+ boundary);
		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");

		/* DataOutputStream */
		DataOutputStream dos = null;
		FileInputStream fis = null;
		try
		{
			dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(twoHen + boundary + end);
			dos.writeBytes("Content-Disposition:form-data; "
					+ "name=\"f\";filename=\"" + request.getLocalRoute() + "\""
					+ end);
			dos.writeBytes("Content-Type:image/*, video/*" + end + end);

			/* FileInputStream */
			fis = new FileInputStream(FileUtil.newInstance().getFile(
					request.getLocalRoute(), false));

			/* 1024bytes */
			byte[] buffer = new byte[1024];

			int count = 0;
			int length = -1;
			int available = fis.available();
			while ((length = fis.read(buffer)) != -1)
			{
				dos.write(buffer, 0, length);
				count += length;
				if (request.isCancelled())
				{
					conn.disconnect();
					throw new IOException();
				}
				if (null != request.getmProgressCallback())
				{
					holder.progress = 100 * count / available;
					obtainMessage(WHAT_PROGRESS, holder).sendToTarget();
				}
			}

			dos.writeBytes(end);
			dos.writeBytes(twoHen + boundary + twoHen);
			dos.writeBytes(end);
			dos.flush();
			int rspCode = conn.getResponseCode();
			boolean success = HttpURLConnection.HTTP_OK == rspCode;
			if (success)
			{
				InputStream is = conn.getInputStream();
				int len = is.available();
				if (len > 0)
				{
					byte[] buff = new byte[len];
					is.read(buffer);
					Loger.print(this.getClass().getSimpleName(), new String(
							buff), Loger.INFO);
					request.setResponse(buff);
				}
				is.close();
			}
			if (success)
			{
				request.setDownloadResult(UDCallback.SUCCESS);
			}
			obtainMessage(WHAT_RESULT, request).sendToTarget();
		} catch (IOException e)
		{
			obtainMessage(WHAT_RESULT, request).sendToTarget();
			e.printStackTrace();
		} finally
		{
			if (null != fis)
			{
				try
				{
					fis.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (null != dos)
			{
				try
				{
					dos.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			fis = null;
			dos = null;
		}
	}
}
