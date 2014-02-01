package net.onedaybeard.agrotera.system;

import static org.junit.Assert.assertNotNull;
import net.onedaybeard.agrotera.annotations.ArtemisSystem;
import net.onedaybeard.agrotera.component.ComponentA;
import net.onedaybeard.agrotera.component.ComponentB;
import net.onedaybeard.agrotera.component.ComponentC;

import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

@ArtemisSystem(
	requires=ComponentA.class,
	requiresOne={ComponentB.class, ComponentC.class}
)
public class OneOfSystem extends EntityProcessingSystem {

	public OneOfSystem() {
		super(null);
	}

	@Override
	protected void process(Entity e) {

	}

	public void assertAMapper() {
		assertNotNull("ComponentA (requires) not injected.", componentAMapper);
	}
	
	public void assertBMapper() {
		assertNotNull("ComponentB (requiresOne) not injected.", componentBMapper);
	}
	
	public void assertCMapper() {
		assertNotNull("ComponentC (requiresOne) not injected.", componentCMapper);
	}
}
