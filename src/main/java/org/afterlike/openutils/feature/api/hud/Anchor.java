package org.afterlike.openutils.feature.api.hud;

public enum Anchor {
	TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER;
	public int toScreenX(final int offset, final int screenWidth, final int elementWidth) {
		switch (this) {
			case TOP_RIGHT :
			case BOTTOM_RIGHT :
				return screenWidth - offset - elementWidth;
			case CENTER :
				return screenWidth / 2 + offset;
			default :
				return offset;
		}
	}

	public int toScreenY(final int offset, final int screenHeight, final int elementHeight) {
		switch (this) {
			case BOTTOM_LEFT :
			case BOTTOM_RIGHT :
				return screenHeight - offset - elementHeight;
			case CENTER :
				return screenHeight / 2 + offset;
			default :
				return offset;
		}
	}

	public int fromScreenX(final int screenX, final int screenWidth, final int elementWidth) {
		switch (this) {
			case TOP_RIGHT :
			case BOTTOM_RIGHT :
				return screenWidth - screenX - elementWidth;
			case CENTER :
				return screenX - screenWidth / 2;
			default :
				return screenX;
		}
	}

	public int fromScreenY(final int screenY, final int screenHeight, final int elementHeight) {
		switch (this) {
			case BOTTOM_LEFT :
			case BOTTOM_RIGHT :
				return screenHeight - screenY - elementHeight;
			case CENTER :
				return screenY - screenHeight / 2;
			default :
				return screenY;
		}
	}

	public static Anchor detect(final int x, final int y, final int screenWidth,
			final int screenHeight) {
		final int centerThreshold = (int) (Math.min(screenWidth, screenHeight) * 0.2);
		final int centerX = screenWidth / 2;
		final int centerY = screenHeight / 2;
		if (Math.abs(x - centerX) < centerThreshold && Math.abs(y - centerY) < centerThreshold) {
			return CENTER;
		}
		final boolean left = x < centerX;
		final boolean top = y < centerY;
		if (top && left)
			return TOP_LEFT;
		if (top)
			return TOP_RIGHT;
		if (left)
			return BOTTOM_LEFT;
		return BOTTOM_RIGHT;
	}
}
