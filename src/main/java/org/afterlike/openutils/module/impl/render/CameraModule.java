package org.afterlike.openutils.module.impl.render;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.setting.impl.DescriptionSetting;
import org.afterlike.openutils.module.api.setting.impl.NumberSetting;
import org.afterlike.openutils.platform.mixin.minecraft.client.renderer.EntityRendererAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class CameraModule extends Module {
	private final DescriptionSetting description1;
	private final NumberSetting distance;
	private final DescriptionSetting description2;
	private final NumberSetting shakeMultiplier;
	public CameraModule() {
		super("Camera", ModuleCategory.RENDER);
		description1 = this.registerSetting(new DescriptionSetting("Vanilla camera distance is 4"));
		distance = this.registerSetting(new NumberSetting("Camera Distance", 4, 1, 40, 1));
		description2 = this.registerSetting(new DescriptionSetting("Vanilla multiplier is 14"));
		shakeMultiplier = this
				.registerSetting(new NumberSetting("Hurt Shake Multiplier", 0, -40, 40, 1));
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		EntityRendererAccessor accessor = (EntityRendererAccessor) mc.entityRenderer;
		accessor.ou$setThirdPersonDistance(distance.getFloat());
	}

	@Override
	protected void onDisable() {
		EntityRendererAccessor accessor = (EntityRendererAccessor) mc.entityRenderer;
		accessor.ou$setThirdPersonDistance(4f);
	}
}