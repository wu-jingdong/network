package org.wjd.net.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.text.TextUtils;

public class Loger
{
	public static final byte DEBUG = 0;
	public static final byte ERROR = 2;
	public static final byte INFO = 1;

	private static final String LOG_TAG = "log_main_app";

	public static void print(String tag, Object msg, byte level)
	{
		// if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.DEBUG))
		{
			if (null != msg && null != tag)
			{
				switch (level)
				{
					case DEBUG:
					{
						android.util.Log.d(tag, msg.toString());
						break;
					}
					case INFO:
					{
						android.util.Log.i(tag, msg.toString());
						break;
					}
					case ERROR:
					{
						android.util.Log.e(tag, msg.toString());
						break;
					}
					default:
					{
						android.util.Log.i(tag, msg.toString());
						break;
					}
				}
			}
		}
	}

	public static void logToFile(String time, String msg)
	{
		if (!TextUtils.isEmpty(msg))
		{
			writeToSDCard(LOG_TAG, "[" + time + "]"
					+ "<-------------------------\n" + msg.toString()
					+ "------------------------------------>\n");
		}
	}

	private static void writeToSDCard(String fileName, String text)
	{
		File file = FileUtil.newInstance().getFile(fileName, true);
		if (null != file)
			writeFile(file, text);
	}

	private static void writeFile(File file, String logContent)
	{
		if (null == file)
			return;
		FileOutputStream fileOutputStream = null;
		FileInputStream fileInputStream = null;
		try
		{
			fileOutputStream = new FileOutputStream(file, true);
			fileOutputStream.write(logContent.getBytes());
		} catch (Exception e)
		{

		} finally
		{
			if (fileOutputStream != null)
			{
				try
				{
					fileOutputStream.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (fileInputStream != null)
			{
				try
				{
					fileInputStream.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
