package net.onedaybeard.agrotera;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

final class ThreadPoolUtil
{
	private ThreadPoolUtil() {}
	
	static void awaitTermination(ExecutorService threadPool)
	{
		threadPool.shutdown();
		try
		{
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}

