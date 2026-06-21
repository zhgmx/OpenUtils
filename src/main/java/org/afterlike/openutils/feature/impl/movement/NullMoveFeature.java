package org.afterlike.openutils.feature.impl.movement;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.platform.mixin.minecraft.client.settings.KeyBindingAccessor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import re.tsuku.confikure.annotations.Mode;
import re.tsuku.confikure.annotations.Option;

public class NullMoveFeature extends ToggleableFeature {
	private static final String LAST_INPUT_PRIORITY = "Last Input Priority";
	private static final String ABSOLUTE_PRIORITY = "Absolute Priority";
	private static final String NEUTRAL = "Neutral";
	private static final String FORWARD = "Forward";
	private static final String BACKWARD = "Backward";
	private static final String LEFT = "Left";
	private static final String RIGHT = "Right";
	private final AxisState forwardBack = new AxisState();
	private final AxisState side = new AxisState();
	@Option(name = "Enable Snap Tap",
			description = "Resolves simultaneous opposite movement inputs per axis.", order = 0)
	public boolean enabled;
	@Option(name = "Forward / Back SOCD",
			description = "Choose how forward and backward are resolved when both are held.",
			order = 1)
	@Mode(values = {LAST_INPUT_PRIORITY, ABSOLUTE_PRIORITY, NEUTRAL})
	public String forwardBackMode = LAST_INPUT_PRIORITY;
	@Option(id = "forward-back-priority", name = "Absolute Priority",
			description = "Direction kept when forward and backward are both held.", order = 2)
	@Mode(values = {FORWARD, BACKWARD})
	public String forwardBackPriority = FORWARD;
	@Option(name = "Side SOCD",
			description = "Choose how left and right are resolved when both are held.", order = 3)
	@Mode(values = {LAST_INPUT_PRIORITY, ABSOLUTE_PRIORITY, NEUTRAL})
	public String sideMode = LAST_INPUT_PRIORITY;
	@Option(id = "side-priority", name = "Absolute Priority",
			description = "Direction kept when left and right are both held.", order = 4)
	@Mode(values = {LEFT, RIGHT})
	public String sidePriority = LEFT;
	public NullMoveFeature() {
		super("Snap Tap", FeatureCategory.MOVEMENT);
	}

	public void applyBeforeMovementInput() {
		if (!isEnabled()) {
			return;
		}
		if (mc.thePlayer == null || mc.currentScreen != null) {
			releaseMovement();
			return;
		}
		final GameSettings settings = mc.gameSettings;
		applyAxis(settings.keyBindForward, settings.keyBindBack, forwardBack, forwardBackMode,
				FORWARD.equals(forwardBackPriority));
		applyAxis(settings.keyBindLeft, settings.keyBindRight, side, sideMode,
				LEFT.equals(sidePriority));
	}

	public boolean showsForwardBackPriority() {
		return ABSOLUTE_PRIORITY.equals(forwardBackMode);
	}

	public boolean showsSidePriority() {
		return ABSOLUTE_PRIORITY.equals(sideMode);
	}

	@Override
	protected void onEnable() {
		reset();
	}

	@Override
	protected void onDisable() {
		restoreMovementState();
		reset();
	}

	private void restoreMovementState() {
		if (mc.gameSettings == null) {
			return;
		}
		if (mc.thePlayer == null || mc.currentScreen != null) {
			releaseMovement();
			return;
		}
		final GameSettings settings = mc.gameSettings;
		setPressed(settings.keyBindForward, isPhysicallyDown(settings.keyBindForward));
		setPressed(settings.keyBindBack, isPhysicallyDown(settings.keyBindBack));
		setPressed(settings.keyBindLeft, isPhysicallyDown(settings.keyBindLeft));
		setPressed(settings.keyBindRight, isPhysicallyDown(settings.keyBindRight));
	}

	private void releaseMovement() {
		if (mc.gameSettings == null) {
			return;
		}
		final GameSettings settings = mc.gameSettings;
		setPressed(settings.keyBindForward, false);
		setPressed(settings.keyBindBack, false);
		setPressed(settings.keyBindLeft, false);
		setPressed(settings.keyBindRight, false);
	}

	private void reset() {
		forwardBack.reset();
		side.reset();
	}

	private void applyAxis(final KeyBinding first, final KeyBinding second, final AxisState state,
			final String mode, final boolean absoluteFirst) {
		final boolean firstDown = isPhysicallyDown(first);
		final boolean secondDown = isPhysicallyDown(second);
		state.update(firstDown, secondDown);
		if (firstDown && secondDown) {
			if (NEUTRAL.equals(mode)) {
				setPressed(first, false);
				setPressed(second, false);
				return;
			}
			final boolean useFirst = ABSOLUTE_PRIORITY.equals(mode)
					? absoluteFirst
					: state.lastFirst;
			setPressed(first, useFirst);
			setPressed(second, !useFirst);
			return;
		}
		setPressed(first, firstDown);
		setPressed(second, secondDown);
	}

	private static void setPressed(final KeyBinding key, final boolean pressed) {
		((KeyBindingAccessor) key).ou$setPressed(pressed);
	}

	private static boolean isPhysicallyDown(final KeyBinding key) {
		final int code = key.getKeyCode();
		try {
			if (code > 0) {
				return Keyboard.isKeyDown(code);
			}
			if (code < 0) {
				return Mouse.isButtonDown(code + 100);
			}
		} catch (final RuntimeException ignored) {
			return false;
		}
		return false;
	}
	private static final class AxisState {
		private boolean previousFirst;
		private boolean previousSecond;
		private boolean lastFirst = true;
		private void update(final boolean firstDown, final boolean secondDown) {
			if (firstDown && !previousFirst) {
				lastFirst = true;
			}
			if (secondDown && !previousSecond) {
				lastFirst = false;
			}
			previousFirst = firstDown;
			previousSecond = secondDown;
		}

		private void reset() {
			previousFirst = false;
			previousSecond = false;
			lastFirst = true;
		}
	}
}
