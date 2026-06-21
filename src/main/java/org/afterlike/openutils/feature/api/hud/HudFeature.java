package org.afterlike.openutils.feature.api.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.afterlike.openutils.gui.LayoutEditorScreen;

public interface HudFeature {
	Position getHudPosition();

	String getHudPlaceholderText();

	default boolean useHudDropShadow() {
		return true;
	}

	default void openHudEditor() {
		final Minecraft minecraft = Minecraft.getMinecraft();
		final GuiScreen currentScreen = minecraft.currentScreen;
		minecraft.displayGuiScreen(new LayoutEditorScreen(currentScreen, this));
	}
}
