package net.onedaybeard.agrotera;

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

}
