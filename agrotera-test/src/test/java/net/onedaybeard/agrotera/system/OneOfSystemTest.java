package net.onedaybeard.agrotera.system;

import static net.onedaybeard.agrotera.EntityUtil.addEntity;
import static org.junit.Assert.assertEquals;
import net.onedaybeard.agrotera.component.ComponentA;
import net.onedaybeard.agrotera.component.ComponentB;
import net.onedaybeard.agrotera.component.ComponentC;

import org.junit.Before;
import org.junit.Test;

import com.artemis.World;

@SuppressWarnings("unchecked")
public class OneOfSystemTest {
	private World world;
	private OneOfSystem es;

	@Before
	public void init() {
		world = new World();
		es = world.setSystem(new OneOfSystem());
		world.initialize();
		
		addEntity(world, ComponentB.class);
		addEntity(world, ComponentA.class, ComponentC.class);
		addEntity(world, ComponentA.class, ComponentB.class);
		addEntity(world, ComponentA.class, ComponentB.class, ComponentC.class);
		
		world.process();
	}
	
	@Test
	public void mappers_are_injected() {
		es.assertAMapper();
		es.assertBMapper();
		es.assertCMapper();
	}
	
	@Test
	public void expected_entity_count() {
		assertEquals(3, es.getActives().size());
	}
}
