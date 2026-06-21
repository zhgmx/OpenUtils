package org.afterlike.openutils.platform.mixin.minecraft.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.handler.FeatureHandler;
import org.afterlike.openutils.feature.impl.render.AnimationsFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class EntityPlayerMixin {
	@Unique private boolean ou$shouldForceBlock() {
		EntityPlayer self = (EntityPlayer) (Object) this;
		if (!(self instanceof EntityPlayerSP))
			return false;
		FeatureHandler handler = OpenUtils.get().getFeatureHandler();
		AnimationsFeature module = handler.getFeature(AnimationsFeature.class);
		if (!handler.isEnabled(AnimationsFeature.class))
			return false;
		if (!module.forceBlockAnimation)
			return false;
		ItemStack held = self.getHeldItem();
		if (held == null || !(held.getItem() instanceof ItemSword))
			return false;
		if (module.requireMouseDown) {
			return Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown();
		}
		return self.isSwingInProgress;
	}

	@Inject(method = "isBlocking", at = @At("RETURN"), cancellable = true)
	private void ou$forceBlocking(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && ou$shouldForceBlock()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getItemInUseCount", at = @At("RETURN"), cancellable = true)
	private void ou$forceUseCount(CallbackInfoReturnable<Integer> cir) {
		if (cir.getReturnValue() <= 0 && ou$shouldForceBlock()) {
			cir.setReturnValue(10);
		}
	}
}
