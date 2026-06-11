package org.afterlike.openutils.event.impl;

import re.tsuku.fastbus.Event;

public class ReceiveChatEvent implements Event {
	private final String message;
	public ReceiveChatEvent(final String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
