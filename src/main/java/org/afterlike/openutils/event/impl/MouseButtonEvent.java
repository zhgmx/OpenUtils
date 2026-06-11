package org.afterlike.openutils.event.impl;

import re.tsuku.fastbus.Event;

public class MouseButtonEvent implements Event {
	private final int button;
	private final boolean pressed;
	public MouseButtonEvent(final int button, final boolean pressed) {
		this.button = button;
		this.pressed = pressed;
	}

	public int getButton() {
		return button;
	}

	public boolean isPressed() {
		return pressed;
	}
}
