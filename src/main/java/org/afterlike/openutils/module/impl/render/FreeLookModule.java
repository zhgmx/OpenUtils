package org.afterlike.openutils.module.impl.render;

import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.setting.impl.BooleanSetting;
import org.afterlike.openutils.module.api.setting.impl.DescriptionSetting;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class FreeLookModule extends Module {
	private final BooleanSetting lockYaw;
	private final BooleanSetting invertYaw;
	private final BooleanSetting lockPitch;
	private final BooleanSetting invertPitch;
	private float cameraYaw = 0f;
	private float cameraPitch = 0f;
	private int prevPerspective = 0;
	public FreeLookModule() {
		super("Free Look", ModuleCategory.RENDER);
		this.registerSetting(
				new DescriptionSetting("Free Look requires the keybind to be held down"));
		lockYaw = this.registerSetting(new BooleanSetting("Lock yaw", false));
		invertYaw = this.registerSetting(new BooleanSetting("Invert yaw", false));
		lockPitch = this.registerSetting(new BooleanSetting("Lock pitch", false));
		invertPitch = this.registerSetting(new BooleanSetting("Invert pitch", false));
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
		if (!lockYaw.getValue()) {
			handleYaw();
		}
		if (!lockPitch.getValue()) {
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
		if (invertYaw.getValue()) {
			yaw = -yaw;
		}
		cameraYaw += yaw * 0.15f;
	}

	private void handlePitch() {
		float sens = calculateSensitivity();
		float pitch = mc.mouseHelper.deltaY * sens;
		if (invertPitch.getValue()) {
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
			setEnabled(false);
		}
	}

	@Subscribe
	private void onGameTick(final GameTickEvent event) {
		if (isEnabled() && mc.currentScreen != null) {
			setEnabled(false);
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
