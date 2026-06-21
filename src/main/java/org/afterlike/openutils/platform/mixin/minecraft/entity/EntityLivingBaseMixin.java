package org.afterlike.openutils.platform.mixin.minecraft.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.render.AntiDebuffFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin {
	@Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"),
			cancellable = true)
	private void isPotionActive(final Potion potionIn, final CallbackInfoReturnable<Boolean> cir) {
		if (!OpenUtils.get().getFeatureHandler().isEnabled(AntiDebuffFeature.class))
			return;
		final AntiDebuffFeature feature = OpenUtils.get().getFeatureHandler()
				.getFeature(AntiDebuffFeature.class);
		if ((feature.nausea && potionIn == Potion.confusion)
				|| (feature.blindness && potionIn == Potion.blindness)) {
			cir.setReturnValue(false);
		}
	}
}
