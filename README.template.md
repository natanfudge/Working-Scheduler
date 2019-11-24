# Working Scheduler
[![CurseForge](https://curse.nikky.moe/api/img/340964?logo)](https://curseforge.com/minecraft/mc-mods/working-scheduler)
[![Discord](https://img.shields.io/discord/219787567262859264?color=blue&label=Discord)](https://discord.gg/CFaCu97)
[![Bintray](https://api.bintray.com/packages/natanfudge/libs/working-scheduler/images/download.svg)](https://bintray.com/beta/#/natanfudge/libs/working-scheduler?tab=overview)
[![Latest Commit](https://img.shields.io/github/last-commit/natanfudge/working-scheduler)](https://github.com/natanfudge/working-scheduler/commits/master)

Working Scheduler provides a working, fast, and comprehensive alternative to Minecraft's scheduler for Fabric mods.

## Introduction

A scheduler simply provides a way to execute an operation after a certain amount of time has passed. 
In Minecraft that time is measured in ticks.
### What is the problem with the vanilla Minecraft scheduler?
- It's slow: the more operations you schedule, the worse the tick overhead becomes.
- It doesn't work properly: Minecraft tries to save the state of your scheduled actions when the world unloads, but fails miserably. 
As soon as the world will load again all scheduled actions will execute immediately, with no respect to the amount of time they have left.
- It's limited: In Minecraft you can't cancel a scheduled action, you can't schedule in the client, only the server,
you can't have it repeated multiple times, and you can't pass any state into the scheduled action other than a single BlockPos. 
This also makes it extremely difficult to schedule multiple different actions on a singular block. 
### What about Working Scheduler?
- It's fast: the tick overhead is constant no matter how many actions you schedule. 
- It works: your state will be saved properly.
- It's comprehensive: You can can cancel schedules, you can schedule from the client, you can repeat the same action multiple times, 
you can pass arbitrary NBT data to the schedule action, and you can optionally pass a "schedule ID" which allows you to easily
differentiate between different scheduled actions.  

## Gradle setup
Add `jcenter()` to your repositories if you haven't yet:
```groovy
repositories {
    // [...]
    jcenter()
}
```
Add the mod dependency:
```groovy
dependencies {
    modImplementation("com.lettuce.fudge:working-scheduler:$total_version")
}
```
## Usage
The first thing you need to do is implement `Scheduleable` on a block of your choosing. 
Generally this should be the same block will execute the schedule, but it can be whatever block you want.
[Why do we need a Block?](https://github.com/natanfudge/Working-Scheduler#why-do-we-need-a-block-and-the-future)
```java
public class ExampleScheduleableBlock extends Block implements Scheduleable {
    @Override
    public void onScheduleEnd(World world, BlockPos pos, int scheduleId, CompoundTag additionalData) {
        System.out.println("X amount of ticks have passed!");
    }
}
```
As an example we will schedule things when the player has right-clicked the block.
To schedule an action we have separate idiomatic Java and Kotlin apis.
 (Note: Java API is usable from Kotlin as well but using the Kotlin-specific one is recommended)
#### Java
```java
public class ExampleScheduleableBlock extends Block implements Scheduleable {
    public void onScheduleEnd(World world, BlockPos pos, int scheduleId, CompoundTag additionalData) {/*...*/}

    @Override
    public boolean activate(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        // Schedule the print statement action to occur after 30 ticks
        Scheduler.Builder(this, world).schedule(30);
        
        // Repeat the action 4 times with a 10 ticks interval
        Scheduler.Builder(this, world).repeat(4,10);
            
        // Repeat the action for 100 ticks with a 15 ticks interval
        Scheduler.Builder(this, world).repeatFor(100,15);
        
        return true;
    }
}
```

#### Kotlin
```kotlin
class ExampleScheduleableBlock(settings: Block.Settings) : Block(settings), Scheduleable {
    override fun onScheduleEnd(world: World, pos: BlockPos, scheduleId: Int, additionalData: CompoundTag) {/*...*/ }

    override fun activate(blockState: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hitResult: BlockHitResult): Boolean {
        // Schedule the print statement action to occur after 30 ticks
        BlockScheduler.schedule(ticksUntilEnd = 30, block = this, world = world)

        // Repeat the action 4 times with a 10 ticks interval
        BlockScheduler.repeat(repeatAmount = 4, tickInterval = 10, block = this, world = world)
        
        // Repeat the action for 100 ticks with a 15 ticks interval
        BlockScheduler.repeatFor(ticksUntilStop = 100, tickInterval = 15, block = this, world = world)

        return true
    }
}
```

Full example projects can be seen [here](https://github.com/natanfudge/Working-Scheduler/tree/master/example/src/main).

### Attaching additional data
Provide the data while scheduling:
#### Java
```java
public boolean activate(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
    if(player == null) return false;
    CompoundTag scheduleData = new CompoundTag();
    scheduleData.putUuid("player", player.getUuid());

    Scheduler.Builder(this, world)
                .scheduleId(1)
                .pos(pos)
                .additionalData(scheduleData)
                .schedule(100);
    
    Scheduler.Builder(this, world)
                .scheduleId(2)
                .pos(pos)
                .additionalData(scheduleData)
                .repeat(4,20);
    
    return true;
}
```

#### Kotlin
```kotlin
override fun activate(
        blockState: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity?,
        hand: Hand?,
        hitResult: BlockHitResult?
    ): Boolean {
    val scheduleData = CompoundTag().apply { putUuid("player", player?.uuid ?: UUID(0, 0)) }

    BlockScheduler.schedule(
        ticksUntilEnd = 100,
        block = this,
        scheduleId = 1,
        world = world,
        blockPos = pos,
        additionalData = scheduleData
    )

    BlockScheduler.repeat(
        repeatAmount = 4,
        tickInterval = 20,
        block = this,
        scheduleId = 2,
        world = world,
        blockPos = pos,
        additionalData = scheduleData
    )

    return true
}
```

And then use the data when the schedule ends:
```java
public class ExampleScheduleableBlock extends Block implements Scheduleable {
    @Override
    public void onScheduleEnd(World world, BlockPos pos, int scheduleId, CompoundTag additionalData) {
        // Note: you should validate that the player exists and the additionalData was not tampered with.
        // No validation is done for the sake of simplicity.
        PlayerEntity player = world.getPlayerByUuid(additionalData.getUuid("player"));
        if(scheduleId == 1){
            player.sendMessage(new LiteralText("Normal schedule ended at pos " + pos));
        }else if(scheduleId == 2){
            player.sendMessage(new LiteralText("Repeating schedule ended at pos " + pos));
        }
    }
}
```
Notice how we use the `scheduleId` to differentiate between different schedule calls! 

### Cancelling
Whenever you call a schedule, you will receive a `CancellationToken` instance. Simply call `cancel` on the same `world` to cancel:
```java
public class ExampleScheduleableBlock extends Block implements Scheduleable {
    public void onScheduleEnd(World world, BlockPos pos, int scheduleId, CompoundTag additionalData) {/*...*/}

    CancellationToken cancellationToken;

    // First this happens...
    @Override
    public boolean activate(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
       cancellationToken = Scheduler.Builder(this, world).schedule(30);
       
       return true;
    }

    // And then later on...
    @Override
    public void onBlockRemoved(BlockState beforeState, World world, BlockPos pos, BlockState afterState, boolean bool) {
        // The scheduled action won't occur!
       if(cancellationToken != null) cancellationToken.cancel(world);
    }

}
```

Note that usually you want your `CancellationToken` to persist between world loads,
in which case you can store it in a block entity for example.
A `CancellationTokenSerializer` exists specifically to make this easier using [Drawer](https://github.com/natanfudge/Fabric-Drawer).

## Dependencies
Working Scheduler depends on [Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin) and [Drawer](https://www.curseforge.com/minecraft/mc-mods/fabric-drawer).
If you don't want to depend on them yourself, you can include them in your mod like so:
```groovy
include("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")
include("com.lettuce.fudge:fabric-drawer:$drawer_version")
```

## Why do we need a block, and the future
Right now Scheduler requires you to implement an interface on a Block of yours. 
This is because the Block is already instantiated by yourself which helps avoid reflection. 
Additionally, scheduling is most commonly done from blocks.
Theoretically, an API like that looks like this could exist:
```kotlin
schedule(world,10) {
    println("10 ticks have passed.")
}
```
But that would require either reflection or a compiler plugin. If you think this is a good idea, feel free to make an issue.