package org.wjd.business.audio;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;

import org.wjd.business.base.Module;
import org.wjd.net.common.Loger;
import org.wjd.net.tcp_udp.BaseMessage;
import org.wjd.net.tcp_udp.ChannelProxy;
import org.wjd.net.tcp_udp.NormalHandler;
import org.wjd.net.tcp_udp.UnsyncRequest;
import org.wjd.speex.Speex;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;

public class AudioRecPlay implements NormalHandler
{

	private static final int REC_SIZE = 8000;

	private AudioRecord recorder;

	private int recSize = 0;

	private int frameSize = 0;

	private int readSize = 0;

	private Speex speex;

	private String ip;

	private int port;

	private InetAddress addr;

	private ChannelProxy cProxy;

	public AudioRecPlay()
	{
		speex = new Speex();
		speex.init();
		frameSize = speex.getFrameSize();
	}

	/**
	 * 设置发送音频数据需要的参数
	 * 
	 * @param cProxy
	 * @param ip
	 * @param port
	 */
	public void setParameter(ChannelProxy cProxy, String ip, int port)
	{
		this.ip = ip;
		this.port = port;
		this.cProxy = cProxy;
		try
		{
			addr = InetAddress.getByName(ip);
		} catch (UnknownHostException e)
		{
			Loger.print(this.getClass().getSimpleName(), e.getMessage(),
					Loger.ERROR);
		}
	}

	private byte[] lock = new byte[0];

	public void release()
	{
		stopPlay();
		stopRecord();
		synchronized (lock)
		{
			speex.close();
			speex = null;
		}
	}

	// record and encode --------------------------------
	public void initRecorder()
	{
		recSize = AudioRecord.getMinBufferSize(REC_SIZE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		recorder = new AudioRecord(AudioSource.MIC, REC_SIZE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				recSize);
		int temp = frameSize * 6;
		readSize = temp > recSize / 2 ? recSize / 2 : temp;
	}

	public boolean startRecord()
	{
		if (null == recorder)
		{
			initRecorder();
		}
		if (null == recorder)
		{
			return false;
		}
		Loger.print(this.getClass().getName(),
				"Audio Recorder Start Successfully!!!", Loger.INFO);
		recorder.startRecording();
		return startRecThread();
	}

	public void stopRecord()
	{
		if (null == recorder)
		{
			return;
		}
		recorder.release();
		recorder = null;
	}

	private boolean startRecThread()
	{
		EncodeThread ec = new EncodeThread();
		ec.start();
		return true;
	}

	private class EncodeThread extends Thread
	{

		@Override
		public void run()
		{
			while (isRecording())
			{
				doRecord();
			}
		}

		short[] frame = new short[frameSize];
		byte[] encoded = new byte[38];
		short[] buffer = new short[readSize];
		ShortBuffer sbuffer = ShortBuffer.wrap(buffer);

		long beginTime = 0;
		long endTime = 0;
		long duration = 1000 * readSize / REC_SIZE;

		private void doRecord()
		{
			beginTime = System.currentTimeMillis();
			int len = 0;
			try
			{
				len = recorder.read(buffer, 0, buffer.length);
			} catch (NullPointerException e)
			{
				e.printStackTrace();
				return;
			}
			if (len == buffer.length)
			{
				sbuffer.clear();
				int n = readSize / frameSize;
				for (int i = 0; i < n; ++i)
				{
					sbuffer.get(frame);
					synchronized (speex)
					{
						if (null == speex)
						{
							break;
						}
						int eLen = speex.encode(frame, encoded);
						if (eLen > 0)
						{
							sendAudioData(encoded);
						}
					}
				}
			}
			endTime = System.currentTimeMillis();
			long rDuration = endTime - beginTime;
			if (duration - 10 > rDuration)
			{
				try
				{
					Thread.sleep(duration - rDuration - 10);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void sendAudioData(byte[] encoded)
	{
		if (null == cProxy)
		{
			return;
		}
		UnsyncRequest request = new UnsyncRequest(null, null, ip, port);
		AudioMessage msg = new AudioMessage(Module.M_AUDIO, encoded);
		request.setMessage(msg);
		request.setWaitResponse(false);
		cProxy.sendRequestImmediately(request, addr);
	}

	private boolean isRecording()
	{
		return null != recorder
				&& recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
	}

	// decode and play -------------------------------------------

	private AudioTrack track;

	public void initAudioTrack()
	{
		int minBufferSize = AudioTrack.getMinBufferSize(REC_SIZE,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, REC_SIZE,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				minBufferSize, AudioTrack.MODE_STREAM);
	}

	public void startPlay()
	{
		if (null == track)
		{
			initAudioTrack();
		}
		if (null == track)
		{
			return;
		}
		track.play();
		startPlayThread();
	}

	private void startPlayThread()
	{
		DecodeThread decode = new DecodeThread();
		decode.start();
	}

	public void stopPlay()
	{
		if (null != track)
		{
			track.release();
			track = null;
		}
	}

	byte[] aLock = new byte[0];
	List<byte[]> srcs = new LinkedList<byte[]>();

	public void addAudioDataToCacahe(byte[] src)
	{
		synchronized (aLock)
		{
			srcs.add(src);
			aLock.notify();
		}
	}

	private class DecodeThread extends Thread
	{
		short[] frame = new short[frameSize];

		@Override
		public void run()
		{
			doPlay();
		}

		private void doPlay()
		{
			while (isPlaying())
			{
				byte[] data = null;
				synchronized (aLock)
				{
					if (!srcs.isEmpty())
					{
						data = srcs.remove(0);
					}
					if (null == data)
					{
						try
						{
							aLock.wait();
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				if (null == data)
				{
					continue;
				}
				int len = 0;
				synchronized (lock)
				{
					if (null == speex)
					{
						break;
					}
					len = speex.decode(data, frame, data.length);
				}
				if (len == frameSize)
				{
					try
					{
						track.write(frame, 0, frame.length);
					} catch (Exception e)
					{
						e.printStackTrace();
						break;
					}
				}
			}
		}
	}

	public boolean isPlaying()
	{
		return null != track
				&& track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
	}

	@Override
	public void handleResponse(BaseMessage message)
	{
		AudioMessage aMsg = (AudioMessage) message;
		byte[] data = aMsg.getBusiData();
		if (null != data)
		{
			Loger.print(
					this.getClass().getSimpleName(),
					"received data sequence ================== "
							+ aMsg.getSequence(), Loger.INFO);
			addAudioDataToCacahe(data);
		}
	}
}
