package net.onedaybeard.agrotera;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface ClassWeaver
{
	void process(String file) throws FileNotFoundException, IOException;
}
