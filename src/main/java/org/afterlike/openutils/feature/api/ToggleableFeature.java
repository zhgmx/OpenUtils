package org.afterlike.openutils.feature.api;

import java.lang.reflect.Field;

public class ToggleableFeature extends Feature {
	public ToggleableFeature(final String name, final FeatureCategory category) {
		super(name, category);
	}

	@Override
	protected boolean isConfiguredEnabled() {
		try {
			return enabledField().getBoolean(this);
		} catch (final IllegalAccessException exception) {
			throw new IllegalStateException(
					"Unable to read enabled config field: " + getClass().getName(), exception);
		}
	}

	@Override
	protected void setConfiguredEnabled(final boolean enabled) {
		try {
			enabledField().setBoolean(this, enabled);
		} catch (final IllegalAccessException exception) {
			throw new IllegalStateException(
					"Unable to write enabled config field: " + getClass().getName(), exception);
		}
	}

	private Field enabledField() {
		try {
			final Field field = getClass().getField("enabled");
			if (field.getType() != boolean.class) {
				throw new IllegalStateException(
						"Feature enabled field must be boolean: " + getClass().getName());
			}
			return field;
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException("Missing enabled config field: " + getClass().getName(),
					exception);
		}
	}
}
