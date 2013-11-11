package org.wjd.business.picture;

import org.wjd.BaseActivity;
import org.wjd.R;
import org.wjd.net.http.file.ProgressCallback;
import org.wjd.net.http.file.UDCallback;
import org.wjd.net.http.file.UDChannel;
import org.wjd.net.http.file.UDRequest;
import org.wjd.net.http.file.download.DownloadChannel;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class UploadPictureActivity extends BaseActivity implements
		OnClickListener, UDCallback, ProgressCallback
{

	private EditText editUrl;

	private ImageView imgPreview;

	private ProgressBar progress;

	private String localRoute;

	private String remoteRoute;

	private String matchIndicator;

	private UDChannel channel;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_upload_picture_layout);
		editUrl = (EditText) findViewById(R.id.edit_upload_url);
		imgPreview = (ImageView) findViewById(R.id.img_preview);
		progress = (ProgressBar) findViewById(R.id.progress);
		findViewById(R.id.btn_upload).setOnClickListener(this);
		findViewById(R.id.btn_album).setOnClickListener(this);
		channel = DownloadChannel.getInstance();
	}

	private void onAlbumClick()
	{
		
	}

	private void onUpload()
	{
		remoteRoute = editUrl.getText().toString();
		if (TextUtils.isEmpty(remoteRoute) || TextUtils.isEmpty(localRoute))
		{
			return;
		}
		matchIndicator = String.valueOf(System.currentTimeMillis());
		UDRequest request = new UDRequest(localRoute, remoteRoute,
				matchIndicator, this, this);
		channel.request(request);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v)
	{
		if (R.id.btn_album == v.getId())
		{
			onAlbumClick();
		} else if (R.id.btn_upload == v.getId())
		{
			onUpload();
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
		if (!this.matchIndicator.equals(matchIndicator))
		{
			return;
		}
		Toast.makeText(getBaseContext(),
				result == SUCCESS ? "Upload Success!" : "Upload Fail!",
				Toast.LENGTH_SHORT).show();
	}
}
