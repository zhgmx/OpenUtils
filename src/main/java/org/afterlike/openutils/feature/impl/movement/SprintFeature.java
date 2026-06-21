package org.afterlike.openutils.feature.impl.movement;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.platform.mixin.minecraft.client.settings.KeyBindingAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class SprintFeature extends ToggleableFeature {
	@Option(name = "Enable Sprint",
			description = "Hold Minecraft's sprint input for you whenever movement allows it.",
			order = 0)
	public boolean enabled;
	public SprintFeature() {
		super("Sprint", FeatureCategory.MOVEMENT);
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		((KeyBindingAccessor) mc.gameSettings.keyBindSprint).ou$setPressed(true);
	}

	@Override
	public void onDisable() {
		if (!ClientUtil.notNull())
			return;
		((KeyBindingAccessor) mc.gameSettings.keyBindSprint).ou$setPressed(false);
	}
}
