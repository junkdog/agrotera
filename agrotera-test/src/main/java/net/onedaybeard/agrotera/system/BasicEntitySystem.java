package net.onedaybeard.agrotera.system;

import lombok.ArtemisSystem;

import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;

@ArtemisSystem
public class BasicEntitySystem extends EntityProcessingSystem {

	public BasicEntitySystem() {
		super(null);
	}

	@Override
	protected void process(Entity e) {

	}
}
