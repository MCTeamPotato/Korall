package me.kall.korall.api;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface InvTickable {
    boolean inv$ticked();
    void inv$setTicked(boolean ticked);

    static InvTickable cast(ItemStack stack) {
        return (InvTickable) (Object) stack;
    }
}
