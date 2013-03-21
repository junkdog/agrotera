# artemis-lombok

Artemis-specific annotation(s) for lombok-pg.

Relies on [artemis-odb](https://github.com/junkdog/artemis-odb) to function properly.

Currently very much a WIP.

## Example:
```java
@ArtemisConfiguration(
    requires={ThrusterPush.class, TimeDilationFuel.class},
    optional=Velocity.class,
    managers=TagManager.class,
    systems=BroadcasterSystem.class)
public final class TimeDilationSystem extends EntityProcessingSystem
```
