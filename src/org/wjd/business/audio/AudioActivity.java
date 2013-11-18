package org.wjd.business.audio;

import org.wjd.App;
import org.wjd.BaseActivity;
import org.wjd.R;
import org.wjd.business.Module;
import org.wjd.business.push.BusiPushHandler;
import org.wjd.net.tcp_udp.ChannelProxy.CHANNEL_TYPE;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class AudioActivity extends BaseActivity implements OnClickListener
{

	private EditText editIp;
	private AudioRecPlay aRecPlay;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_audio_layout);
		findViewById(R.id.btn_audio_start).setOnClickListener(this);
		findViewById(R.id.btn_audio_end).setOnClickListener(this);
		editIp = (EditText) findViewById(R.id.edit_ip);
		if (App.CTYPE != CHANNEL_TYPE.TYPE_UDP)
		{
			Toast.makeText(getBaseContext(), R.string.app_name,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		aRecPlay = new AudioRecPlay();
		aRecPlay.startPlay();
		BusiPushHandler.instance.registHandler(Module.M_AUDIO, aRecPlay);
	}

	/**
	 * 开始发送音频数据
	 */
	private void startSendAudio()
	{
		String ip = editIp.getText().toString();

		if (TextUtils.isEmpty(ip))
		{
			Toast.makeText(getBaseContext(), "Start Error!", Toast.LENGTH_LONG)
					.show();
			return;
		}
		aRecPlay.setParameter(getChannelProxy(), ip, 10101);
		aRecPlay.startRecord();
	}

	/**
	 * 停止发送音频数据
	 */
	private void stopSendAudio()
	{
		if (null != aRecPlay)
		{
			aRecPlay.stopRecord();
		}
	}

	@Override
	public void onClick(View v)
	{
		if (R.id.btn_audio_start == v.getId())
		{
			startSendAudio();
		} else if (R.id.btn_audio_end == v.getId())
		{
			stopSendAudio();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (null != aRecPlay)
		{
			aRecPlay.release();
			aRecPlay = null;
		}
		BusiPushHandler.instance.unRegistHandler(Module.M_AUDIO);
	}
}
