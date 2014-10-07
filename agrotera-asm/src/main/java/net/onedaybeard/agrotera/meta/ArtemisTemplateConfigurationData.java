package net.onedaybeard.agrotera.meta;

import java.util.ArrayList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.objectweb.asm.Type;

/**
 * Will contain all the data coming from the ArtemisTemplate annotation
 * @author GJ Roelofs info@codepoke.net
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class ArtemisTemplateConfigurationData {

	public ArrayList<Type> components = new ArrayList();
	public String name;
	public String description;

}
