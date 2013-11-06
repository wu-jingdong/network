package org.wjd.business.base;

import java.nio.ByteBuffer;

import org.wjd.net.tcp_udp.BaseMessage;

public class BusMessage extends BaseMessage
{

	/**
	 * 消息头长度
	 */
	public static final int HEAD_LEN = 13;

	/**
	 * 全局消息序列号，每构造一个消息加1
	 */
	public static byte SEQUENCE = 0;

	/**
	 * 模块ID
	 */
	private byte moduleId;

	/**
	 * 命令码
	 */
	private byte commondId;

	/**
	 * 用户ID
	 */
	private long userId;

	/**
	 * 客户端类型
	 */
	private byte clientType;

	/**
	 * 消息序列号
	 */
	private byte sequence;

	/**
	 * 状态
	 */
	private byte status = -1;

	/**
	 * 业务数据
	 */
	private byte[] busiData;

	public BusMessage()
	{

	}

	public BusMessage(byte moduleId, byte commondId, long userId,
			byte clientType, byte[] busiData)
	{
		super();
		this.moduleId = moduleId;
		this.commondId = commondId;
		this.userId = userId;
		this.clientType = clientType;
		this.busiData = busiData;
		sequence = SEQUENCE;
		if (++SEQUENCE == Byte.MAX_VALUE)
		{
			SEQUENCE = 0;
		}
	}

	public byte getModuleId()
	{
		return moduleId;
	}

	public byte getCommondId()
	{
		return commondId;
	}

	public long getUserId()
	{
		return userId;
	}

	public byte getClientType()
	{
		return clientType;
	}

	public byte getSequence()
	{
		return sequence;
	}

	public byte[] getBusiData()
	{
		return busiData;
	}

	@Override
	public boolean match(ByteBuffer wrapper)
	{
		if (null == wrapper)
		{
			return false;
		}
		return moduleId == wrapper.get() && commondId == wrapper.get()
				&& sequence == wrapper.get();
	}

	@Override
	public byte[] createData()
	{
		int len = null == busiData ? 0 : busiData.length;
		byte[] data = new byte[HEAD_LEN + len];
		ByteBuffer wrapper = ByteBuffer.wrap(data);
		wrapper.put(moduleId);
		wrapper.put(commondId);
		wrapper.put(sequence);
		wrapper.putLong(userId);
		wrapper.put(clientType);
		wrapper.put(status);
		if (len > 0)
		{
			wrapper.put(busiData);
		}
		return data;
	}

	@Override
	public void parseData(byte[] receivedData)
	{
		if (null == receivedData || receivedData.length < HEAD_LEN)
		{
			return;
		}

		ByteBuffer buffer = ByteBuffer.wrap(receivedData);
		moduleId = buffer.get();
		commondId = buffer.get();
		sequence = buffer.get();
		userId = buffer.getLong();
		clientType = buffer.get();
		status = buffer.get();
		if (receivedData.length > HEAD_LEN)
		{
			busiData = new byte[receivedData.length - HEAD_LEN];
			buffer.get(busiData);
		} else
		{
			busiData = null;
		}
	}

	@Override
	public String toString()
	{
		return "BusMessage [moduleId=" + moduleId + ", commondId=" + commondId
				+ ", userId=" + userId + ", clientType=" + clientType
				+ ", sequence=" + sequence + ", status=" + status
				+ ", busiData=" + new String(busiData) + "]";
	}
}
