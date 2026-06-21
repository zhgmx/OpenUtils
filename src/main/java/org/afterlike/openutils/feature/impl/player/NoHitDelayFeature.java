package org.afterlike.openutils.feature.impl.player;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.platform.mixin.minecraft.client.MinecraftAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class NoHitDelayFeature extends ToggleableFeature {
	@Option(name = "Enable No Hit Delay",
			description = "Clear Minecraft's left-click cooldown every tick.", order = 0)
	public boolean enabled;
	public NoHitDelayFeature() {
		super("No Hit Delay", FeatureCategory.PLAYER);
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		MinecraftAccessor accessor = (MinecraftAccessor) mc;
		accessor.ou$setLeftClickCounter(0);
	}
}
