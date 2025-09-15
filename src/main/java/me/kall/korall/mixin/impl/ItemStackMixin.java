package me.kall.korall.mixin.impl;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.kall.korall.api.InvTickable;
import me.kall.korall.event.ItemEnchantEvent;
import me.kall.korall.event.ItemInvTickEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements InvTickable {
    @Unique private boolean inv$ticked;

    @WrapMethod(method = "inventoryTick")
    private void onTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, Operation<Void> original) {
        ItemInvTickEvent event = new ItemInvTickEvent(level, entity, inventorySlot, isCurrentItem, (ItemStack) (Object) this, level.isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER);
        boolean cancel = MinecraftForge.EVENT_BUS.post(event);
        if (!cancel) original.call(level, entity, inventorySlot, isCurrentItem);
    }

    @WrapMethod(method = "enchant")
    private void onEnchant(Enchantment enchantment, int level, Operation<Void> original) {
        ItemEnchantEvent event = new ItemEnchantEvent((ItemStack) (Object) this, enchantment, level, false);
        boolean cancel = MinecraftForge.EVENT_BUS.post(event);
        if (!cancel) original.call(event.getEnchantment(), event.getLevel());
    }

    public boolean inv$ticked() {
        return this.inv$ticked;
    }

    public void inv$setTicked(boolean ticked) {
        this.inv$ticked = ticked;
    }
}
