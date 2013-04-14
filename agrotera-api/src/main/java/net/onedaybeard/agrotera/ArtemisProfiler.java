package net.onedaybeard.agrotera;

public interface ArtemisProfiler
{
	void start();
	void stop();
	void setTag(String tag);
	void setTag(Class<?> tag);
}
