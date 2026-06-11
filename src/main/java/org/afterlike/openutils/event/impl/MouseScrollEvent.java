package org.afterlike.openutils.event.impl;

import re.tsuku.fastbus.Event;

public class MouseScrollEvent implements Event {
	private final int dWheel;
	public MouseScrollEvent(int dWheel) {
		this.dWheel = dWheel;
	}

	public int getDWheel() {
		return dWheel;
	}
}
