package org.afterlike.openutils.util.game;

import java.util.List;
import org.afterlike.openutils.util.client.TextUtil;

public class GameModeUtil {
	public static boolean onHypixel() {
		List<String> sb = WorldUtil.getScoreboard();
		for (String s : sb) {
			if (TextUtil.stripColorCodes(s).contains("hypixel.net")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the current Bed Wars status.
	 *
	 * @return status code:
	 *         <ul>
	 *         <li>0 — Not in Bed Wars</li>
	 *         <li>1 — In Lobby</li>
	 *         <li>2 — In Pregame</li>
	 *         <li>3 — In Game</li>
	 *         </ul>
	 */
	public static int getBedWarsStatus() {
		List<String> sb = WorldUtil.getScoreboard();
		if (sb.size() < 7) {
			return 0;
		}
		// title: BED WARS
		String title = sb.get(0);
		if (!title.contains("BED") || !title.contains("WARS")) {
			return 0;
		}
		// L = lobby
		if (sb.get(1).contains("§8L")) {
			return 1;
		}
		// pregame
		if (sb.get(4).contains("/") && sb.get(4).contains("§a")) {
			return 2;
		}
		// in game
		if (sb.get(5).contains("§c") && sb.get(6).contains("§9")) {
			return 3;
		}
		return 0;
	}
}
