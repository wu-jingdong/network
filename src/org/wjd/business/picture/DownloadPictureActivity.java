package org.wjd.business.picture;

import org.wjd.BaseActivity;
import org.wjd.R;
import org.wjd.net.common.FileUtil;
import org.wjd.net.http.file.ProgressCallback;
import org.wjd.net.http.file.UDCallback;
import org.wjd.net.http.file.UDChannel;
import org.wjd.net.http.file.UDRequest;
import org.wjd.net.http.file.download.DownloadChannel;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Demonstration file download via picture download.
 * 
 * @author wjd
 * 
 */
public class DownloadPictureActivity extends BaseActivity implements
		OnClickListener, UDCallback, ProgressCallback
{

	private EditText editUrl;

	private Button btnDownload;

	private ImageView imgPreview;

	private ProgressBar progress;

	private UDChannel downloadChannel;

	private String matchIndicator;

	private String localRoute;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_download_picture_layout);
		editUrl = (EditText) findViewById(R.id.edit_url);
		btnDownload = (Button) findViewById(R.id.btn_download);
		btnDownload.setOnClickListener(this);
		imgPreview = (ImageView) findViewById(R.id.img_preview);
		progress = (ProgressBar) findViewById(R.id.progress);
		progress.setVisibility(View.GONE);
		downloadChannel = DownloadChannel.getInstance();
	}

	/**
	 * begin to download picture.
	 */
	private void download()
	{

		String remoteRoute = editUrl.getText().toString();
		if (TextUtils.isEmpty(remoteRoute))
		{
			return;
		}
		int idx = remoteRoute.lastIndexOf("/");
		if (idx == -1)
		{
			return;
		}

		localRoute = FileUtil.appendWithImg(remoteRoute.substring(idx + 1));

		// set bitmap via local cache
		if (setBitmap(imgPreview, getScreenWidth(), false, localRoute))
		{
			return;
		}

		// download from internet
		btnDownload.setEnabled(false);
		progress.setVisibility(View.VISIBLE);
		progress.setProgress(0);
		matchIndicator = String.valueOf(System.currentTimeMillis());
		UDRequest request = new UDRequest(localRoute, remoteRoute,
				matchIndicator, this, this);
		downloadChannel.request(request);
	}

	@Override
	public void onClick(View v)
	{
		if (R.id.btn_download == v.getId())
		{
			download();
		}
	}

	@Override
	public void publishProgress(String matchIndicator, int progress)
	{
		if (this.matchIndicator.equals(matchIndicator))
		{
			this.progress.setProgress(progress);
		}
	}

	@Override
	public void callback(String matchIndicator, int result)
	{
		if (this.matchIndicator.equals(matchIndicator))
		{
			this.progress.setVisibility(View.GONE);
			btnDownload.setEnabled(true);
			if (result == SUCCESS)
			{
				setBitmap(imgPreview, getScreenWidth(), false, localRoute);
			} else
			{
				Toast.makeText(getBaseContext(), "Download Error",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
