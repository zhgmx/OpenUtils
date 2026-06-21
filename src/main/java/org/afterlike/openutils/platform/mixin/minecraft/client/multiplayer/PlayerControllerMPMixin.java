package org.afterlike.openutils.platform.mixin.minecraft.client.multiplayer;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.event.impl.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControllerMP.class)
public class PlayerControllerMPMixin {
	@Inject(method = "attackEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;syncCurrentPlayItem()V"))
	private void ou$attackEntity(final EntityPlayer playerIn, final Entity targetEntity,
			final CallbackInfo ci) {
		OpenUtils.get().getEventBus().post(new AttackEntityEvent(playerIn, targetEntity));
	}
}
