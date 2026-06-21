package org.afterlike.openutils.platform.mixin.minecraft.client.renderer;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.EntityPlayer;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.render.FreeLookFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ActiveRenderInfo.class)
public class ActiveRenderInfoMixin {
	@Redirect(method = "updateRenderInfo",
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/entity/player/EntityPlayer;rotationPitch:F",
					opcode = Opcodes.GETFIELD))
	private static float modifyPitch(final EntityPlayer instance) {
		return OpenUtils.get().getFeatureHandler().isEnabled(FreeLookFeature.class)
				? OpenUtils.get().getFeatureHandler().getFeature(FreeLookFeature.class)
						.getCameraPitch()
				: instance.rotationPitch;
	}

	@Redirect(method = "updateRenderInfo",
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/entity/player/EntityPlayer;rotationYaw:F",
					opcode = Opcodes.GETFIELD))
	private static float modifyYaw(final EntityPlayer instance) {
		return OpenUtils.get().getFeatureHandler().isEnabled(FreeLookFeature.class)
				? OpenUtils.get().getFeatureHandler().getFeature(FreeLookFeature.class)
						.getCameraYaw()
				: instance.rotationYaw;
	}
}
