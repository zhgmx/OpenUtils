package org.afterlike.openutils.feature.impl.render;

import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import re.tsuku.confikure.annotations.Option;

public class AntiShuffleFeature extends ToggleableFeature {
	@Option(name = "Enable Anti Shuffle",
			description = "Strip obfuscated text formatting from rendered strings.", order = 0)
	public boolean enabled;
	public AntiShuffleFeature() {
		super("Anti Shuffle", FeatureCategory.RENDER);
	}
}
