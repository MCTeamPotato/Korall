package me.kall.korall.mixin.impl;

import me.kall.korall.api.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements Registry {
    @Unique
    private ResourceLocation provider$registryName = null;

    @Override
    public ResourceLocation provider$getRegistryName() {
        if (provider$registryName == null) provider$registryName = BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>) (Object) this);
        return provider$registryName;
    }
}
