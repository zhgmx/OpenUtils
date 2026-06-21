package org.afterlike.openutils.feature.api;

public interface KeyboundFeature {
	int getKeybind();

	void onKeyInput(boolean pressed);
}
