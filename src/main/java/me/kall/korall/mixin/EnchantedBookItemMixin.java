package me.kall.korall.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.kall.korall.event.ItemEnchantEvent;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantedBookItem.class)
public abstract class EnchantedBookItemMixin {
    @WrapMethod(method = "addEnchantment")
    private static void onEnchant(ItemStack stack, EnchantmentInstance instance, Operation<Void> original) {
        ItemEnchantEvent event = new ItemEnchantEvent(stack, instance.enchantment, instance.level, true);
        boolean cancel = MinecraftForge.EVENT_BUS.post(event);
        if (!cancel) original.call(stack, new EnchantmentInstance(event.getEnchantment(), event.getLevel()));
    }
}
