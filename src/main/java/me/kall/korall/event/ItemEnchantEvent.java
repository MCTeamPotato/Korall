package me.kall.korall.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ItemEnchantEvent extends Event {
    private final ItemStack itemStack;
    private Enchantment enchantment;
    private int level;
    private final boolean isEnchantedBook;

    public ItemEnchantEvent(ItemStack itemStack, Enchantment enchantment, int level, boolean isEnchantedBook) {
        this.itemStack = itemStack;
        this.enchantment = enchantment;
        this.level = level;
        this.isEnchantedBook = isEnchantedBook;
    }

    public boolean isEnchantedBook() {
        return this.isEnchantedBook;
    }

    public Enchantment getEnchantment() {
        return this.enchantment;
    }

    public int getLevel() {
        return this.level;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
