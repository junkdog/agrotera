package net.onedaybeard.agrotera;

import org.junit.Assert;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;

public final class EntityUtil {
	private EntityUtil() {}
	
	public static Entity addEntity(World w, Class<? extends Component>... components) {
		Entity e = w.createEntity();
		for (Class<? extends Component> c : components) {
			try {
				e.addComponent(c.newInstance());
			}
			catch (InstantiationException e1) {
				Assert.fail();
			}
			catch (IllegalAccessException e1) {
				Assert.fail();
			}
		}
	
		e.addToWorld();
		
		return e;
	}
}
