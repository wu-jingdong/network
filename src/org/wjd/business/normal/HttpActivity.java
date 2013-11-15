package org.wjd.business.normal;

import org.wjd.BaseActivity;
import org.wjd.R;
import org.wjd.net.http.conn.HttpChannel;
import org.wjd.net.http.conn.HttpRequest;
import org.wjd.net.http.conn.IOCallback;
import org.wjd.net.http.conn.ResponseCallback;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class HttpActivity extends BaseActivity implements IOCallback,
		ResponseCallback
{

	private ProgressBar progress;

	private WebView webView;

	private HttpChannel channel;

	private long timestamp;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_http_layout);
		progress = (ProgressBar) findViewById(R.id.progress);
		webView = (WebView) findViewById(R.id.webView);
		channel = new HttpChannel(1);
		getWebContent();
	}

	private void getWebContent()
	{
		timestamp = System.currentTimeMillis();
		HttpRequest request = new HttpRequest(timestamp, "http://www.baidu.com", this,
				this);
		channel.request(request);
	}

	@Override
	public void callback(long timestamp, byte[] response)
	{
		if (this.timestamp == timestamp)
		{
			webView.loadData(new String(response), "text/html", "utf-8");
		}
		progress.setVisibility(View.GONE);
	}

	@Override
	public void callback(long timestamp)
	{
		Toast.makeText(getBaseContext(), "Load Error!", Toast.LENGTH_SHORT)
				.show();
		progress.setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (null != channel)
		{
			channel.release();
			channel = null;
		}
	}
}
