package org.afterlike.openutils.feature.impl.world;

import net.minecraft.client.Minecraft;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;

public class TimeChangerFeature extends ToggleableFeature {
	@Option(name = "Enable Time Changer",
			description = "Override the client-side world time shown in game.", order = 0)
	public boolean enabled;
	@Option(name = "Time", description = "Displayed world time in hours.", order = 1)
	@Range(min = 0.0D, max = 24.0D, step = 0.1D)
	public float time = 6.0F;
	@Option(name = "Animate time",
			description = "Continuously advance the displayed time instead of holding a fixed hour.",
			order = 2)
	public boolean fastTime;
	@Option(name = "Animation speed", description = "Multiplier for animated time.", order = 3)
	@Range(min = 0.1D, max = 10.0D, step = 0.1D)
	public float fastSpeed = 1.0F;
	public TimeChangerFeature() {
		super("Time Changer", FeatureCategory.WORLD);
	}

	public long timeToTicks() {
		if (fastTime) {
			return (long) ((Minecraft.getSystemTime() * Math.max(0.1D, fastSpeed)) % 24000.0D);
		}
		return timeToMcTime(time);
	}

	private long timeToMcTime(final float time) {
		return (long) (time * 1000L) + 18000L;
	}
}
