package org.afterlike.openutils.feature.impl.render;

import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.feature.api.Feature;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.KeyboundFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.lwjgl.input.Keyboard;
import re.tsuku.confikure.annotations.Keybind;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class FreeLookFeature extends Feature implements KeyboundFeature {
	@Option(name = "Keybind", description = "Hold this key to detach the third-person camera.",
			order = 0)
	@Keybind
	public int keybind = Keyboard.KEY_NONE;
	@Option(name = "Lock yaw",
			description = "Keep horizontal camera rotation fixed while Free Look is held.",
			order = 1)
	public boolean lockYaw;
	@Option(name = "Invert yaw",
			description = "Reverse horizontal mouse movement while Free Look is held.", order = 2)
	public boolean invertYaw;
	@Option(name = "Lock pitch",
			description = "Keep vertical camera rotation fixed while Free Look is held.", order = 3)
	public boolean lockPitch;
	@Option(name = "Invert pitch",
			description = "Reverse vertical mouse movement while Free Look is held.", order = 4)
	public boolean invertPitch;
	private float cameraYaw = 0f;
	private float cameraPitch = 0f;
	private int prevPerspective = 0;
	public FreeLookFeature() {
		super("Free Look", FeatureCategory.RENDER);
	}

	@Override
	public int getKeybind() {
		return keybind;
	}

	@Override
	public void onKeyInput(final boolean pressed) {
		setActiveTemporarily(pressed);
	}

	private void enter() {
		if (!ClientUtil.notNull()) {
			return;
		}
		cameraYaw = mc.thePlayer.rotationYaw;
		cameraPitch = mc.thePlayer.rotationPitch;
		prevPerspective = mc.gameSettings.thirdPersonView;
		mc.gameSettings.thirdPersonView = 1;
		mc.renderGlobal.setDisplayListEntitiesDirty();
	}

	public void reset() {
		if (!ClientUtil.notNull()) {
			return;
		}
		mc.gameSettings.thirdPersonView = prevPerspective;
		mc.renderGlobal.setDisplayListEntitiesDirty();
	}

	public boolean overrideMouse() {
		if (!ClientUtil.notNull() || !mc.inGameHasFocus) {
			return false;
		}
		mc.mouseHelper.mouseXYChange();
		if (!lockYaw) {
			handleYaw();
		}
		if (!lockPitch) {
			handlePitch();
		}
		if (cameraPitch > 90.0f) {
			cameraPitch = 90.0f;
		} else if (cameraPitch < -90.0f) {
			cameraPitch = -90.0f;
		}
		mc.renderGlobal.setDisplayListEntitiesDirty();
		return false;
	}

	private void handleYaw() {
		float sens = calculateSensitivity();
		float yaw = mc.mouseHelper.deltaX * sens;
		if (invertYaw) {
			yaw = -yaw;
		}
		cameraYaw += yaw * 0.15f;
	}

	private void handlePitch() {
		float sens = calculateSensitivity();
		float pitch = mc.mouseHelper.deltaY * sens;
		if (invertPitch) {
			pitch = -pitch;
		}
		cameraPitch += pitch * 0.15f;
	}

	public static float calculateSensitivity() {
		float sensitivity = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
		return sensitivity * sensitivity * sensitivity * 8.0f;
	}

	@Subscribe
	private void onWorldLoad(final WorldLoadEvent event) {
		if (isEnabled()) {
			setActiveTemporarily(false);
		}
	}

	@Subscribe
	private void onGameTick(final GameTickEvent event) {
		if (isEnabled() && mc.currentScreen != null) {
			setActiveTemporarily(false);
		}
	}

	@Override
	protected void onEnable() {
		enter();
	}

	@Override
	protected void onDisable() {
		reset();
	}

	public float getCameraYaw() {
		return cameraYaw;
	}

	public float getCameraPitch() {
		return cameraPitch;
	}
}
