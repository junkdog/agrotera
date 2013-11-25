# Agrotera

Zero-overhead anti-boilerplate strategies for [Artemis Entity System Framework][01].


## Features
- Compile-time class engineering: no reflection overhead, no extra classes, no additional
  runtime dependencies, works with Android.
- [@ArtemisSystem][11] for EntitySystems and [@ArtemisManager][12] for Managers.
  - Injects `Aspect` in constructor, unless already defined (only applies to EntitySystems).
  - Declares fields for referenced component mappers, managers and systems (only tested with Eclipse and maven).
  - Wires up referenced classes in `initialize()`, prepending to the method if already defined.
  - Generate *Component Dependency Matrices* via maven plugin ([example][14]).
- [@Profile][13] EntitySystems
  - Injects conditional profiler call at start of `begin()` and before any exit point in `end()`.
  - User-specified profiler class - adhering to [ArtemisProfiler][15].
- Inject abitrary classes with [@ArtemisInjected][16].
  - Creates an `initialize(World)` method on the annotated class.


## Installation


- [Preparing Eclipse IDE](http://github.com/junkdog/agrotera/wiki/Eclipse-IDE-Installation): 
  Add lombok-pg and agrotera-lombok to `eclipse.ini`.
- [Eclipse project setup](http://github.com/junkdog/agrotera/wiki/Eclipse-Project-Setup):
  Add lombok-pg to classpath and `agrotera-asm` as a project builder. _Not necessary_ when using Eclipse+Maven.
- [Maven project setup](http://github.com/junkdog/agrotera/wiki/Maven-Project-Setup):
  Configure `agrotera-maven-plugin`. Also applies to Eclipse+Maven.


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
@WovenByTheHuntress // marker annotation; don't process class when present
@Profile(using=Profiler.class, enabled=true)
@ArtemisSystem(
    requires={Renderable.class, Velocity.class},
	excludes={Cullable.class, AssetReference.class},
	managers={TagManager.class, GroupManager.class},
	systems=VelocitySystem.class)
public class TestSystem extends IntervalEntityProcessingSystem
{
	private final Profiler $profiler;
	private ComponentMapper<Renderable> renderableMapper;
	private ComponentMapper<Velocity> velocityMapper;
	private VelocitySystem velocitySystem;
	private TagManager tagManager;
	private GroupManager groupManager;

	public TestSystem()
	{
		super(Aspect.getAspectForAll(Renderable.class, Velocity.class)
			.exclude(Cullable.class, AssetReference.class), 0.05f);
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
	protected void process(Entity e)
	{
		// process system
	}
}
```

# Behind the veil
Agrotera consists of two intermingling parts.

### agrotera-lombok
Responsible for declaring the fields, inferred from annotations,
ensuring that the IDE doesn't complain about unresolved fields.

Provides `@Profiler`, `@ArtemisSystem` and `@ArtemisManager`, processed alongside
[lombok-pg][41] (you know, that fork of [Project Lombok][42] - because I couldn't
get type resolution working under vanilla lombok).


### agrotera-asm
Transforms the entity systems and managers; wiring up dependencies and
injecting profiler calls. Conceived as a post-compile step run via the
agrotera-maven-plugin or with an eclipse builder.

## Artemis Maven dependency / Shameless self-promotion
Our [fork][61] of Artemis:
```xml
<dependency>
    <groupId>net.onedaybeard.artemis</groupId>
    <artifactId>artemis-odb</artifactId>
    <version>0.5.0</version>
</dependency>
```

## Contact
junkdog at onedaybeard dot net - twitter: [@junkdogAP](http://twitter.com/junkdogAP)

 [01]: http://gamadu.com/artemis/
 [11]: https://github.com/junkdog/agrotera/wiki/@ArtemisSystem
 [12]: https://github.com/junkdog/agrotera/wiki/@ArtemisManager
 [13]: https://github.com/junkdog/agrotera/wiki/@Profile
 [14]: https://raw.github.com/wiki/junkdog/agrotera/images/matrix.png
 [15]: https://github.com/junkdog/agrotera/blob/master/agrotera-api/src/main/java/net/onedaybeard/agrotera/ArtemisProfiler.java
 [16]: https://github.com/junkdog/agrotera/wiki/@ArtemisInjected
 [41]: https://github.com/peichhorn/lombok-pg
 [42]: http://projectlombok.org/
 [61]: https://github.com/junkdog/artemis-odb
