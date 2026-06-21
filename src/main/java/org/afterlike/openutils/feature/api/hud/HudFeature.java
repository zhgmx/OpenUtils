package org.afterlike.openutils.feature.api.hud;

import net.minecraft.client.Minecraft;

public interface HudFeature {
	Position getHudPosition();

	String[] getHudPreviewLines();

	default int getHudPreviewWidth() {
		int width = 34;
		for (final String line : getHudPreviewLines()) {
			width = Math.max(width, Minecraft.getMinecraft().fontRendererObj.getStringWidth(line));
		}
		return width;
	}

	default int getHudPreviewHeight() {
		final int lines = getHudPreviewLines().length;
		return lines * Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT
				+ Math.max(0, lines - 1) * 2;
	}

	default void renderHudPreview(final int x, final int y) {
		int currentY = y;
		for (final String line : getHudPreviewLines()) {
			Minecraft.getMinecraft().fontRendererObj.drawString(line, x, currentY, 0xFFFFFFFF,
					useHudDropShadow());
			currentY += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2;
		}
	}

	default boolean useHudDropShadow() {
		return true;
	}
}
