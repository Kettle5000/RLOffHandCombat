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
}
