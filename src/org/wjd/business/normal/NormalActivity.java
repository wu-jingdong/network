package org.wjd.business.normal;

import org.wjd.BaseActivity;
import org.wjd.R;
import org.wjd.business.Module;
import org.wjd.net.tcp_udp.BaseMessage;
import org.wjd.net.tcp_udp.NetErrorHandler;
import org.wjd.net.tcp_udp.ResponseHandler;
import org.wjd.net.tcp_udp.UnsyncRequest;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class NormalActivity extends BaseActivity implements OnClickListener,
		NetErrorHandler, ResponseHandler
{

	private EditText editContent;

	private TextView tvReceived;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_primary_message_layout);
		editContent = (EditText) findViewById(R.id.edit_content);
		tvReceived = (TextView) findViewById(R.id.tv_received);
		findViewById(R.id.btn_send).setOnClickListener(this);
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
		BaseMessage message = new BusiMessage(Module.M_NORMAL, (byte) 1, 12345,
				(byte) 2, data);
		request.setMessage(message);
		getChannelProxy().sendRequest(request);
	}

	@Override
	public void onClick(View v)
	{
		if (R.id.btn_send == v.getId())
		{
			onSend();
		}
	}

	@Override
	public void handleResponse(BaseMessage message)
	{
		BusiMessage msg = (BusiMessage) message;
		tvReceived.setText("Response:\n");
		// tvReceived.append(msg.statusOk() ? msg.getBusiString() : "Error!");
		tvReceived.append(msg.getBusiString());
	}

	@Override
	public void handleNetError(BaseMessage message)
	{
		tvReceived.setText("Response:\n");
		tvReceived.append("Network Error!");
	}
}
