package org.afterlike.openutils.util.game;

import net.minecraft.entity.player.EntityPlayer;

public final class BedWarsUtil {
	private BedWarsUtil() {
	}
	public enum TeamColor {
		RED("Red", "§c"), BLUE("Blue", "§9"), GREEN("Green", "§a"), YELLOW("Yellow", "§e"), AQUA(
				"Aqua", "§b"), WHITE("White", "§f"), PINK("Pink", "§d"), GRAY("Gray", "§8");
		private final String displayName;
		private final String colorCode;
		TeamColor(final String displayName, final String colorCode) {
			this.displayName = displayName;
			this.colorCode = colorCode;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getColorCode() {
			return colorCode;
		}

		public static TeamColor fromFormattedName(final String formattedName) {
			if (formattedName == null) {
				return null;
			}
			for (TeamColor color : values()) {
				if (formattedName.contains(color.colorCode)) {
					return color;
				}
			}
			return null;
		}

		public static TeamColor fromColorCode(final String colorCode) {
			if (colorCode == null) {
				return null;
			}
			for (TeamColor color : values()) {
				if (colorCode.equals(color.colorCode)) {
					return color;
				}
			}
			return null;
		}
	}
	public static String getFormattedTeamName(final EntityPlayer player) {
		TeamColor color = getTeamColor(player);
		return color != null ? color.getColorCode() + color.getDisplayName() : null;
	}

	public static String getTeamName(final EntityPlayer player) {
		TeamColor color = getTeamColor(player);
		return color != null ? color.getDisplayName() : null;
	}

	public static TeamColor getTeamColor(final EntityPlayer player) {
		if (player == null) {
			return null;
		}
		return TeamColor.fromFormattedName(player.getDisplayName().getFormattedText());
	}

	public static boolean isTeammate(final EntityPlayer self, final EntityPlayer target) {
		TeamColor targetColor = getTeamColor(target);
		TeamColor selfColor = getTeamColor(self);
		return targetColor != null && targetColor == selfColor;
	}
}
