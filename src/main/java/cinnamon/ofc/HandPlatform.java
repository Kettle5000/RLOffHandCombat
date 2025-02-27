package cinnamon.ofc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.Map;

public class HandPlatform {

    public static boolean canUseOffhand(Entity entity) {
        return entity instanceof Player;
    }

    public static void attack(Player player, Entity targetEntity) {
        RLOffHandCombatMod.get(player).doOverride = false;

        ItemStack offhand = player.getOffhandItem();
        ItemStack mainHand = player.getMainHandItem();
        RLOffHandCombatMod.Data data = RLOffHandCombatMod.get(player);
        int ticksSinceLastSwingOff = data.attackStrengthTicker;
        int ticksSinceLastSwingMain = player.attackStrengthTicker;

        //Switch items
        setItemStackToSlot(player, EquipmentSlot.MAINHAND, offhand);
        setItemStackToSlot(player, EquipmentSlot.OFFHAND, mainHand);
        makeActive(player, offhand, mainHand);

        //Swing 
        player.attackStrengthTicker = ticksSinceLastSwingOff;
        player.attack(targetEntity);
        player.attackStrengthTicker = ticksSinceLastSwingMain;

        //Reset Swing to half on main hand and full on off hand
        data.attackStrengthTicker = 0;
        if (canSwingHand(player, InteractionHand.MAIN_HAND)) {
            int halfTick = (int) (Config.cooldown * player.getCurrentItemAttackStrengthDelay());
            if (ticksSinceLastSwingMain > halfTick) {
                player.attackStrengthTicker = halfTick;
            }
        }

        setItemStackToSlot(player, EquipmentSlot.OFFHAND, offhand);
        setItemStackToSlot(player, EquipmentSlot.MAINHAND, mainHand);
        makeInactive(player, offhand, mainHand);
    }

    public static void resetAttackStrengthTickerMainHand(Player player) {
        if (canSwingHand(player, InteractionHand.OFF_HAND)) {
            RLOffHandCombatMod.Data data = RLOffHandCombatMod.get(player);
            int ticksSinceLastSwingOff = data.attackStrengthTicker;
            ItemStack offhand = player.getOffhandItem();
            ItemStack mainHand = player.getMainHandItem();

            HandPlatform.makeActive(player, offhand, mainHand);
            int halfTick = (int) (Config.cooldown * player.getCurrentItemAttackStrengthDelay());
            HandPlatform.makeInactive(player, offhand, mainHand);

            if (ticksSinceLastSwingOff > halfTick) {
                data.attackStrengthTicker = halfTick;
            }
        }
    }

    public static void resetAttackStrengthTickerOffHand(Player player) {
        RLOffHandCombatMod.Data data = RLOffHandCombatMod.get(player);
        data.attackStrengthTicker = 0;
        if (canSwingHand(player, InteractionHand.MAIN_HAND)) {
            int halfTick = (int) (Config.cooldown * player.getCurrentItemAttackStrengthDelay());
            if (player.attackStrengthTicker > halfTick) {
                player.attackStrengthTicker = halfTick;
            }
        }
    }

    public static boolean canSwingHand(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean flag1 = !Config.items_blacklist.contains(stack.getItem());
        Map<Enchantment, Integer> enchList = stack.getAllEnchantments();
        boolean flag2 = true;
        for (Enchantment ench: Config.enchantments_blacklist) {
        	if (enchList.containsKey(ench)) {
        		flag2= false;
        		break;
        	}
        }
        boolean flag3 = stack.getAttributeModifiers(
                hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND
        ).containsKey(Attributes.ATTACK_DAMAGE) || stack.getAttributeModifiers(
                EquipmentSlot.MAINHAND
        ).containsKey(Attributes.ATTACK_DAMAGE);
        return flag1 && flag2 && flag3;
    }

    public static void makeActive(Player playerIn, ItemStack offhand, ItemStack mainHand) {
        playerIn.getAttributes().removeAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        playerIn.getAttributes().removeAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlot.OFFHAND));
        playerIn.getAttributes().addTransientAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        playerIn.getAttributes().addTransientAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.OFFHAND));
    }

    public static void makeInactive(Player playerIn, ItemStack offhand, ItemStack mainHand) {
        playerIn.getAttributes().removeAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.OFFHAND));
        playerIn.getAttributes().removeAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        playerIn.getAttributes().addTransientAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        playerIn.getAttributes().addTransientAttributeModifiers(offhand.getAttributeModifiers(EquipmentSlot.OFFHAND));
    }

    public static void setItemStackToSlot(Player playerIn, EquipmentSlot slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlot.MAINHAND) {
            playerIn.getInventory().items.set(playerIn.getInventory().selected, stack);
        } else if (slotIn == EquipmentSlot.OFFHAND) {
            playerIn.getInventory().offhand.set(0, stack);
        }
    }
}
