package org.afterlike.openutils.platform.mixin.minecraft.gui;

import net.minecraft.client.gui.FontRenderer;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.render.AntiShuffleFeature;
import org.afterlike.openutils.feature.impl.render.NameHiderFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FontRenderer.class)
public class FontRendererMixin {
	@ModifyVariable(method = "renderString", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
	private String ou$renderString(String string) {
		if (string == null) {
			return null;
		}
		if (OpenUtils.get().getFeatureHandler().isEnabled(AntiShuffleFeature.class)
				&& string.contains("§k")) {
			string = string.replace("§k", "");
		}
		if (OpenUtils.get().getFeatureHandler().isEnabled(NameHiderFeature.class)) {
			string = OpenUtils.get().getFeatureHandler().getFeature(NameHiderFeature.class)
					.replaceName(string);
		}
		return string;
	}

	@ModifyVariable(method = "getStringWidth", at = @At(value = "HEAD"), ordinal = 0,
			argsOnly = true)
	private String ou$getStringWidth(String string) {
		if (string == null) {
			return null;
		}
		if (OpenUtils.get().getFeatureHandler().isEnabled(AntiShuffleFeature.class)
				&& string.contains("§k")) {
			string = string.replace("§k", "");
		}
		if (OpenUtils.get().getFeatureHandler().isEnabled(NameHiderFeature.class)) {
			string = OpenUtils.get().getFeatureHandler().getFeature(NameHiderFeature.class)
					.replaceName(string);
		}
		return string;
	}
}
