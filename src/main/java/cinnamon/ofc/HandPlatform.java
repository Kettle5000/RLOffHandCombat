package cinnamon.ofc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.WeakHashMap;

public class HandPlatform {

    private static WeakHashMap<Player, InteractionHand> AttackMap = new WeakHashMap<>();

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

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
        //here is the issue, the method attack uses the native AttackStrength instead of a custom one, this can be fixed by using a mixin to pass the hand used here.
        //Since this line is called here and you should not be able to attack twice in the same instant, hence causing instantiation loss, you can add this to a weakhashmap
        //taking the player and the hand used, that way other mods can retrieve the hand used in the attack method and do whatever they want.
        AttackMap.put(player, InteractionHand.OFF_HAND);
        player.attack(targetEntity);
        player.attackStrengthTicker = ticksSinceLastSwingMain;

        //Reset Swing to half on main hand and full on off hand
        data.attackStrengthTicker = 0;
        if (canSwingHand(player, InteractionHand.MAIN_HAND)) {
            int halfTick = (int) (Config.Runtime.attackTimeoutAfterSwing * player.getCurrentItemAttackStrengthDelay());
            if (ticksSinceLastSwingMain > halfTick) {
                player.attackStrengthTicker = halfTick;
            }
        }

        //Switch back items, and we reset the hand used in the map
        AttackMap.put(player, InteractionHand.MAIN_HAND);
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
            int halfTick = (int) (Config.Runtime.attackTimeoutAfterSwing * player.getCurrentItemAttackStrengthDelay());
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
            int halfTick = (int) (Config.Runtime.attackTimeoutAfterSwing * player.getCurrentItemAttackStrengthDelay());
            if (player.attackStrengthTicker > halfTick) {
                player.attackStrengthTicker = halfTick;
            }
        }
    }

    public static boolean canSwingHand(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return stack.getAttributeModifiers(
                hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND
        ).containsKey(Attributes.ATTACK_DAMAGE) || stack.getAttributeModifiers(
                EquipmentSlot.MAINHAND
        ).containsKey(Attributes.ATTACK_DAMAGE);
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

    public static InteractionHand getAttackHand(Player player) {
        InteractionHand hand = AttackMap.get(player);
        if (hand != null) {
            return hand;
        }
        //Assuming kind of to avoid null checks but logically if it is not set to the offhand then it must be the mainhand
        return InteractionHand.MAIN_HAND;
    }
}
