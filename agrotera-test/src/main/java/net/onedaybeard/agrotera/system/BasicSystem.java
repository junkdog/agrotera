package net.onedaybeard.agrotera.system;

import static org.junit.Assert.assertNotNull;
import net.onedaybeard.agrotera.annotations.ArtemisSystem;
import net.onedaybeard.agrotera.component.ComponentA;
import net.onedaybeard.agrotera.component.ComponentB;

import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

@ArtemisSystem(
	requires=ComponentA.class,
	excludes=ComponentB.class)
public class BasicSystem extends EntityProcessingSystem {

	public BasicSystem() {
		super(null);
	}

	@Override
	protected void process(Entity e) {

	}
	
	public void assertAMapper() {
		assertNotNull(componentAMapper);
	}
}
