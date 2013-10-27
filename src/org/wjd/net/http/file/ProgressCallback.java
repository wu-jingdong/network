package org.wjd.net.http.file;

public interface ProgressCallback
{
	public void publishProgress(String matchIndicator, int progress);
}
