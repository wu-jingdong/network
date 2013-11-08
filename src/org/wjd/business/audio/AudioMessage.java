package org.wjd.business.audio;

import java.nio.ByteBuffer;

import org.wjd.net.tcp_udp.BaseMessage;

/**
 * 音频消息
 * 
 * @author wjd
 * 
 */
public class AudioMessage extends BaseMessage
{

	/**
	 * 消息头长度
	 */
	public static final int HEAD_LEN = 3;

	/**
	 * 全局消息序列号，每构造一个消息加1
	 */
	public static byte SEQUENCE = 0;

	/**
	 * 模块id
	 */
	private byte moduleId;

	/**
	 * 序列号
	 */
	private short sequence;

	/**
	 * 音频数据
	 */
	private byte[] a_data;

	public AudioMessage()
	{

	}

	public AudioMessage(byte moduleId, byte[] a_data)
	{
		super();
		this.moduleId = moduleId;
		this.a_data = a_data;
		sequence = SEQUENCE;
		if (++SEQUENCE == Short.MAX_VALUE)
		{
			SEQUENCE = 0;
		}
	}

	@Override
	public boolean match(ByteBuffer wrapper)
	{
		return false;
	}

	@Override
	public byte[] createData()
	{
		int len = a_data.length;
		byte[] data = new byte[HEAD_LEN + len];
		ByteBuffer wrapper = ByteBuffer.wrap(data);
		wrapper.put(moduleId);
		wrapper.putShort(sequence);
		wrapper.put(a_data);
		return data;
	}

	@Override
	public void parseData(byte[] receivedData)
	{
		if (null == receivedData || receivedData.length <= HEAD_LEN)
		{
			return;
		}

		ByteBuffer buffer = ByteBuffer.wrap(receivedData);
		moduleId = buffer.get();
		sequence = buffer.getShort();
		if (receivedData.length > HEAD_LEN)
		{
			a_data = new byte[receivedData.length - HEAD_LEN];
			buffer.get(a_data);
		} else
		{
			a_data = null;
		}
	}

	public byte[] getBusiData()
	{
		return a_data;
	}

	public short getSequence()
	{
		return sequence;
	}
}
