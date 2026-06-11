package org.afterlike.openutils.event.impl;

import re.tsuku.fastbus.Event;

public class RenderWorldEvent implements Event {
	private final float partialTicks;
	public RenderWorldEvent(final float partialTicks) {
		this.partialTicks = partialTicks;
	}

	public float getPartialTicks() {
		return partialTicks;
	}
}
