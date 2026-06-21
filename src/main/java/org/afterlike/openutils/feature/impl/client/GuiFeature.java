package org.afterlike.openutils.feature.impl.client;

import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.api.Feature;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.gui.OpenUtilsConfigScreen;
import org.afterlike.openutils.util.client.ClientUtil;
import org.lwjgl.input.Keyboard;
import re.tsuku.confikure.annotations.Keybind;
import re.tsuku.confikure.annotations.Mode;
import re.tsuku.confikure.annotations.Option;

public class GuiFeature extends Feature {
	@Option(name = "Keybind", description = "Opens the config screen.", order = 0)
	@Keybind
	public int keybind = Keyboard.KEY_RSHIFT;
	@Option(name = "Dim background",
			description = "Draw the vanilla dim background behind the config screen.", order = 1)
	public boolean background = true;
	@Option(name = "Theme", description = "Color scheme used by the config screen.", order = 2)
	@Mode(values = {"minecraft", "catppuccin mocha", "ayu mirage"})
	public String theme = "minecraft";
	public GuiFeature() {
		super("GUI", FeatureCategory.CLIENT);
		setKeybind(this.keybind);
	}

	@Override
	public void onConfigChanged() {
		setKeybind(this.keybind);
	}

	@Override
	protected void onEnable() {
		if (ClientUtil.notNull() && !(mc.currentScreen instanceof OpenUtilsConfigScreen)) {
			OpenUtils.get().getConfigHandler().openGui();
		}
		this.setEnabled(false);
	}
}
