package org.afterlike.openutils.util.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.afterlike.openutils.util.client.TextUtil;

public final class WorldUtil {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static List<String> cachedScoreboard = Collections.emptyList();
	private static int lastTick = -1;
	private WorldUtil() {
	}

	public static List<String> getScoreboard() {
		if (mc.theWorld == null) {
			cachedScoreboard = Collections.emptyList();
			lastTick = -1;
			return Collections.emptyList();
		}
		if (mc.thePlayer != null && mc.thePlayer.ticksExisted == lastTick
				&& !cachedScoreboard.isEmpty()) {
			return cachedScoreboard;
		}
		Scoreboard scoreboard = mc.theWorld.getScoreboard();
		if (scoreboard == null) {
			return Collections.emptyList();
		}
		ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
		if (objective == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		result.add(TextUtil.stripAliens(objective.getDisplayName()));
		List<Score> scores = new ArrayList<>(scoreboard.getSortedScores(objective));
		List<String> scoreList = new ArrayList<>();
		int limit = Math.min(15, scores.size());
		for (int i = 0; i < limit; i++) {
			Score score = scores.get(i);
			String playerName = score.getPlayerName();
			ScorePlayerTeam team = scoreboard.getPlayersTeam(playerName);
			scoreList.add(ScorePlayerTeam.formatPlayerName(team, playerName));
		}
		Collections.reverse(scoreList);
		for (String line : scoreList) {
			result.add(TextUtil.stripAliens(line));
		}
		if (mc.thePlayer != null) {
			lastTick = mc.thePlayer.ticksExisted;
			cachedScoreboard = Collections.unmodifiableList(result);
		}
		return result;
	}
}
