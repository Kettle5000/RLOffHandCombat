package cinnamon.ofc.mixin;

import cinnamon.ofc.HandPlatform;
import cinnamon.ofc.RLOffHandCombatMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(Minecraft.class)
public abstract class ClickMixin {

    @Shadow
    public LocalPlayer player;
    @Shadow
    public HitResult hitResult;
    @Shadow
    public MultiPlayerGameMode gameMode;

    @Inject(method = "startUseItem", at = @At(value = "HEAD"), cancellable = true)
    public void startUseItem(CallbackInfo ci) {
        RLOffHandCombatMod.LOGGER.info("CLICKMIXIN");
        if (!this.player.isHandsBusy() && !this.player.isCrouching() && HandPlatform.canUseOffhand(player) && HandPlatform.canSwingHand(this.player, InteractionHand.OFF_HAND)) {
            RLOffHandCombatMod.Data data = RLOffHandCombatMod.get(this.player);
            if (data.missTime <= 0 && this.hitResult != null) {
                switch (this.hitResult.getType()) {
                    case ENTITY:
                        data.doOverride = true;
                        RLOffHandCombatMod.LOGGER.debug("ATTEMPTING OFFHAND SWING?");
                        Entity target = ((EntityHitResult) this.hitResult).getEntity();

                        // Perform off-hand attack
                        boolean isCritical = isCriticalHit();
                        boolean isSweeping = isSweepingAttack();

                        // Apply critical damage and particles
                        if (isCritical) {
                            DamageSources damageSources = this.player.level().damageSources(); // Get DamageSources instance
                            target.hurt(damageSources.playerAttack(this.player), 1.5F); // Apply critical hit damage
                            applyCriticalHitParticles(target);
                        }

                        // Apply sweeping attack
                        if (isSweeping) {
                            performSweepingAttack(target);
                        }

                        // Regular attack
                        this.gameMode.attack(this.player, target);
                        this.player.swing(InteractionHand.OFF_HAND);

                        // Cancel default key behavior
                        Minecraft.getInstance().options.keyUse.release();
                        break;

                    case BLOCK:
                        return;

                    case MISS:
                        ItemStack stack = this.player.getMainHandItem();
                        UseAnim useAnimation = stack.getUseAnimation();
                        if (useAnimation != UseAnim.NONE) {
                            return;
                        }

                        if (Objects.requireNonNull(this.gameMode).hasMissTime()) {
                            data.missTime = 10;
                        }
                        this.player.swing(InteractionHand.OFF_HAND);
                        Minecraft.getInstance().options.keyUse.release();
                        break;
                }
                ci.cancel();
            }
        }
    }

    /**
     * Checks if the off-hand attack should be a critical hit.
     */
    private boolean isCriticalHit() {
        return !this.player.onGround() // Player must not be on the ground
                && !this.player.isInWater() // Player must not be in water
                && !this.player.isSprinting() // Player must not be sprinting
                && this.player.getDeltaMovement().y < 0.0 // Player must be falling
                && this.player.getAttackStrengthScale(0.5F) >= 1.0F; // Attack strength must be charged
    }

    /**
     * Checks if the off-hand attack should trigger a sweeping attack.
     */
    private boolean isSweepingAttack() {
        ItemStack offHandStack = this.player.getOffhandItem();
        if (!(offHandStack.getItem() instanceof SwordItem)) {
            return false;
        }
        return this.player.getAttackStrengthScale(0.5F) >= 1.0F && this.player.onGround();
    }

    /**
     * Performs a sweeping attack around the player, closely following vanilla behavior.
     */
    /**
     * Performs a sweeping attack around the player, closely following vanilla behavior.
     */
    private void performSweepingAttack(Entity target) {
        float sweepingDamage = 1.0F + EnchantmentHelper.getSweepingDamageRatio(this.player) * this.player.getAttackStrengthScale(0.5F);

        // Get all nearby LivingEntities within the sweeping range
        List<LivingEntity> nearbyEntities = this.player.level().getEntitiesOfClass(LivingEntity.class,
                this.player.getItemInHand(InteractionHand.OFF_HAND).getSweepHitBox(this.player, target));

        for (LivingEntity livingEntity : nearbyEntities) {
            // Ensure entity meets the criteria for a sweeping attack
            if (livingEntity != this.player
                    && livingEntity != target
                    && !this.player.isAlliedTo(livingEntity)
                    && (!(livingEntity instanceof ArmorStand armorStand) || !armorStand.isMarker())
                    && this.player.distanceToSqr(livingEntity) < Mth.square(this.player.getEntityReach())) {

                // Send attack packet to server for the swept entity
                Minecraft.getInstance().getConnection().send(ServerboundInteractPacket.createAttackPacket(livingEntity, this.player.isShiftKeyDown()));

                double dx = livingEntity.getX() - this.player.getX();
                double dz = livingEntity.getZ() - this.player.getZ();
                livingEntity.knockback(0.4F, dx, dz);
            }
        }

        // Play the sweeping attack sound effect
        this.player.level().playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, this.player.getSoundSource(), 1.0F, 1.0F);

        // Trigger sweeping particle effect
        this.sweepAttackParticles();
    }

    /**
     * Spawns sweeping attack particles in front of the player.
     */
    private void sweepAttackParticles() {
        double particleX = this.player.getX() + -Math.sin(Math.toRadians(this.player.getYRot())) * 1.0D;
        double particleZ = this.player.getZ() + Math.cos(Math.toRadians(this.player.getYRot())) * 1.0D;
        double particleY = this.player.getY() + 1.0D;

        this.player.level().addParticle(ParticleTypes.SWEEP_ATTACK, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
    }

    /**
     * Applies critical hit particles and effects with velocity in random directions.
     */
    private void applyCriticalHitParticles(Entity target) {
        double centerX = target.getX();
        double centerY = target.getY() + target.getBbHeight() / 2.0;
        double centerZ = target.getZ();

        for (int i = 0; i < 25; i++) { // Render particles for critical hits. around 25 seems to be good.
            double offsetX = (this.player.getRandom().nextDouble() - 0.5); // Small random X offset
            double offsetY = (this.player.getRandom().nextDouble() - 0.4) * 0.4; // Small random Y offset
            double offsetZ = (this.player.getRandom().nextDouble() - 0.5); // Small random Z offset

            double velocityX = (this.player.getRandom().nextDouble() - 0.4) * 1.5; // Random X velocity
            double velocityY = (this.player.getRandom().nextDouble() - 0.4) * 2;         // Random upward velocity
            double velocityZ = (this.player.getRandom().nextDouble() - 0.4) * 1.5; // Random Z velocity

            this.player.level().addParticle(ParticleTypes.CRIT,
                    centerX + offsetX,
                    centerY + offsetY,
                    centerZ + offsetZ,
                    velocityX,
                    velocityY,
                    velocityZ);
        }
    }
}
