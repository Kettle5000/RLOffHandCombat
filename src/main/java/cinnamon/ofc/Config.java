package cinnamon.ofc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;


@Mod.EventBusSubscriber(modid = RLOffHandCombatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue COOLDOWN = BUILDER
            .comment("Percentage of cooldown applied to the opposite hand after an attack is made")
            .defineInRange("AttackCooldown", 0.5d, 0.0d, 1.0d);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BANNEDITEMS_LIST;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BANNEDENCHANTMENTS_LIST;
    
    static {
        // Default list of enchantments
        List<String> defaultEnchantments = new ArrayList<>(List.of());
        List<String> defaultItems = new ArrayList<>(List.of());
        // Check if the other mod is loaded
        if (ModList.get().isLoaded("jlme")) {
            // Add additional enchantments if the mod is present
            defaultEnchantments.addAll(List.of("jlme:critical_strike"));
        }
        if (ModList.get().isLoaded("fn")) {
            // Add additional items if the mod is present
            defaultItems.addAll(List.of(
            		"fn:wood_nunchakus",
            		"fn:stone_nunchakus",
            		"fn:copper_nunchakus",
            		"fn:gold_nunchakus",
            		"fn:iron_nunchakus",
            		"fn:silver_nunchakus",
            		"fn:desertchitin_nunchakus",
            		"fn:junglechitin_nunchakus",
            		"fn:dragonbone_nunchakus",
            		"fn:firedragonbone_nunchakus",
            		"fn:icedragonbone_nunchakus",
            		"fn:lightningdragonbone_nunchakus"
            		));
        }
        BANNEDITEMS_LIST = BUILDER
                .comment("List of Items that are banned to work with offhand combat, meant to avoid buggy interactions with other mods")
                .defineListAllowEmpty("Banned Items", defaultItems, Config::validateItems);
        BANNEDENCHANTMENTS_LIST = BUILDER
                .comment("List of enchantments that will cause offhand to not work")
                .defineListAllowEmpty("Banned Enchantments", defaultEnchantments, Config::validateEnchantments);
        
        BUILDER.build();
    }


    static final ForgeConfigSpec SPEC = BUILDER.build();

 
    public static float cooldown;
    public static Set<Item> items_blacklist;
    public static Set<Enchantment> enchantments_blacklist;
    
    private static boolean validateItems(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ENCHANTMENTS.containsKey(new ResourceLocation(itemName));
    }
    
    private static boolean validateEnchantments(final Object obj)
    {
        return obj instanceof final String enchName && ForgeRegistries.ENCHANTMENTS.containsKey(new ResourceLocation(enchName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        cooldown = (float)(COOLDOWN.get() * 1f);
        items_blacklist = BANNEDITEMS_LIST.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
        enchantments_blacklist = BANNEDENCHANTMENTS_LIST.get().stream()
                .map(itemName -> ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
        
    }
}
