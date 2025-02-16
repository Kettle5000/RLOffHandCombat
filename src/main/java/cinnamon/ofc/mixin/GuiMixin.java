package cinnamon.ofc.mixin;

import cinnamon.ofc.HandPlatform;
import cinnamon.ofc.RLOffHandCombatMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int screenHeight;
    @Shadow
    private int screenWidth;

    @Inject(method = "renderHotbar", at = @At(target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F", value = "INVOKE_ASSIGN", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void renderHotbar(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
        if (!HandPlatform.canUseOffhand(player)) return;
        if (!HandPlatform.canSwingHand(player, InteractionHand.OFF_HAND)) return;

        if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            float g = this.getAttackStrengthScale(player);
            if (g < 1.0F) {
                int k2 = this.screenHeight - 20;
                int l2 = humanoidArm == HumanoidArm.RIGHT ? i + 91 + 6 : i - 91 - 52;

                guiGraphics.blit(Gui.GUI_ICONS_LOCATION, l2, k2, 0, 94, 18, 18);
                int i2 = (int) (g * 19.0F);
                guiGraphics.blit(Gui.GUI_ICONS_LOCATION, l2, k2 + 18 - i2, 18, 112 - i2, 18, i2);
            }
        }
    }

    @Inject(method = "renderCrosshair", at = @At(target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F", value = "INVOKE_ASSIGN", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (!HandPlatform.canUseOffhand(player)) return;
        if (!HandPlatform.canSwingHand(player, InteractionHand.OFF_HAND)) return;

        if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
            float g = this.getAttackStrengthScale(player);
            boolean flag = false;
            if (this.minecraft.crosshairPickEntity instanceof LivingEntity livingEntity && g >= 1.0F) {
                ItemStack offhand = player.getOffhandItem();
                ItemStack mainHand = player.getMainHandItem();
                HandPlatform.makeActive(player, offhand, mainHand);
                flag = player.getCurrentItemAttackStrengthDelay() > 5.0F;
                HandPlatform.makeInactive(player, offhand, mainHand);
                flag &= livingEntity.isAlive();
            }

            int j = this.screenHeight / 2 - 7 + 22;
            int k = this.screenWidth / 2 - 8;
            if (flag) {
                guiGraphics.blit(Gui.GUI_ICONS_LOCATION, k, j, 68, 94, 16, 16);
            } else if (g < 1.0F) {
                int l = (int) (g * 17.0F);
                guiGraphics.blit(Gui.GUI_ICONS_LOCATION, k, j, 36, 94, 16, 4);
                guiGraphics.blit(Gui.GUI_ICONS_LOCATION, k, j, 52, 94, l, 4);
            }
        }
    }

    private float getAttackStrengthScale(Player player) {
        ItemStack offhand = player.getOffhandItem();
        ItemStack mainHand = player.getMainHandItem();
        HandPlatform.makeActive(player, offhand, mainHand);
        RLOffHandCombatMod.Data data = RLOffHandCombatMod.get(player);
        float f = Mth.clamp(((float) data.attackStrengthTicker) / player.getCurrentItemAttackStrengthDelay(), 0.0f, 1.0f);
        HandPlatform.makeInactive(player, offhand, mainHand);
        return f;
    }
}
