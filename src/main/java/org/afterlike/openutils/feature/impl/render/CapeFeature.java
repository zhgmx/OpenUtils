package org.afterlike.openutils.feature.impl.render;

import net.minecraft.util.ResourceLocation;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import re.tsuku.confikure.annotations.Dropdown;
import re.tsuku.confikure.annotations.Option;

public class CapeFeature extends ToggleableFeature {
	@Option(name = "Enable Cape", description = "Render the selected cape texture on your player.",
			order = 0)
	public boolean enabled;
	@Option(name = "Cape", description = "Cape texture to render on your player.", order = 1)
	@Dropdown(values = {"2011", "2012", "2013", "2015", "2016", "daisy"})
	public String cape = "2016";
	private ResourceLocation location;
	public CapeFeature() {
		super("Cape", FeatureCategory.RENDER);
	}

	@Override
	protected void onEnable() {
		setLocation();
	}

	@Override
	public void onConfigChanged() {
		setLocation();
	}

	private void setLocation() {
		location = new ResourceLocation("openutils", "capes/" + cape + ".png");
	}

	public ResourceLocation getCapeLocation() {
		return location;
	}
}
