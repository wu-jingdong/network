package org.wjd;

import org.wjd.business.audio.AudioRecPlay;
import org.wjd.business.base.BusMessage;
import org.wjd.business.base.Module;
import org.wjd.business.push.BusiPushHandler;
import org.wjd.net.tcp_udp.BaseMessage;
import org.wjd.net.tcp_udp.ChannelProxy;
import org.wjd.net.tcp_udp.ChannelProxy.CHANNEL_TYPE;
import org.wjd.net.tcp_udp.NetErrorHandler;
import org.wjd.net.tcp_udp.NormalHandler;
import org.wjd.net.tcp_udp.UnsyncRequest;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements NetErrorHandler,
		NormalHandler, OnClickListener
{

	private EditText editContent;

	private EditText editIp;

	private TextView tvReceived;

	private ChannelProxy cProxy;

	private AudioRecPlay aRecPlay;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editContent = (EditText) findViewById(R.id.edit_content);
		findViewById(R.id.btn_send).setOnClickListener(this);
		tvReceived = (TextView) findViewById(R.id.tv_received);
		findViewById(R.id.btn_audio_start).setOnClickListener(this);
		findViewById(R.id.btn_audio_end).setOnClickListener(this);
		editIp = (EditText) findViewById(R.id.edit_ip);

		// 修改此处参数即可分别测试udp和tcp
		cProxy = new ChannelProxy(CHANNEL_TYPE.TYPE_UDP);
		cProxy.setPushHandler(BusiPushHandler.instance);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// 初始化网络连接
				cProxy.init("127.0.0.1", 10011);
			}
		}).start();
		aRecPlay = new AudioRecPlay();
		BusiPushHandler.instance.registHandler(Module.M_AUDIO, aRecPlay);
		aRecPlay.startPlay();
	}

	/**
	 * 发送文字消息
	 */
	private void onSend()
	{
		String content = editContent.getText().toString();
		if (TextUtils.isEmpty(content))
		{
			return;
		}
		UnsyncRequest request = new UnsyncRequest(this, this, "127.0.0.1",
				10011);
		byte[] data = content.getBytes();
		BaseMessage message = new BusMessage((byte) 1, (byte) 1, 12345,
				(byte) 2, data);
		request.setMessage(message);
		cProxy.sendRequest(request);
	}

	@Override
	public void handleResponse(BaseMessage message)
	{
		tvReceived.setText(message.toString());
	}

	@Override
	public void handleNetError(BaseMessage message)
	{
		tvReceived.setText("Send Error!");
	}

	@Override
	protected void onDestroy()
	{
		if (null != cProxy)
		{
			cProxy.unInit();
		}
		if (null != aRecPlay)
		{
			aRecPlay.release();
			aRecPlay = null;
		}
		BusiPushHandler.instance.unRegistHandler(Module.M_AUDIO);
		super.onDestroy();
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
		aRecPlay.setParameter(cProxy, ip, 10100);
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
		switch (v.getId())
		{
			case R.id.btn_send:
				onSend();
				break;
			case R.id.btn_audio_start:
				startSendAudio();
				break;
			case R.id.btn_audio_end:
				stopSendAudio();
				break;
			default:
				break;
		}
	}
}
