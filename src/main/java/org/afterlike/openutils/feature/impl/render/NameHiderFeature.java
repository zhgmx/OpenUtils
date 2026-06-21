package org.afterlike.openutils.feature.impl.render;

import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Text;

public class NameHiderFeature extends ToggleableFeature {
	@Option(name = "Enable Name Hider",
			description = "Replace your Minecraft username in rendered client text.", order = 0)
	public boolean enabled;
	@Option(name = "Display name", description = "Name shown in place of your Minecraft username.",
			order = 1)
	@Text
	public String customName = "&bYou";
	public NameHiderFeature() {
		super("Name Hider", FeatureCategory.RENDER);
	}

	public String replaceName(String text) {
		if (!ClientUtil.notNull()) {
			return text;
		}
		final String username = mc.thePlayer.getName();
		String replacement = TextUtil.replaceColorCodes(customName);
		return text.replace(username, replacement);
	}
}
