package org.wjd;

import org.wjd.net.tcp_udp.ChannelProxy;

import android.app.Activity;

public class BaseActivity extends Activity
{

	protected ChannelProxy getChannelProxy()
	{
		return ((App) getApplication()).getChannelProxy();
	}

	protected App getApp()
	{
		return (App) getApplication();
	}
}
