package me.kall.korall.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;

public class ItemInvTickEvent extends Event implements ICancellableEvent {
    private final Level level;
    private final Entity entity;
    private final int invSlot;
    private final boolean isCurrentItem;
    private final ItemStack stack;
    private final LogicalSide side;

    public ItemInvTickEvent(Level level, Entity entity, int invSlot, boolean isCurrentItem, ItemStack stack, LogicalSide side) {
        this.level = level;
        this.entity = entity;
        this.invSlot = invSlot;
        this.isCurrentItem = isCurrentItem;
        this.stack = stack;
        this.side = side;
    }

    public Level getLevel() {
        return level;
    }

    public Entity getEntity() {
        return entity;
    }

    public int getInvSlot() {
        return invSlot;
    }

    public boolean isCurrentItem() {
        return isCurrentItem;
    }

    public ItemStack getStack() {
        return stack;
    }

    public LogicalSide getSide() {
        return side;
    }
}
