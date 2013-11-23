package net.onedaybeard.agrotera;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;

public final class EntityUtil {
	private EntityUtil() {}
	
	public static Entity addEntity(World w, Class<? extends Component>... components) {
		Entity e = w.createEntity();
		for (Class<? extends Component> c : components)
			e.createComponent(c);
	
		e.addToWorld();
		
		return e;
	}
}
