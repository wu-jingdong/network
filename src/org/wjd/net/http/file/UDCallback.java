package org.wjd.net.http.file;

public interface UDCallback
{

	public static final int SUCCESS = 0;

	public static final int FAIL = 1;

	public void callback(String matchIndicator, int result);
}
