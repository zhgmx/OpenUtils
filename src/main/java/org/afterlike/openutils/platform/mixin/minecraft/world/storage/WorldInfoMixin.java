package org.afterlike.openutils.platform.mixin.minecraft.world.storage;

import net.minecraft.world.storage.WorldInfo;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.world.TimeChangerFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldInfo.class)
public class WorldInfoMixin {
	@Shadow
	private long worldTime;
	@Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
	private void ou$getWorldTime(final CallbackInfoReturnable<Long> cir) {
		if (!OpenUtils.get().getFeatureHandler().isEnabled(TimeChangerFeature.class))
			return;
		final TimeChangerFeature module = OpenUtils.get().getFeatureHandler()
				.getFeature(TimeChangerFeature.class);
		cir.setReturnValue(module.timeToTicks());
	}
}
