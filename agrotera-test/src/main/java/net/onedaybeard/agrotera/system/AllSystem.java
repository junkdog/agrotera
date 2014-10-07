package net.onedaybeard.agrotera.system;

import net.onedaybeard.agrotera.annotations.ArtemisSystem;

import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

@ArtemisSystem
public class AllSystem extends EntityProcessingSystem {

	public AllSystem() {
		super(null);
	}

	@Override
	protected void process(Entity e) {

	}
}
