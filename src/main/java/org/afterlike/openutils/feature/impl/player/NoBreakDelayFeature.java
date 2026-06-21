package org.afterlike.openutils.feature.impl.player;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.platform.mixin.minecraft.client.multiplayer.PlayerControllerMPAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.fastbus.Subscribe;

public class NoBreakDelayFeature extends ToggleableFeature {
	@Option(name = "Enable No Break Delay",
			description = "Limit the cooldown Minecraft applies after breaking a block.", order = 0)
	public boolean enabled;
	@Option(name = "Break delay",
			description = "Maximum block-hit cooldown to allow after each break.", order = 1)
	@Range(min = 0.0D, max = 5.0D, step = 1.0D)
	public int delay;
	public NoBreakDelayFeature() {
		super("No Break Delay", FeatureCategory.PLAYER);
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		PlayerControllerMPAccessor accessor = (PlayerControllerMPAccessor) mc.playerController;
		if (accessor.ou$getBlockHitDelay() > delay) {
			accessor.ou$setBlockHitDelay(delay);
		}
	}
}
