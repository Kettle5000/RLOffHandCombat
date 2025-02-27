package cinnamon.ofc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Fired when a player performs a sweeping attack.
 * This event is NOT cancelable  
 */
public class SweepingAttackEvent extends PlayerEvent {
    private final LivingEntity target;
    private float sweepingDamage;
    private AABB sweepArea;

    public SweepingAttackEvent(Player player, LivingEntity target, float sweepingDamage, AABB area) {
        super(player);
        this.target = target;
        this.sweepingDamage = sweepingDamage;
        this.sweepArea = area;
    }

    /**
     * Gets the main target of the attack.
     */
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Gets the sweeping attack damage.
     */
    public float getSweepingDamage() {
        return sweepingDamage;
    }
    
    public AABB getSweepingBox() {
    	return this.sweepArea;
    }
    
    public void setSweepingBox(AABB newArea) {
    	this.sweepArea = newArea;
    }

    /**
     * Sets the sweeping attack damage.
     */
    public void setSweepingDamage(float sweepingDamage) {
        this.sweepingDamage = sweepingDamage;
    }
}
