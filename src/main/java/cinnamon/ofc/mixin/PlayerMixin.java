package cinnamon.ofc.mixin;

import cinnamon.ofc.HandPlatform;
import cinnamon.ofc.SweepingAttackEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

@Mixin(Player.class)
public abstract class PlayerMixin extends Entity {
	@Unique
	private ItemStack lastItemInOffHand = ItemStack.EMPTY;

	public PlayerMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", shift = At.Shift.BEFORE))
	public void resetReEquipAttackStrengthTicker(CallbackInfo ci) {
		Player player = getThis();
		if (HandPlatform.canUseOffhand(player)) {
			ItemStack itemstack = player.getOffhandItem();
			if (!ItemStack.matches(this.lastItemInOffHand, itemstack)) {
				if (this.lastItemInOffHand.is(itemstack.getItem()) && ItemStack.isSameItemSameTags(this.lastItemInOffHand, itemstack) && HandPlatform.canSwingHand(player, InteractionHand.OFF_HAND)) {
					HandPlatform.resetAttackStrengthTickerOffHand(player);
				}
				this.lastItemInOffHand = itemstack.copy();
			}
		}
	}

	@ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), ordinal = 5)
	private float injectSweepEvent(float f3, Entity target, @Share("arg") LocalRef<SweepingAttackEvent> argRef) {
		Player player = getThis();
		AABB originalbox = player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, target);
		SweepingAttackEvent event = new SweepingAttackEvent((Player) (Object) this, (LivingEntity) target, f3, originalbox);
		argRef.set(event);
		MinecraftForge.EVENT_BUS.post(event);
		f3 = event.getSweepingDamage();
		return f3;
	}

	@ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;", ordinal = 0), index = 1)																																																					// 1)
	private AABB modifySweepHitbox(AABB originalHitbox, @Share("arg") LocalRef<SweepingAttackEvent> argRef) {
		// Get the original hitbox
		originalHitbox = argRef.get().getSweepingBox();
		return originalHitbox;
	}

	private Player getThis() {
		return (Player) (Object) this;
	}
}
