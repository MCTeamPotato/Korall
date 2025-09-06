package me.kall.korall.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface Registry {
    ResourceLocation provider$getRegistryName();

    static Registry cast(Block block) {
        return (Registry) block;
    }

    static Registry cast(EntityType<?> entityType) {
        return (Registry) entityType;
    }

    static ResourceLocation getRegistryName(Block block) {
        return cast(block).provider$getRegistryName();
    }

    static ResourceLocation getRegistryName(@NotNull BlockState block) {
        return getRegistryName(block.getBlock());
    }

    static ResourceLocation getRegistryName(EntityType<?> entityType) {
        return cast(entityType).provider$getRegistryName();
    }

    static ResourceLocation getRegistryName(@NotNull Entity entity) {
        return getRegistryName(entity.getType());
    }
}
