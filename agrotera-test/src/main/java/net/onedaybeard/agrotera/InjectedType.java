package net.onedaybeard.agrotera;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

import lombok.ArtemisInjected;
import net.onedaybeard.agrotera.component.ComponentA;
import net.onedaybeard.agrotera.component.ComponentB;
import net.onedaybeard.agrotera.manager.SomeManager;
import net.onedaybeard.agrotera.system.BasicSystem;

@ArtemisInjected(
	mappers={ComponentA.class, ComponentB.class},
	managers=SomeManager.class,
	systems=BasicSystem.class)
public class InjectedType {

	
	private ComponentMapper<ComponentA> mapper;
	private ComponentMapper<ComponentB> mapperB;

	public ComponentA getComponentA(Entity e) {
		return componentAMapper.get(e);
	}
	
	public ComponentB getComponentB(Entity e) {
		return componentBMapper.get(e);
	}
	
	public SomeManager getManager() {
		return someManager;
	}
	
	public BasicSystem getSystem() {
		return basicSystem;
	}
	
	public void initialize2(World world) {
		mapper = world.getMapper(ComponentA.class);
		mapperB = world.getMapper(ComponentB.class);
	}
}
