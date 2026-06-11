package org.afterlike.openutils.event.impl;

import net.minecraft.network.Packet;
import re.tsuku.fastbus.Event;

public class ReceivePacketEvent implements Event {
	private final Packet<?> packet;
	public ReceivePacketEvent(final Packet<?> packet) {
		this.packet = packet;
	}

	public Packet<?> getPacket() {
		return packet;
	}
}
