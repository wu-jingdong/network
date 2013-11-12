package org.wjd.business.picture;

import org.wjd.BaseActivity;
import org.wjd.R;
import org.wjd.net.common.FileUtil;
import org.wjd.net.http.file.ProgressCallback;
import org.wjd.net.http.file.UDCallback;
import org.wjd.net.http.file.UDChannel;
import org.wjd.net.http.file.UDRequest;
import org.wjd.net.http.file.upload.UploadChannel;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

	private static final int TAKE_PICTURE = 1;
	private static final int GET_PICTURE = 2;

	private EditText editUrl;

	private ImageView imgPreview;

	private ProgressBar progress;

	private String localRoute;

	private String remoteRoute;

	private String matchIndicator;

	private UDChannel channel;

	private String tempPicPath;

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
		findViewById(R.id.btn_camera).setOnClickListener(this);
		channel = UploadChannel.getInstance();
	}

	private void onAlbumClick()
	{
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				"image/*");
		startActivityForResult(intent, GET_PICTURE);
	}

	private void onCameraClick()
	{
		tempPicPath = FileUtil.appendWithImg(System.currentTimeMillis()
				+ ".jpg");
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(FileUtil.newInstance().getFile(tempPicPath, true)));
		startActivityForResult(intent, TAKE_PICTURE);
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
		if (resultCode == RESULT_OK)
		{
			if (requestCode == GET_PICTURE)
			{
				handleGetPictureResult(data);
			} else if (requestCode == TAKE_PICTURE)
			{
				localRoute = tempPicPath;
				setBitmap(imgPreview, getScreenWidth(), false, localRoute);
			}
		}
	}

	private void handleGetPictureResult(Intent data)
	{
		if (data != null)
		{
			Uri uri = data.getData();
			if (!TextUtils.isEmpty(uri.getAuthority()))
			{
				Cursor cursor = getContentResolver().query(uri,
						new String[] { MediaStore.Images.Media.DATA }, null,
						null, null);
				if (null == cursor)
				{
					return;
				}
				cursor.moveToFirst();
				String path = cursor.getString(cursor
						.getColumnIndex(MediaStore.Images.Media.DATA));
				cursor.close();
				localRoute = path;
				setBitmap(imgPreview, getScreenWidth(), false, localRoute);
			} else
			{
				localRoute = uri.getPath();
				setBitmap(imgPreview, getScreenWidth(), false, localRoute);
			}
		}
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
		} else if (R.id.btn_camera == v.getId())
		{
			onCameraClick();
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
