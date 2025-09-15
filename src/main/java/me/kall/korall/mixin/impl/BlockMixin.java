package me.kall.korall.mixin.impl;

import me.kall.korall.api.Registry;
import me.kall.korall.api.Trackable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public abstract class BlockMixin implements Registry, Trackable {
    @Unique private ResourceLocation provider$registryName = null;
    @Unique private boolean block$isTracked, worldGen$accepted;

    @Override
    public ResourceLocation provider$getRegistryName() {
        if (provider$registryName == null) provider$registryName = ForgeRegistries.BLOCKS.getKey((Block) (Object)this);
        return this.provider$registryName;
    }

    @Override
    public boolean trackable$isTracked() {
        return this.block$isTracked;
    }

    @Override
    public void trackable$setTracked(boolean tracked) {
        this.block$isTracked = tracked;
    }

    @Override
    public boolean worldGen$accepted() {
        return this.worldGen$accepted;
    }

    @Override
    public void worldGen$setAccepted(boolean accepted) {
        this.worldGen$accepted = accepted;
    }
}
