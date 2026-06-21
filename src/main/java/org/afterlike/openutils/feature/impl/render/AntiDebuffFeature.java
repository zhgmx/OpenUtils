package org.afterlike.openutils.feature.impl.render;

import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import re.tsuku.confikure.annotations.Option;

public class AntiDebuffFeature extends ToggleableFeature {
	@Option(name = "Enable Anti Debuff",
			description = "Suppress selected visual potion effects on your client.", order = 0)
	public boolean enabled;
	@Option(name = "Remove nausea", description = "Suppress the portal-style nausea camera wobble.",
			order = 1)
	public boolean nausea = true;
	@Option(name = "Remove blindness", description = "Prevent blindness from darkening the screen.",
			order = 2)
	public boolean blindness = true;
	public AntiDebuffFeature() {
		super("Anti Debuff", FeatureCategory.RENDER);
	}
}
