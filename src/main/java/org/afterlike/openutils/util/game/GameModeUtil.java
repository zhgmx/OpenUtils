package org.afterlike.openutils.util.game;

import java.util.List;
import org.afterlike.openutils.util.client.TextUtil;

public final class GameModeUtil {
	private GameModeUtil() {
	}

	public static boolean onHypixel() {
		List<String> sb = WorldUtil.getScoreboard();
		for (String s : sb) {
			if (TextUtil.stripColorCodes(s).contains("hypixel.net")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isInBedWarsGame() {
		return getBedWarsStatusType() == BedWarsStatus.IN_GAME;
	}

	public static BedWarsStatus getBedWarsStatusType() {
		List<String> sb = WorldUtil.getScoreboard();
		if (sb.size() < 7) {
			return BedWarsStatus.NOT_BEDWARS;
		}
		// title: BED WARS
		String title = sb.get(0);
		if (!title.contains("BED") || !title.contains("WARS")) {
			return BedWarsStatus.NOT_BEDWARS;
		}
		// L = lobby
		if (sb.get(1).contains("§8L")) {
			return BedWarsStatus.LOBBY;
		}
		// pregame
		if (sb.get(4).contains("/") && sb.get(4).contains("§a")) {
			return BedWarsStatus.PREGAME;
		}
		// in game
		if (sb.get(5).contains("§c") && sb.get(6).contains("§9")) {
			return BedWarsStatus.IN_GAME;
		}
		return BedWarsStatus.NOT_BEDWARS;
	}
	public enum BedWarsStatus {
		NOT_BEDWARS, LOBBY, PREGAME, IN_GAME
	}
}
