package me.kall.korall.mixin.impl;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.kall.korall.event.ItemEnchantEvent;
import me.kall.korall.event.ItemInvTickEvent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapMethod(method = "inventoryTick")
    private void onTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, Operation<Void> original) {
        ItemInvTickEvent event = new ItemInvTickEvent(level, entity, inventorySlot, isCurrentItem, (ItemStack) (Object) this, level.isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) original.call(level, entity, inventorySlot, isCurrentItem);
    }

    @WrapMethod(method = "enchant")
    private void onEnchant(Holder<Enchantment> enchantment, int level, Operation<Void> original) {
        ItemEnchantEvent event = new ItemEnchantEvent((ItemStack) (Object) this, enchantment, level, false);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) original.call(event.getEnchantment(), event.getLevel());
    }
}
