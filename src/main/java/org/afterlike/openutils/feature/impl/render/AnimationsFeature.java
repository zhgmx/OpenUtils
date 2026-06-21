package org.afterlike.openutils.feature.impl.render;

import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import re.tsuku.confikure.annotations.Option;

public class AnimationsFeature extends ToggleableFeature {
	@Option(name = "Enable Animations",
			description = "Customize first-person item animation behavior.", order = 0)
	public boolean enabled;
	@Option(name = "Force block animation",
			description = "Keep the first-person block pose visible while using a sword.",
			order = 1)
	public boolean forceBlockAnimation = true;
	@Option(name = "Require mouse down",
			description = "Only force the block pose while the use-item key is currently pressed.",
			order = 2)
	public boolean requireMouseDown;
	public AnimationsFeature() {
		super("Animations", FeatureCategory.RENDER);
	}
}
