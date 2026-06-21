package org.afterlike.openutils.feature.impl.client;

import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.confikure.annotations.Option;

public class DebugFeature extends ToggleableFeature {
	@Option(name = "Enable Debug", description = "Print extra diagnostic chat messages.", order = 0)
	public boolean enabled;
	public DebugFeature() {
		super("Debug", FeatureCategory.CLIENT);
	}

	@Override
	protected void onEnable() {
		ClientUtil.sendDebugMessage("debug enabled");
	}
}
