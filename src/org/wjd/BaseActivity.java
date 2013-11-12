package org.wjd;

import org.wjd.business.picture.cache.ImageCache;
import org.wjd.net.tcp_udp.ChannelProxy;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

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

	@SuppressWarnings("deprecation")
	protected int getScreenWidth()
	{
		return getWindowManager().getDefaultDisplay().getWidth();
	}

	/**
	 * @param imgView
	 * @param width
	 * @param circle
	 * @param localRoute
	 * @return
	 */
	protected boolean setBitmap(ImageView imgView, int width, boolean circle,
			String localRoute)
	{
		Bitmap bitmap = ImageCache.getInstance().getCacheBitmap(localRoute,
				getScreenWidth(), false);
		if (null != bitmap)
		{
			imgView.setImageBitmap(bitmap);
			return true;
		}
		return false;
	}
}
