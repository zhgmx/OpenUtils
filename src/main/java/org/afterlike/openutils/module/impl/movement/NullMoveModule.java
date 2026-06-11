package org.afterlike.openutils.module.impl.movement;

import net.minecraft.client.settings.KeyBinding;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.KeyPressEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.setting.impl.DescriptionSetting;
import org.afterlike.openutils.platform.mixin.minecraft.client.settings.KeyBindingAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class NullMoveModule extends Module {
	private final DescriptionSetting description;
	public NullMoveModule() {
		super("Null Move", ModuleCategory.MOVEMENT);
		description = this.registerSetting(new DescriptionSetting(
				"Prevents opposite movement inputs from canceling each other"));
	}

	@Subscribe
	private void onKeyPress(final KeyPressEvent event) {
		if (!ClientUtil.notNull())
			return;
		KeyBinding left = mc.gameSettings.keyBindLeft;
		KeyBinding right = mc.gameSettings.keyBindRight;
		KeyBinding fwd = mc.gameSettings.keyBindForward;
		KeyBinding back = mc.gameSettings.keyBindBack;
		int code = event.getKeyCode();
		boolean down = event.isPressed();
		if (code == left.getKeyCode()) {
			if (down)
				horiz.press(false);
			else
				horiz.release(false);
		}
		if (code == right.getKeyCode()) {
			if (down)
				horiz.press(true);
			else
				horiz.release(true);
		}
		if (code == back.getKeyCode()) {
			if (down)
				vert.press(false);
			else
				vert.release(false);
		}
		if (code == fwd.getKeyCode()) {
			if (down)
				vert.press(true);
			else
				vert.release(true);
		}
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (mc.currentScreen != null)
			return;
		apply(horiz, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight);
		apply(vert, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindForward);
	}

	private void apply(final InputAxis axis, final KeyBinding negKey, final KeyBinding posKey) {
		boolean neg = axis.neg;
		boolean pos = axis.pos;
		if (neg && pos) {
			boolean posLast = axis.posTime >= axis.negTime;
			neg = !posLast;
			pos = posLast;
		}
		((KeyBindingAccessor) negKey).ou$setPressed(neg);
		((KeyBindingAccessor) posKey).ou$setPressed(pos);
	}
	private static class InputAxis {
		private boolean neg, pos;
		private long negTime, posTime;
		void press(boolean positive) {
			if (positive) {
				pos = true;
				posTime = System.nanoTime();
			} else {
				neg = true;
				negTime = System.nanoTime();
			}
		}

		void release(boolean positive) {
			if (positive)
				pos = false;
			else
				neg = false;
		}
	}
	private final InputAxis horiz = new InputAxis();
	private final InputAxis vert = new InputAxis();
}
