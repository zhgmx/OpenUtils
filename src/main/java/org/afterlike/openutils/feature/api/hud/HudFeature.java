package org.afterlike.openutils.feature.api.hud;

public interface HudFeature {
	Position getHudPosition();

	String getHudPlaceholderText();

	default boolean useHudDropShadow() {
		return true;
	}
}
