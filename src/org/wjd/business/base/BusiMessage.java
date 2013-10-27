package org.wjd.business.base;

import java.nio.ByteBuffer;

/**
 * 业务消息结构
 * 
 * @author wjd
 * 
 */
public class BusiMessage
{

	/**
	 * 消息透长度
	 */
	public static final int HEAD_LEN = 11;

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
	 * 业务数据
	 */
	private byte[] busiData;

	/**
	 * 默认构造方法
	 * 
	 * @param moduleId
	 * @param commondId
	 * @param userId
	 * @param clientType
	 * @param busiData
	 */
	public BusiMessage(byte moduleId, byte commondId, long userId,
			byte clientType, byte[] busiData)
	{
		super();
		this.moduleId = moduleId;
		this.commondId = commondId;
		this.userId = userId;
		this.clientType = clientType;
		this.busiData = busiData;
	}

	/**
	 * 封装消息
	 * 
	 * @return
	 */
	public byte[] createData()
	{
		byte[] data = new byte[HEAD_LEN + getBusiDataLength()];
		ByteBuffer wrapper = ByteBuffer.wrap(data);
		wrapper.put(moduleId);
		wrapper.put(commondId);
		wrapper.putLong(userId);
		wrapper.put(clientType);
		if (getBusiDataLength() > 0)
		{
			wrapper.put(busiData);
		}
		return data;
	}

	/**
	 * 解析消息
	 * 
	 * @param src
	 */
	public void parseData(byte[] src)
	{
		if (null == src || src.length < HEAD_LEN)
		{
			return;
		}
		ByteBuffer wrapper = ByteBuffer.wrap(src);
		moduleId = wrapper.get();
		commondId = wrapper.get();
		userId = wrapper.getLong();
		clientType = wrapper.get();
		if (src.length > HEAD_LEN)
		{
			busiData = new byte[src.length - HEAD_LEN];
			wrapper.get(busiData);
		}
	}

	private int getBusiDataLength()
	{
		return null == busiData ? 0 : busiData.length;
	}
}
