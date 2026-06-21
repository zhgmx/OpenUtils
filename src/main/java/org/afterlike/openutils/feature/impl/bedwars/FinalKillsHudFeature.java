package org.afterlike.openutils.feature.impl.bedwars;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.ReceiveChatEvent;
import org.afterlike.openutils.event.impl.RenderOverlayEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.feature.api.hud.HudFeature;
import org.afterlike.openutils.feature.api.hud.Position;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import org.afterlike.openutils.util.game.BedWarsUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.confikure.annotations.Button;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class FinalKillsHudFeature extends ToggleableFeature implements HudFeature {
	private final Position position = new Position(5, 50);
	@Option(name = "Enable Final Kills HUD",
			description = "Track final kills as a movable BedWars HUD list.", order = 0)
	public boolean enabled;
	@Option(name = "Drop shadow",
			description = "Draw final-kill lines with Minecraft's text shadow.", order = 1)
	public boolean dropShadow = true;
	@Option(name = "Show void kills", description = "Track final kills credited to the void.",
			order = 2)
	public boolean showVoidKills = true;
	@Option(name = "Teammates only",
			description = "Only count final kills from players on your team.", order = 3)
	public boolean teammatesOnly;
	private final Map<String, Integer> finalKills = new HashMap<>();
	private static final String VOID_KEY = "§8Void";
	private static final Pattern FINAL_KILL_MARKER_PATTERN = Pattern.compile(
			"(?:§[0-9a-fk-or]\\s*)*FINAL\\s+(?:§[0-9a-fk-or]\\s*)*KILL!", Pattern.CASE_INSENSITIVE);
	private static final Pattern NAME_CHUNK_PATTERN = Pattern.compile(
			"§([0-9a-f])(?:\\s*§[k-or])*\\s*([A-Za-z0-9_]{1,16})", Pattern.CASE_INSENSITIVE);
	private BedWarsUtil.TeamColor playerTeamColor = null;
	public FinalKillsHudFeature() {
		super("Final Kills HUD", FeatureCategory.BEDWARS);
	}

	@Subscribe
	private void onChatReceived(final ReceiveChatEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (GameModeUtil.getBedWarsStatus() != 3)
			return;
		final String message = event.getMessage();
		final Matcher finalKillMarker = FINAL_KILL_MARKER_PATTERN.matcher(message);
		if (!finalKillMarker.find())
			return;
		final String body = message.substring(0, finalKillMarker.start()).trim();
		final NameChunk nameChunk = findLastNameChunk(body);
		if (nameChunk == null) {
			ClientUtil.sendDebugMessage("no name chunks found in: " + body);
			return;
		}
		final String lastColorCode = nameChunk.colorCode;
		final String lastPlayerName = nameChunk.name;
		final String unformattedName = TextUtil.stripColorCodes(lastColorCode + lastPlayerName);
		if (unformattedName.equalsIgnoreCase("void")) {
			if (showVoidKills) {
				finalKills.put(VOID_KEY, finalKills.getOrDefault(VOID_KEY, 0) + 1);
				ClientUtil.sendDebugMessage("counted void kill: " + finalKills.get(VOID_KEY));
			}
			return;
		}
		if (teammatesOnly) {
			final BedWarsUtil.TeamColor killerTeamColor = BedWarsUtil.TeamColor
					.fromColorCode(lastColorCode);
			if (killerTeamColor != playerTeamColor) {
				ClientUtil.sendDebugMessage(
						"skipped non-teammate kill: " + lastColorCode + lastPlayerName);
				return;
			}
		}
		final String displayName = lastColorCode + lastPlayerName;
		finalKills.put(displayName, finalKills.getOrDefault(displayName, 0) + 1);
		ClientUtil.sendDebugMessage(
				"counted kill for " + displayName + ": " + finalKills.get(displayName));
	}

	@Subscribe
	private void onRender(final RenderOverlayEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (mc.gameSettings.showDebugInfo)
			return;
		if (GameModeUtil.getBedWarsStatus() != 3)
			return;
		if (finalKills.isEmpty())
			return;
		final int[] y = {position.getY()};
		finalKills.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
			if (!entry.getKey().equals(VOID_KEY) || showVoidKills) {
				final String displayName = entry.getKey().equals(VOID_KEY)
						? VOID_KEY
						: entry.getKey();
				final String line = "§r" + displayName + ": §f" + entry.getValue();
				mc.fontRendererObj.drawString(line, position.getX(), y[0], 0xFFFFFFFF,
						useHudDropShadow());
				y[0] += mc.fontRendererObj.FONT_HEIGHT + 2;
			}
		});
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		if (GameModeUtil.getBedWarsStatus() != 3) {
			resetTracking();
		} else if (ClientUtil.notNull() && playerTeamColor == null) {
			playerTeamColor = BedWarsUtil.getTeamColor(mc.thePlayer);
			if (playerTeamColor != null) {
				ClientUtil.sendDebugMessage(
						"detected team color: " + playerTeamColor.getDisplayName());
			}
		}
	}

	@Subscribe
	private void onWorldLoad(final WorldLoadEvent event) {
		resetTracking();
	}

	private void resetTracking() {
		finalKills.clear();
		playerTeamColor = null;
	}

	@Override
	protected void onEnable() {
		super.onEnable();
		resetTracking();
	}

	@Override
	protected void onDisable() {
		resetTracking();
		super.onDisable();
	}

	@Override
	public Position getHudPosition() {
		return position;
	}

	@Override
	public String getHudPlaceholderText() {
		return "Player:-Player:-Player:";
	}

	@Override
	public boolean useHudDropShadow() {
		return dropShadow;
	}

	@Button(name = "Layout Editor",
			description = "Open the layout editor with Final Kills selected.", label = "layout",
			order = 4)
	public void editPosition() {
		openHudEditor();
	}

	private static NameChunk findLastNameChunk(final String formattedText) {
		NameChunk lastNameChunk = null;
		final Matcher matcher = NAME_CHUNK_PATTERN.matcher(formattedText);
		while (matcher.find()) {
			final String colorCode = "§" + Character.toLowerCase(matcher.group(1).charAt(0));
			final String name = matcher.group(2);
			if (BedWarsUtil.TeamColor.fromColorCode(colorCode) != null) {
				lastNameChunk = new NameChunk(colorCode, name);
			}
		}
		return lastNameChunk;
	}
	private static final class NameChunk {
		private final String colorCode;
		private final String name;
		private NameChunk(final String colorCode, final String name) {
			this.colorCode = colorCode;
			this.name = name;
		}
	}
}
