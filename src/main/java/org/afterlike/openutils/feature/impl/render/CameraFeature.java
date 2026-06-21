package org.afterlike.openutils.feature.impl.render;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.platform.mixin.minecraft.client.renderer.EntityRendererAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.fastbus.Subscribe;

public class CameraFeature extends ToggleableFeature {
	@Option(name = "Enable Camera",
			description = "Apply custom third-person distance and hurt-shake behavior.", order = 0)
	public boolean enabled;
	@Option(name = "Camera distance", description = "Third-person camera distance. Vanilla is 4.",
			order = 1)
	@Range(min = 1.0D, max = 40.0D, step = 1.0D)
	public float distance = 4.0F;
	@Option(name = "Hurt shake multiplier",
			description = "Scales hurt-camera shake; use 0 to remove it.", order = 2)
	@Range(min = -40.0D, max = 40.0D, step = 1.0D)
	public float shakeMultiplier;
	public CameraFeature() {
		super("Camera", FeatureCategory.RENDER);
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		EntityRendererAccessor accessor = (EntityRendererAccessor) mc.entityRenderer;
		accessor.ou$setThirdPersonDistance(distance);
	}

	@Override
	protected void onDisable() {
		EntityRendererAccessor accessor = (EntityRendererAccessor) mc.entityRenderer;
		accessor.ou$setThirdPersonDistance(4f);
	}
}
