package org.afterlike.openutils.feature.api;

import net.minecraft.client.Minecraft;
import org.afterlike.openutils.OpenUtils;

public class Feature {
	protected static final Minecraft mc = Minecraft.getMinecraft();
	private final String name;
	private final FeatureCategory category;
	private boolean active;
	private boolean transientEnabled;
	public Feature(final String name, final FeatureCategory category) {
		this.name = name;
		this.category = category;
	}

	public void toggle() {
		setEnabled(!isEnabled());
	}

	public void setEnabled(final boolean enabled) {
		setConfiguredEnabled(enabled);
		setActive(enabled);
		OpenUtils.get().getConfigHandler().saveConfiguration();
	}

	public void applyConfiguredState() {
		setActive(isConfiguredEnabled());
	}

	public void setActiveTemporarily(final boolean enabled) {
		setActive(enabled);
	}

	private void setActive(final boolean enabled) {
		if (this.active == enabled) {
			return;
		}
		this.active = enabled;
		if (enabled) {
			onEnable();
			if (this.active) {
				OpenUtils.get().getEventBus().subscribe(this);
			}
		} else {
			onDisable();
			OpenUtils.get().getEventBus().unsubscribe(this);
		}
	}

	public boolean isEnabled() {
		return this.active;
	}

	protected boolean isConfiguredEnabled() {
		return this.transientEnabled;
	}

	protected void setConfiguredEnabled(final boolean enabled) {
		this.transientEnabled = enabled;
	}

	public String getName() {
		return name;
	}

	public FeatureCategory getCategory() {
		return category;
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	public void onConfigChanged() {
	}
}
