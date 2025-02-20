package cinnamon.ofc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.utils.Log;

import java.io.*;
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
        String directory = HandPlatform.getConfigDirectory().toAbsolutePath().normalize().toString();
        RLOffHandCombatMod.makeConfig(directory);
        RLOffHandCombatMod.readConfig(directory);
    }

    public static void makeConfig(String location) {
        File file = new File(location + "/ofc.json");
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                if (newFile) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("attackTimeoutAfterSwing", new JsonPrimitive(Config.Runtime.attackTimeoutAfterSwing));

                    ObjectMapper objectMapper = ObjectMapper.create();
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(objectMapper.writeValueAsString(jsonObject));
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void readConfig(String location) {
        File file = new File(location + "/ofc.json");
        if (file.exists()) {
            try {
                JsonObject jsonObject = (JsonObject) JsonParser.parseReader(new FileReader(file));
                JsonElement attackTimeoutAfterSwing = jsonObject.get("attackTimeoutAfterSwing");
                Config.Runtime.attackTimeoutAfterSwing = attackTimeoutAfterSwing.getAsDouble();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
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
