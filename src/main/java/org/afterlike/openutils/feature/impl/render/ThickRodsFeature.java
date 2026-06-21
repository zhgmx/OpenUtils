package org.afterlike.openutils.feature.impl.render;

import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;

public class ThickRodsFeature extends ToggleableFeature {
	@Option(name = "Enable Thick Rods",
			description = "Render cast fishing lines with a custom thickness.", order = 0)
	public boolean enabled;
	@Option(name = "Line thickness",
			description = "Pixel width used when drawing cast fishing lines.", order = 1)
	@Range(min = 0.0D, max = 10.0D, step = 1.0D)
	public int thickness = 4;
	public ThickRodsFeature() {
		super("Thick Rods", FeatureCategory.RENDER);
	}
}
