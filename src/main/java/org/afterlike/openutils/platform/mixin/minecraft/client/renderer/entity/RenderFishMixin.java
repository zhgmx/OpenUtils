package org.afterlike.openutils.platform.mixin.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderFish;
import net.minecraft.entity.projectile.EntityFishHook;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.render.ThickRodsFeature;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderFish.class)
public class RenderFishMixin {
	@Inject(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/WorldRenderer;begin(ILnet/minecraft/client/renderer/vertex/VertexFormat;)V",
			ordinal = 1))
	private void ou$modifyLineThickness(final EntityFishHook entity, final double x, final double y,
			final double z, final float entityYaw, final float partialTicks,
			final CallbackInfo ci) {
		if (!OpenUtils.get().getFeatureHandler().isEnabled(ThickRodsFeature.class))
			return;
		GL11.glLineWidth(1.0f
				+ OpenUtils.get().getFeatureHandler().getFeature(ThickRodsFeature.class).thickness);
	}
}
