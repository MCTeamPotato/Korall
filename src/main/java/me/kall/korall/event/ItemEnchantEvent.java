package me.kall.korall.event;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ItemEnchantEvent extends Event implements ICancellableEvent {
    private final ItemStack itemStack;
    private Holder<Enchantment> enchantment;
    private int level;
    private final boolean isEnchantedBook;

    public ItemEnchantEvent(ItemStack itemStack, Holder<Enchantment> enchantment, int level, boolean isEnchantedBook) {
        this.itemStack = itemStack;
        this.enchantment = enchantment;
        this.level = level;
        this.isEnchantedBook = isEnchantedBook;
    }

    public boolean isEnchantedBook() {
        return this.isEnchantedBook;
    }

    public Holder<Enchantment> getEnchantment() {
        return this.enchantment;
    }

    public int getLevel() {
        return this.level;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setEnchantment(Holder<Enchantment> enchantment) {
        this.enchantment = enchantment;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
