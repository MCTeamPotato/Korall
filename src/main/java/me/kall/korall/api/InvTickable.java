package me.kall.korall.api;

import net.minecraft.world.item.ItemStack;

public interface InvTickable {
    boolean inv$ticked();
    void inv$setTicked(boolean ticked);

    static InvTickable cast(ItemStack stack) {
        return (InvTickable) (Object) stack;
    }
}
