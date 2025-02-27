package cinnamon.ofc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.config.ModConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(RLOffHandCombatMod.MOD_ID)
public class RLOffHandCombatMod {   // changed name since it conflicted with net.minecraftforge.fml.common.Mod

    public static final String MOD_ID = "offhandcombat";   // maybe we should change the name of this mod?
    public static final Logger LOGGER = LogManager.getLogger();

    public static Map<UUID, Data> swing = new HashMap<>();
    public static Map<UUID, Data> swingLocal = new HashMap<>();

    public RLOffHandCombatMod() {
    	MinecraftForge.EVENT_BUS.register(this);
	IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "RLOffHandCombat.toml");
    }
	
    public static Data get(Player entity) {
        if (entity.isLocalPlayer()) {
            if (!swingLocal.containsKey(entity.getUUID())) {
                swingLocal.put(entity.getUUID(), new Data());
            }
            return swingLocal.get(entity.getUUID());
        } else {
            if (!swing.containsKey(entity.getUUID())) {
                swing.put(entity.getUUID(), new Data());
            }
            return swing.get(entity.getUUID());
        }
    }

    public static class Data {
        //
        public boolean doOverride;
        //
        public int missTime;
        //
        public int swingTime;
        public boolean swinging;
        public float attackAnim;
        public float attackAnim_;
        public int attackStrengthTicker;
        public InteractionHand swingingArm;
        //
        public int ticksSinceLastActiveStack;
        public InteractionHand handOfLastActiveStack;
    }

}
