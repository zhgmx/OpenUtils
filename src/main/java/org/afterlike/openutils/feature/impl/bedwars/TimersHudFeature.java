package org.afterlike.openutils.feature.impl.bedwars;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.RenderOverlayEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.feature.api.hud.HudFeature;
import org.afterlike.openutils.feature.api.hud.Position;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import org.afterlike.openutils.util.game.WorldUtil;
import re.tsuku.confikure.annotations.Button;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class TimersHudFeature extends ToggleableFeature implements HudFeature {
	private final Position position = new Position(5, 150);
	@Option(name = "Enable Timers HUD",
			description = "Show BedWars game and generator timers as a movable HUD.", order = 0)
	public boolean enabled;
	@Option(name = "Drop shadow", description = "Draw timer text with Minecraft's text shadow.",
			order = 1)
	public boolean dropShadow = true;
	@Option(name = "Show game time",
			description = "Display elapsed game time above generator timers.", order = 2)
	public boolean showElapsedTime;
	private boolean inGame = false;
	private String mode = "";
	private int tickCounter = 0;
	private int elapsedTime = 0;
	private int emeraldCount = 0;
	private int emeraldNext = 0;
	private int diamondCount = 0;
	private int diamondNext = 0;
	private static final String MODE_EIGHT = "eight";
	private static final String MODE_FOUR = "four";
	private static final int EMERALD_FIRST = 31;
	private static final int DIAMOND_FIRST = 1;
	private static final List<Integer> DIAMOND_UPGRADES = Arrays.asList(6 * 60, 18 * 60);
	private static final List<Integer> EMERALD_UPGRADES = Arrays.asList(12 * 60, 24 * 60);
	private static final List<StageEvent> STAGE_EVENTS = Arrays.asList(
			new StageEvent("Diamond II", 6 * 60), new StageEvent("Emerald II", 12 * 60),
			new StageEvent("Diamond III", 18 * 60), new StageEvent("Emerald III", 24 * 60),
			new StageEvent("Bed gone", 30 * 60), new StageEvent("Sudden Death", 40 * 60),
			new StageEvent("Game End", 50 * 60));
	private static final Pattern TIMER_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})");
	private static final class StageEvent {
		final String name;
		final int time;
		StageEvent(final String name, final int time) {
			this.name = name;
			this.time = time;
		}
	}
	private static final class SpawnResult {
		final int count;
		final int next;
		SpawnResult(final int count, final int next) {
			this.count = count;
			this.next = next;
		}
	}
	public TimersHudFeature() {
		super("Timers HUD", FeatureCategory.BEDWARS);
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		final int status = GameModeUtil.getBedWarsStatus();
		if (status == 3) {
			if (!inGame) {
				startGame();
			} else {
				tickCounter++;
				if (tickCounter % 20 == 0) {
					updateFromSidebar();
				}
			}
			return;
		}
		if (inGame) {
			resetAll();
		}
	}

	@Subscribe
	private void onRender(final RenderOverlayEvent event) {
		if (!ClientUtil.notNull() || mc.gameSettings.showDebugInfo || !inGame) {
			return;
		}
		final int x = position.getX();
		int y = position.getY();
		int delta = 0;
		if (showElapsedTime) {
			drawLine("§eGame Time: §f" + formatTime(elapsedTime), x, y, delta);
			y += mc.fontRendererObj.FONT_HEIGHT + 2;
			delta -= 90;
		}
		drawLine("§aEmeralds (§7" + emeraldCount + "§a): §f" + emeraldNext, x, y, delta);
		y += mc.fontRendererObj.FONT_HEIGHT + 2;
		delta -= 90;
		drawLine("§bDiamonds (§7" + diamondCount + "§b): §f" + diamondNext, x, y, delta);
	}

	private void drawLine(final String text, final int x, final int y, final int delta) {
		mc.fontRendererObj.drawString(text, x, y, 0xFFFFFFFF, useHudDropShadow());
	}

	private void startGame() {
		inGame = true;
		tickCounter = 0;
		detectMode();
		updateFromSidebar();
	}

	@Override
	protected void onEnable() {
		super.onEnable();
		resetAll();
	}

	@Override
	protected void onDisable() {
		resetAll();
		super.onDisable();
	}

	@Override
	public Position getHudPosition() {
		return position;
	}

	@Override
	public String getHudPlaceholderText() {
		return "Game Time:-Emeralds:-Diamonds:";
	}

	@Override
	public boolean useHudDropShadow() {
		return dropShadow;
	}

	@Button(name = "Layout Editor", description = "Open the layout editor with Timers selected.",
			label = "layout", order = 3)
	public void editPosition() {
		openHudEditor();
	}

	private void detectMode() {
		final List<String> scoreboard = WorldUtil.getScoreboard();
		boolean hasPink = false;
		for (final String line : scoreboard) {
			if (TextUtil.stripColorCodes(line).contains("Pink:")) {
				hasPink = true;
				break;
			}
		}
		mode = hasPink ? MODE_EIGHT : MODE_FOUR;
	}

	private void resetAll() {
		inGame = false;
		mode = "";
		tickCounter = 0;
		elapsedTime = 0;
		emeraldCount = 0;
		emeraldNext = 0;
		diamondCount = 0;
		diamondNext = 0;
	}

	private int getEmeraldInterval(final int tier) {
		if (MODE_EIGHT.equals(mode)) {
			switch (tier) {
				case 2 :
					return 50;
				case 3 :
					return 35;
				default :
					return 65;
			}
		} else if (MODE_FOUR.equals(mode)) {
			switch (tier) {
				case 2 :
					return 40;
				case 3 :
					return 27;
				default :
					return 55;
			}
		}
		return 65;
	}

	private int getDiamondInterval(final int tier) {
		switch (tier) {
			case 2 :
				return 23;
			case 3 :
				return 12;
			default :
				return 30;
		}
	}

	private int getTier(final int time, final List<Integer> upgrades) {
		if (upgrades.size() > 1 && time >= upgrades.get(1)) {
			return 3;
		}
		if (!upgrades.isEmpty() && time >= upgrades.get(0)) {
			return 2;
		}
		return 1;
	}

	private SpawnResult calculateSpawns(final int time, final int first,
			final List<Integer> upgrades, final IntUnaryOperator intervalGen) {
		if (time < first) {
			return new SpawnResult(0, first - time);
		}
		int count = 1;
		int last = first;
		int idxUp = 0;
		int nextUp = upgrades.isEmpty() ? Integer.MAX_VALUE : upgrades.get(0);
		while (true) {
			final int tier = getTier(last, upgrades);
			final int interval = intervalGen.applyAsInt(tier);
			final int next = last + interval;
			if (nextUp <= time && nextUp < next) {
				count++;
				last = nextUp;
				idxUp++;
				nextUp = (idxUp < upgrades.size()) ? upgrades.get(idxUp) : Integer.MAX_VALUE;
				continue;
			}
			if (next > time) {
				return new SpawnResult(count, Math.max(1, next - time));
			}
			count++;
			last = next;
		}
	}

	private void updateFromSidebar() {
		final List<String> sidebar = WorldUtil.getScoreboard();
		if (sidebar.size() < 4)
			return;
		String raw = TextUtil.stripColorCodes(sidebar.get(3)).trim();
		final Matcher matcher = TIMER_PATTERN.matcher(raw);
		if (!matcher.find())
			return;
		final String minStr = matcher.group(1);
		final String secStr = matcher.group(2);
		final int min;
		final int sec;
		try {
			min = Integer.parseInt(minStr);
			sec = Integer.parseInt(secStr);
		} catch (final NumberFormatException e) {
			return;
		}
		final int secondsLeft = min * 60 + sec;
		int inIndex = raw.indexOf("in");
		if (inIndex == -1)
			return;
		final String eventName = raw.substring(0, inIndex).trim();
		StageEvent scheduled = null;
		for (final StageEvent event : STAGE_EVENTS) {
			if (event.name.equalsIgnoreCase(eventName)) {
				scheduled = event;
				break;
			}
		}
		if (scheduled == null)
			return;
		elapsedTime = scheduled.time - secondsLeft;
		final SpawnResult emeraldResult = calculateSpawns(elapsedTime + 1, EMERALD_FIRST,
				EMERALD_UPGRADES, this::getEmeraldInterval);
		emeraldCount = emeraldResult.count;
		emeraldNext = emeraldResult.next;
		final SpawnResult diamondResult = calculateSpawns(elapsedTime + 1, DIAMOND_FIRST,
				DIAMOND_UPGRADES, this::getDiamondInterval);
		diamondCount = diamondResult.count;
		diamondNext = diamondResult.next;
	}

	private String formatTime(final int seconds) {
		final int minutes = seconds / 60;
		final int secs = seconds % 60;
		return String.format("%d:%02d", minutes, secs);
	}
}
