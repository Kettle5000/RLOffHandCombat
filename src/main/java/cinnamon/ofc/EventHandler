package cinnamon.ofc;

import cinnamon.ofc.RLOffHandCombatMod.Data;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventHandler {
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) { 
    	if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer serverPlayer) {
    		Data data = RLOffHandCombatMod.get(serverPlayer);
            data.missTime--;
            data.attackStrengthTicker++;
            data.ticksSinceLastActiveStack++;
    	}
    }
	
}
