package org.wjd;

import java.util.Timer;
import java.util.TimerTask;

import org.wjd.business.push.BusiPushHandler;
import org.wjd.net.common.FileUtil;
import org.wjd.net.server.Server;
import org.wjd.net.server.TcpServer;
import org.wjd.net.server.UdpServer;
import org.wjd.net.tcp_udp.ChannelProxy;
import org.wjd.net.tcp_udp.ChannelProxy.CHANNEL_TYPE;

import android.app.Application;
import android.os.Process;

public class App extends Application
{

	public static final CHANNEL_TYPE CTYPE = CHANNEL_TYPE.TYPE_NORMAL_TCP;

	private ChannelProxy cProxy;

	private Server server;

	@Override
	public void onCreate()
	{
		super.onCreate();
		FileUtil.newInstance().initDir(getBaseContext());
		cProxy = new ChannelProxy(CTYPE);
		cProxy.setPushHandler(BusiPushHandler.instance);
		// 初始化服务器网络
		server = (App.CTYPE == CHANNEL_TYPE.TYPE_UDP) ? new UdpServer()
				: new TcpServer();
		server.start();
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				// 初始化本地网络连接
				cProxy.init("127.0.0.1", 10011);
			}
		}, 1000);
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
			cProxy = null;
		}
		if (null != server)
		{
			server.stopThread();
			server = null;
		}
		Process.killProcess(Process.myPid());
	}
}
