package org.afterlike.openutils.feature.api.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public final class Position {
	private final int defaultX;
	private final int defaultY;
	private final Anchor defaultAnchor;
	private int x;
	private int y;
	private Anchor anchor;
	public Position(final int defaultX, final int defaultY) {
		this(defaultX, defaultY, Anchor.TOP_LEFT);
	}

	public Position(final int defaultX, final int defaultY, final Anchor defaultAnchor) {
		this.defaultX = defaultX;
		this.defaultY = defaultY;
		this.defaultAnchor = defaultAnchor != null ? defaultAnchor : Anchor.TOP_LEFT;
		this.x = defaultX;
		this.y = defaultY;
		this.anchor = this.defaultAnchor;
	}

	public int getX() {
		return anchor.toScreenX(x, getScreenWidth(), 0);
	}

	public int getY() {
		return anchor.toScreenY(y, getScreenHeight(), 0);
	}

	public int getX(final int elementWidth) {
		return anchor.toScreenX(x, getScreenWidth(), elementWidth);
	}

	public int getY(final int elementHeight) {
		return anchor.toScreenY(y, getScreenHeight(), elementHeight);
	}

	public int getOffsetX() {
		return x;
	}

	public int getOffsetY() {
		return y;
	}

	public Anchor getAnchor() {
		return anchor;
	}

	public void setAnchor(final Anchor anchor) {
		this.anchor = anchor != null ? anchor : Anchor.TOP_LEFT;
	}

	public void setPosition(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public void setScreenPosition(final int screenX, final int screenY) {
		this.x = anchor.fromScreenX(screenX, getScreenWidth(), 0);
		this.y = anchor.fromScreenY(screenY, getScreenHeight(), 0);
	}

	public void reset() {
		this.x = defaultX;
		this.y = defaultY;
		this.anchor = defaultAnchor;
	}

	public Position copy() {
		final Position copy = new Position(this.x, this.y, this.anchor);
		return copy;
	}

	private static int getScreenWidth() {
		final Minecraft mc = Minecraft.getMinecraft();
		if (mc == null)
			return 0;
		return new ScaledResolution(mc).getScaledWidth();
	}

	private static int getScreenHeight() {
		final Minecraft mc = Minecraft.getMinecraft();
		if (mc == null)
			return 0;
		return new ScaledResolution(mc).getScaledHeight();
	}
}
