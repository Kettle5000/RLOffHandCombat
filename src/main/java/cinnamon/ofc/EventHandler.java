package cinnamon.ofc;

import cinnamon.ofc.RLOffHandCombatMod.Data;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
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

    @SubscribeEvent
    public static void onSweepEvent(SweepingAttackEvent event) {
    	System.out.println("Event Original Area: " + event.getSweepingBox().getSize());
      	Player player = event.getEntity();
        LivingEntity target = event.getTarget();
        Vec3 start = player.position();
        double reach = player.getEntityReach();
        Vec3 direction = target.position().subtract(start).normalize();
        Vec3 end = start.add(direction.scale(reach));
        AABB sweepingBox = new AABB(start, end).inflate(1.5, 1.0, 1); 

        // Modify the sweeping area in the event
        event.setSweepingBox(sweepingBox);
        System.out.println("Event Hand Used: " + player.getMainHandItem());
        System.out.println("Event New Area: " + event.getSweepingBox().getSize());
    	System.out.println("Event Damage: " + event.getSweepingDamage());
    }
	
}
