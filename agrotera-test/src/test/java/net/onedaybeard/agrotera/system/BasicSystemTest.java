package net.onedaybeard.agrotera.system;

import static net.onedaybeard.agrotera.EntityUtil.addEntity;
import static org.junit.Assert.assertEquals;
import net.onedaybeard.agrotera.component.ComponentA;
import net.onedaybeard.agrotera.component.ComponentB;

import org.junit.Before;
import org.junit.Test;

import com.artemis.World;

@SuppressWarnings("unchecked")
public class BasicSystemTest {
	private World world;
	private BasicSystem es;

	@Before
	public void init() {
		world = new World();
		es = world.setSystem(new BasicSystem());
		world.initialize();
		
		addEntity(world, ComponentA.class, ComponentB.class);
		addEntity(world, ComponentA.class);
		
		world.process();
	}
	
	@Test
	public void mappers_are_injected() {
		es.assertAMapper();
	}
	
	@Test
	public void exclude_is_working() {
		assertEquals(1, es.getActives().size());
	}
}
