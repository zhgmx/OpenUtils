package org.afterlike.openutils.platform.mixin.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.render.FreeLookFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderManager.class)
public class RenderManagerMixin {
	@Redirect(method = "cacheActiveRenderInfo",
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewX:F",
					opcode = Opcodes.PUTFIELD))
	private void ou$cacheActiveRenderInfo$playerViewX(final RenderManager instance,
			final float value) {
		instance.playerViewX = OpenUtils.get().getFeatureHandler().isEnabled(FreeLookFeature.class)
				? OpenUtils.get().getFeatureHandler().getFeature(FreeLookFeature.class)
						.getCameraPitch()
				: value;
	}

	@Redirect(method = "cacheActiveRenderInfo",
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewY:F",
					opcode = Opcodes.PUTFIELD))
	private void ou$cacheActiveRenderInfo$playerViewY(final RenderManager instance,
			final float value) {
		instance.playerViewY = OpenUtils.get().getFeatureHandler().isEnabled(FreeLookFeature.class)
				? OpenUtils.get().getFeatureHandler().getFeature(FreeLookFeature.class)
						.getCameraYaw()
				: value;
	}
}
