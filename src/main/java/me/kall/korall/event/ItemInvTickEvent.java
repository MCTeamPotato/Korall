package me.kall.korall.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.fml.LogicalSide;

@Cancelable
public class ItemInvTickEvent extends TickEvent {
    private final Level level;
    private final Entity entity;
    private final int invSlot;
    private final boolean isCurrentItem;
    private final ItemStack stack;

    public ItemInvTickEvent(Level level, Entity entity, int invSlot, boolean isCurrentItem, ItemStack stack, LogicalSide side) {
        super(Type.PLAYER, side, Phase.START);
        this.level = level;
        this.entity = entity;
        this.invSlot = invSlot;
        this.isCurrentItem = isCurrentItem;
        this.stack = stack;
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
}
