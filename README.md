# artemis-lombok

Artemis-specific annotation(s) for lombok-pg.

Relies on [artemis-odb](https://github.com/junkdog/artemis-odb) to function properly.

Currently very much a WIP.

## Installation
__eclipse.ini__
```
# where lombok.jar refers to lombok-pg.
-javaagent:lombok.jar
-Xbootclasspath/a:lombok.jar
-Xbootclasspath/a:artemis-lombok.jar
```

## Example:
```java
@ArtemisConfiguration(
    requires={ThrusterPush.class, TimeDilationFuel.class},
    optional=Velocity.class,
    excludes=Dummy.class,
    managers=TagManager.class,
    systems=BroadcasterSystem.class)
public final class TimeDilationSystem extends EntityProcessingSystem
```

## Missing features:
- Injecting the Aspect inside the constructor.
- Prepending to existing _initialize()_ under ECJ.
- Add support for Managers.
- Separate profiling annotation for injecting logging/profiling into _begin()_ and _end()_.

## What works
- Injecting referenced systems, managers and components as fields.
