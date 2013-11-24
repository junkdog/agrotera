package net.onedaybeard.agrotera.manager;

import lombok.ArtemisManager;
import net.onedaybeard.agrotera.component.ComponentA;
import net.onedaybeard.agrotera.component.ComponentB;
import net.onedaybeard.agrotera.system.BasicSystem;

import com.artemis.Manager;

@ArtemisManager(
	requires={ComponentA.class, ComponentB.class},
	systems=BasicSystem.class,
	managers=SomeManager.class)
public class SomeManager extends Manager {
	@Override public void initialize() {}
}
