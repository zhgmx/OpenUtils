package org.afterlike.openutils.event.impl;

import re.tsuku.fastbus.Event;

public class KeyPressEvent implements Event {
	private final int keyCode;
	private final boolean pressed;
	public KeyPressEvent(final int keyCode, final boolean pressed) {
		this.keyCode = keyCode;
		this.pressed = pressed;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public boolean isPressed() {
		return pressed;
	}
}
