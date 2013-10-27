package org.wjd.net.http.conn;

/**
 * 响应回调
 * 
 * @author wjd
 * 
 */
public interface ResponseCallback
{
	public void callback(long timestamp, byte[] resposne);
}
