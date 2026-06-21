package org.afterlike.openutils.platform.mixin.minecraft.client.renderer;

import net.minecraft.client.renderer.RenderGlobal;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.world.TimeChangerFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderGlobal.class)
public class RenderGlobalMixin {
	@ModifyVariable(method = "renderSky*", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float ou$renderSky(float value) {
		if (!OpenUtils.get().getFeatureHandler().isEnabled(TimeChangerFeature.class))
			return value;
		return 1.0f;
	}
}
