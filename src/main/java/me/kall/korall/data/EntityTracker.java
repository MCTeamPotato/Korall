package me.kall.korall.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.kall.korall.event.EntityChunkChangeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EntityTracker {
    public static final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Set<UUID>>> ENTITIES = new Object2ObjectOpenHashMap<>();
    public static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    public static @NotNull @UnmodifiableView Set<UUID> getEntities(@NotNull ServerLevel level, @NotNull ChunkPos chunkPos) {
        return Collections.unmodifiableSet(ENTITIES.getOrDefault(level.dimension().location(), Long2ObjectMaps.emptyMap()).getOrDefault(chunkPos.toLong(), Collections.emptySet()));
    }

    public static void removeEntity(@NotNull Entity entity, @NotNull ServerLevel level) {
        final long chunkPos = entity.chunkPosition().toLong();
        final ResourceLocation dim = level.dimension().location();
        final var chunkMap = ENTITIES.get(dim);
        if (chunkMap == null) return;
        final var entities = chunkMap.get(chunkPos);
        if (entities == null) return;
        final UUID uuid = entity.getUUID();
        TASKS.add(() -> {
            entities.remove(uuid);
            if (!entities.isEmpty()) return;
            chunkMap.remove(chunkPos);
            if (!chunkMap.isEmpty()) return;
            ENTITIES.remove(dim);
        });
    }

    public static void addEntity(@NotNull Entity entity, @NotNull ServerLevel level) {
        final long chunkPos = entity.chunkPosition().toLong();
        final ResourceLocation dim = level.dimension().location();
        final UUID uuid = entity.getUUID();
        TASKS.add(() -> ENTITIES.computeIfAbsent(dim, key -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(chunkPos, key -> new ObjectOpenHashSet<>()).add(uuid));
    }

    public static void register() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EntityTracker::onJoin);
        bus.addListener(EntityTracker::onLeave);
        bus.addListener(EntityTracker::onUpdatePre);
        bus.addListener(EntityTracker::onUpdatePost);
        bus.addListener(EntityTracker::onTick);
    }

    private static void onJoin(@NotNull EntityJoinLevelEvent event) {
        if (event.isCanceled()) return;
        Entity entity = event.getEntity();
        if (event.getLevel() instanceof ServerLevel level) {
            addEntity(entity, level);
        }
    }

    private static void onLeave(@NotNull EntityLeaveLevelEvent event) {
        if (event.isCanceled()) return;
        Entity entity = event.getEntity();
        if (event.getLevel() instanceof ServerLevel level) {
            removeEntity(entity, level);
        }
    }

    private static void onUpdatePre(EntityChunkChangeEvent.@NotNull Pre event) {
        Entity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            removeEntity(entity, level);
        }
    }

    private static void onUpdatePost(EntityChunkChangeEvent.@NotNull Post event) {
        Entity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            addEntity(entity, level);
        }
    }

    private static void onTick(TickEvent.@NotNull ServerTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            event.getServer().execute(() -> {
                Runnable task;
                while ((task = EntityTracker.TASKS.poll()) != null) task.run();
            });
        }
    }
}
