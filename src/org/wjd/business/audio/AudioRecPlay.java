package org.wjd.business.audio;

import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;

import org.wjd.net.common.Loger;
import org.wjd.speex.Speex;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;

public class AudioRecPlay
{

	private static final int REC_SIZE = 8000;

	private AudioRecord recorder;

	private int recSize = 0;

	private int frameSize = 0;

	private int readSize = 0;

	private Speex speex;

	private AudioService aService;

	public AudioRecPlay()
	{
		this.aService = new AudioService();
		speex = new Speex();
		speex.init();
		frameSize = speex.getFrameSize();
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
					}
					int eLen = speex.encode(frame, encoded);
					Loger.print(this.getClass().getSimpleName(),
							"encode len ======== " + eLen, Loger.INFO);
					if (eLen > 0)
					{
						aService.sendAudioData(encoded);
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
}
