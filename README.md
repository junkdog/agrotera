# Agrotera

Anti-boilerplate strategies for [Artemis Entity System Framework](http://gamadu.com/artemis/).
Injects systems with referenced component mappers, systems and managers
during compilation/post-compilation.


## Installation (wip: builder, maven...)
__eclipse.ini__
```
# where lombok.jar refers to lombok-pg.
-javaagent:lombok.jar
-Xbootclasspath/a:lombok.jar
-Xbootclasspath/a:agrotera-lombok.jar
```

## Minimal example
###What you type:
```java
@ArtemisConfiguration(
    requires={Renderable.class, Velocity.class},
	excludes={Cullable.class, AssetReference.class},
	managers={TagManager.class, GroupManager.class},
	systems=VelocitySystem.class)
public class TestSystem extends IntervalEntityProcessingSystem
{
	@SuppressWarnings("unchecked")
	public TestSystem()
	{
		super(null, 0.05f);
	}
	
	@Override
	protected void process(Entity e)
	{
		// process system
	}
}
```
###What the JVM gets:
```java
@ArtemisConfiguration(
	requires={Renderable.class, Velocity.class},
	excludes={Cullable.class, AssetReference.class},
	managers={TagManager.class, GroupManager.class},
	systems=VelocitySystem.class)
public class TestSystem extends IntervalEntityProcessingSystem
{
	private ComponentMapper renderableMapper;
	private ComponentMapper velocityMapper;
	private VelocitySystem velocitySystem;
	private TagManager tagManager;
	private GroupManager groupManager;
	
	@SuppressWarnings("unchecked")
	public TestSystem()
	{
		super(Aspect.getAspectForAll(Renderable.class, Velocity.class)
			.exclude(Cullable.class, AssetReference.class), 0.05f);
	}
	
	@Override
	protected void initialize()
	{
		renderableMapper = world.getMapper(Renderable.class);
		velocityMapper = world.getMapper(Velocity.class);
		tagManager = world.getManager(TagManager.class);
		groupManager = world.getManager(GroupManager.class);
		velocitySystem = world.getSystem(VelocitySystem.class);
	}
	
	@Override
	protected void process(Entity e)
	{
		// process system
	}
}
```

- If `initialize` is defined, the injection code is prepended to the method.
- In the constructor, the Aspect is inferred if it previously was `null`.

# Behind the veil
Agrotera consists of two intermingling parts.

### agrotera-lombok
Responsible for declaring the fields, inferred from `@ArtemisConfiguration`,
ensuring that the IDE doesn't complain about unresolved fields.

Contains`@ArtemisConfiguration`, processed alongside [lombok-pg](https://github.com/peichhorn/lombok-pg)
(you know, that fork of [Project Lombok](http://projectlombok.org/) - because
I couldn't get type resolution working under vanilla lombok).


### agrotera-asm
Responsible for modifying the classes; wiring up the systems. Conceived as a
post-compile step run with an eclipse builder, exec-maven-plugin or similar.

## Missing/planned features
- Inject conditional profiling calls into `EntitySystem#begin` and
  `EntitySystem#end`
- Add support for Managers: inject fields, only trigger on requested entities.
- Easier maven and IDE integration.


## Contact
junkdog at onedaybeard dot net - twitter: [@junkdogap]()
