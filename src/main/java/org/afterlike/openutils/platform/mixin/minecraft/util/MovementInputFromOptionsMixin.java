package org.afterlike.openutils.platform.mixin.minecraft.util;

import net.minecraft.util.MovementInputFromOptions;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.movement.NullMoveFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public abstract class MovementInputFromOptionsMixin {
	@Inject(method = "updatePlayerMoveState", at = @At("HEAD"))
	private void ou$applyNullMoveBeforeMovementInput(final CallbackInfo callbackInfo) {
		OpenUtils.get().getFeatureHandler().getFeature(NullMoveFeature.class)
				.applyBeforeMovementInput();
	}
}
