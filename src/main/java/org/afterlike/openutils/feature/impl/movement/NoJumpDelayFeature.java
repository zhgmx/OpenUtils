package org.afterlike.openutils.feature.impl.movement;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.platform.mixin.minecraft.entity.EntityLivingBaseAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class NoJumpDelayFeature extends ToggleableFeature {
	@Option(name = "Enable No Jump Delay",
			description = "Remove Minecraft's short cooldown between repeated jumps.", order = 0)
	public boolean enabled;
	@Option(name = "Only while moving",
			description = "Only remove jump delay while horizontal movement input is active.",
			order = 1)
	public boolean onlyWhileMoving;
	public NoJumpDelayFeature() {
		super("No Jump Delay", FeatureCategory.MOVEMENT);
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		if (onlyWhileMoving) {
			if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0)
				return;
		}
		EntityLivingBaseAccessor accessor = (EntityLivingBaseAccessor) mc.thePlayer;
		accessor.ou$setJumpTicks(0);
	}
}
