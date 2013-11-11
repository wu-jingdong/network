package org.wjd;

import org.wjd.business.push.BusiPushHandler;
import org.wjd.net.common.FileUtil;
import org.wjd.net.tcp_udp.ChannelProxy;
import org.wjd.net.tcp_udp.ChannelProxy.CHANNEL_TYPE;

import android.app.Application;
import android.os.Process;

public class App extends Application
{

	public static final CHANNEL_TYPE CTYPE = CHANNEL_TYPE.TYPE_UDP;

	private ChannelProxy cProxy;

	@Override
	public void onCreate()
	{
		super.onCreate();
		FileUtil.newInstance().initDir(getBaseContext());
		cProxy = new ChannelProxy(CTYPE);
		cProxy.setPushHandler(BusiPushHandler.instance);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// 初始化本地网络连接
				cProxy.init("127.0.0.1", 10011);
			}
		}).start();
	}

	public ChannelProxy getChannelProxy()
	{
		return cProxy;
	}

	public void exitApp()
	{
		if (null != cProxy)
		{
			cProxy.unInit();
		}
		Process.killProcess(Process.myPid());
	}
}
