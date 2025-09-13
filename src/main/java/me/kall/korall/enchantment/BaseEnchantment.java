package me.kall.korall.enchantment;

import me.kall.korall.Korall;
import me.kall.korall.api.InvTickable;
import me.kall.korall.event.ItemEnchantEvent;
import me.kall.korall.event.ItemInvTickEvent;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BaseEnchantment extends Enchantment {
    public BaseEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] applicableSlots) {
        super(rarity, category, applicableSlots);
    }

    public abstract boolean isDisabled();

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && !isDisabled();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return super.canEnchant(stack) && !isDisabled();
    }

    @Override
    public boolean isTradeable() {
        return super.isTradeable() && !isDisabled();
    }

    @Override
    public boolean isDiscoverable() {
        return super.isDiscoverable() && !isDisabled();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) && !isDisabled();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return super.isAllowedOnBooks() && !isDisabled();
    }

    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        this.checkAndRemove(attacker);
        super.doPostAttack(attacker, target, level);
    }

    @Override
    public void doPostHurt(LivingEntity target, Entity attacker, int level) {
        this.checkAndRemove(target);
        super.doPostHurt(target, attacker, level);
    }

    private void checkAndRemove(LivingEntity entity) {
        if (isDisabled()) {
            for (ItemStack stack : this.getSlotItems(entity).values()) {
                if (removeEnchantment(stack, this)) {
                    Korall.LOGGER.info("Removed disabled enchantment {} from {} in {}'s inventory", this.getDescriptionId(), stack.getHoverName().getString(), entity.getName().getString());
                }
            }
        }
    }

    public static boolean removeEnchantment(@NotNull ItemStack stack, Enchantment enchantment) {
        boolean removed = false;
        if (stack.isEmpty()) return removed;

        ResourceLocation target = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (target == null) return removed;

        CompoundTag tag = stack.getTag();
        if (tag == null) return removed;

        String tagKey = stack.getItem().equals(Items.ENCHANTED_BOOK) ? EnchantedBookItem.TAG_STORED_ENCHANTMENTS : ItemStack.TAG_ENCH;

        ListTag enchantments = tag.getList(tagKey, Tag.TAG_COMPOUND);
        removed = enchantments.removeIf(entry -> {
            if (!(entry instanceof CompoundTag enchantmentTag)) return false;
            ResourceLocation id = ResourceLocation.tryParse(enchantmentTag.getString("id"));
            return id != null && id.equals(target);
        });
        if (enchantments.isEmpty()) tag.remove(tagKey);
        if (tag.isEmpty()) stack.setTag(null);
        return removed;
    }

    public static boolean canUseAsWeapon(ItemStack stack) {
        boolean hasAttackDamage = stack.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, stack).containsKey(Attributes.ATTACK_DAMAGE);
        if (!hasAttackDamage) hasAttackDamage = stack.getItem().getAttributeModifiers(EquipmentSlot.OFFHAND, stack).containsKey(Attributes.ATTACK_DAMAGE);
        return hasAttackDamage;
    }

    public static int getLevel(ItemStack stack, Enchantment enchantment) {
        return stack.getEnchantmentLevel(enchantment);
    }

    public static int getLevelForSlot(LivingEntity entity, Enchantment enchantment, EquipmentSlot equipmentSlot) {
        return getLevel(entity.getItemBySlot(equipmentSlot), enchantment);
    }

    public static int getLevelForHands(LivingEntity entity, Enchantment enchantment) {
        return Math.max(getLevelForSlot(entity, enchantment, EquipmentSlot.MAINHAND), getLevelForSlot(entity, enchantment, EquipmentSlot.OFFHAND));
    }

    public static int getLevelForHead(LivingEntity entity, Enchantment enchantment) {
        return getLevelForSlot(entity, enchantment, EquipmentSlot.HEAD);
    }

    public static int getLevelForChest(LivingEntity entity, Enchantment enchantment) {
        return getLevelForSlot(entity, enchantment, EquipmentSlot.CHEST);
    }

    public static int getLevelForLegs(LivingEntity entity, Enchantment enchantment) {
        return getLevelForSlot(entity, enchantment, EquipmentSlot.LEGS);
    }

    public static int getLevelForFeet(LivingEntity entity, Enchantment enchantment) {
        return getLevelForSlot(entity, enchantment, EquipmentSlot.FEET);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Korall.MOD_ID)
    public static final class Events {
        @SubscribeEvent
        public static void onInvTick(ItemInvTickEvent event) {
            ItemStack stack = event.getStack();
            if (InvTickable.cast(stack).inv$ticked()) return;
            if (stack.isEmpty()) return;
            if (stack.getCount() != 1) return;
            if (!stack.hasTag()) return;
            if (!stack.isEnchanted()) return;

            InvTickable.cast(stack).inv$setTicked(true);

            ListTag enchantments = stack.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(stack) : stack.getEnchantmentTags();

            for (int i = 0; i < enchantments.size(); i++) {
                CompoundTag tag = enchantments.getCompound(i);
                if (ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(tag.getString("id"))) instanceof BaseEnchantment enchantment && enchantment.isDisabled()) {
                    if (BaseEnchantment.removeEnchantment(stack, enchantment)) {
                        Korall.LOGGER.info("Removed disabled enchantment {} from {} in {}'s inventory.", enchantment.getDescriptionId(), stack.getHoverName().getString(), event.getEntity().getName().getString());
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onEnchant(ItemEnchantEvent event) {
            if (event.getEnchantment() instanceof BaseEnchantment baseEnchantment && baseEnchantment.isDisabled()) {
                event.setCanceled(true);
            }
        }
    }
}
