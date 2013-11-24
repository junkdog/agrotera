package net.onedaybeard.agrotera;

import static net.onedaybeard.agrotera.EntityUtil.addEntity;
import net.onedaybeard.agrotera.component.ComponentA;
import net.onedaybeard.agrotera.component.ComponentB;
import net.onedaybeard.agrotera.manager.SomeManager;
import net.onedaybeard.agrotera.system.BasicSystem;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.artemis.Entity;
import com.artemis.World;

public class PojoInjectedTest {
	
	private World world;
	private InjectedType injected;
	private Entity entity;

	@Before @SuppressWarnings("unchecked")
	public void init() {
		world = new World();
		world.setManager(new SomeManager());
		world.setSystem(new BasicSystem());
		world.initialize();
		
		entity = addEntity(world, ComponentA.class, ComponentB.class);
		
		world.process();
		
		injected = new InjectedType();
		injected.initialize(world);
	}
	
	@Test
	public void mapper_for_a() {
		Assert.assertNotNull(injected.getComponentA(entity));
	}
	
	@Test
	public void mapper_for_b() {
		Assert.assertNotNull(injected.getComponentB(entity));
	}
	
	@Test
	public void system_injected() {
		Assert.assertNotNull(injected.getSystem());
	}
	
	@Test
	public void manager_injected() {
		Assert.assertNotNull(injected.getManager());
	}

}
