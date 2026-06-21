package org.afterlike.openutils.feature.impl.world;

import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import re.tsuku.confikure.annotations.Option;

public class WeatherFeature extends ToggleableFeature {
	@Option(name = "Enable Weather TODO",
			description = "TODO: weather controls are not implemented yet.", order = 0)
	public boolean enabled;
	public WeatherFeature() {
		super("Weather TODO", FeatureCategory.WORLD);
	}
}
