package org.afterlike.openutils.event.impl;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import re.tsuku.fastbus.CancellableEvent;

public class WindowClickEvent extends CancellableEvent {
	private final GuiContainer container;
	private final Slot slot;
	private final int slotId;
	private final int clickedButton;
	private final int clickType;
	public WindowClickEvent(GuiContainer container, Slot slot, int slotId, int clickedButton,
			int clickType) {
		this.container = container;
		this.slot = slot;
		this.slotId = slotId;
		this.clickedButton = clickedButton;
		this.clickType = clickType;
	}

	public GuiContainer getContainer() {
		return container;
	}

	public Slot getSlot() {
		return slot;
	}

	public int getSlotId() {
		return slotId;
	}

	public int getClickedButton() {
		return clickedButton;
	}

	public int getClickType() {
		return clickType;
	}
}
