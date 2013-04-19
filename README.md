# Agrotera

Zero-overhead anti-boilerplate strategies for [Artemis Entity System Framework](http://gamadu.com/artemis/).

## Features
- Compile-time class engineering; no reflection overhead, no extra classes, works with Android.
- `@ArtemisSystem` for EntitySystems
  - Injects `Aspect` in constructor, unless already defined.
  - Declares fields for referenced component mappers, managers and systems (only tested with Eclipse and maven).
  - Wires up referenced classes in `initialize()`, prepending to the method if already defined.
- `@Profile` EntitySystems
  - Injects conditional profiler call at start of `begin()` and before any exit point in `end()`.
  - User-specified profiler class - adhering to [ArtemisProfiler](https://github.com/junkdog/agrotera/blob/master/agrotera-api/src/main/java/net/onedaybeard/agrotera/ArtemisProfiler.java).


## Installation (WIP...)
- [Preparing Eclipse IDE](EclipseIdeInstallation): adding lombok-pg and `agrotera-lombok` to `eclipse.ini`.
- [Eclipse project setup](EclipseProjectSetup): Add lombok-pg to classpath and `agrotera-asm` as a project builder.
- [Maven Project Setup](MavenProjectSetup): Configure `agrotera-maven-plugin`.

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
@Profile(using=Profiler.class, enabled=true)
@ArtemisSystem(
    requires={Renderable.class, Velocity.class},
	excludes={Cullable.class, AssetReference.class},
	managers={TagManager.class, GroupManager.class},
	systems=VelocitySystem.class)
public class TestSystem extends IntervalEntityProcessingSystem
{
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
@Profile(using=Profiler.class, enabled=true)
@ArtemisSystem(
    requires={Renderable.class, Velocity.class},
	excludes={Cullable.class, AssetReference.class},
	managers={TagManager.class, GroupManager.class},
	systems=VelocitySystem.class)
public class TestSystem extends IntervalEntityProcessingSystem
{
	private Profiler $profiler;
	private ComponentMapper<Renderable> renderableMapper;
	private ComponentMapper<Velocity> velocityMapper;
	private VelocitySystem velocitySystem;
	private TagManager tagManager;
	private GroupManager groupManager;

	public TestSystem()
	{
		super(Aspect.getAspectForAll(Renderable.class, Velocity.class)
			.exclude(Cullable.class, AssetReference.class), 0.05F);
		$profiler = new Profiler();
		$profiler.setTag(getClass());
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
	protected void begin()
	{
		$profiler.start();
	}

	@Override
	protected void end()
	{
		$profiler.stop();
	}

	@Override
	protected void process(Entity entity)
	{
		// process system
	}
}
```

# Behind the veil
Agrotera consists of two intermingling parts.

### agrotera-lombok
Responsible for declaring the fields, inferred from `@ArtemisSystem`,
ensuring that the IDE doesn't complain about unresolved fields.

Provides `@ArtemisSystem`, processed alongside [lombok-pg](https://github.com/peichhorn/lombok-pg)
(you know, that fork of [Project Lombok](http://projectlombok.org/) - because
I couldn't get type resolution working under vanilla lombok).


### agrotera-asm
Transforms the classes; wiring up the systems and injecting profiler calls.
Conceived as a post-compile step run with an eclipse builder,
exec-maven-plugin or similar.

## Missing/planned features
- Add support for Managers: inject fields, only trigger on requested entities.
- Easier maven and IDE integration.


## Contact
junkdog at onedaybeard dot net - twitter: [@junkdogap](http://twitter.com/junkdogAP)
