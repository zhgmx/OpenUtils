package org.afterlike.openutils.feature.impl.bedwars;

import java.util.ArrayList;
import java.util.List;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.ReceiveChatEvent;
import org.afterlike.openutils.event.impl.RenderOverlayEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.feature.api.hud.HudFeature;
import org.afterlike.openutils.feature.api.hud.Position;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class UpgradesHudFeature extends ToggleableFeature implements HudFeature {
	private final Position position = new Position(5, 100);
	@Option(name = "Enable Upgrades HUD",
			description = "Show purchased team upgrades as a movable BedWars HUD.", order = 0)
	public boolean enabled;
	@Option(name = "Drop shadow", description = "Draw upgrade lines with Minecraft's text shadow.",
			order = 1)
	public boolean dropShadow = true;
	@Option(name = "Show traps", description = "Show queued trap status.", order = 2)
	public boolean showTrap = true;
	@Option(name = "Show sharpness", description = "Show whether sharpness has been purchased.",
			order = 3)
	public boolean showSharp = true;
	@Option(name = "Show protection", description = "Show the current protection tier.", order = 4)
	public boolean showProt = true;
	private final List<String> trapQueue = new ArrayList<>();
	private final List<String> protUpgrades = new ArrayList<>();
	private final List<String> sharpUpgrades = new ArrayList<>();
	public UpgradesHudFeature() {
		super("Upgrades HUD", FeatureCategory.BEDWARS);
	}

	@Subscribe
	private void onChatReceived(final ReceiveChatEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (!GameModeUtil.isInBedWarsGame())
			return;
		final String message = TextUtil.stripColorCodes(event.getMessage());
		if (message.contains(":"))
			return;
		if (message.contains("Trap was set off!") || message.contains("Trap from the queue!")) {
			if (!trapQueue.isEmpty()) {
				trapQueue.remove(0);
			}
			return;
		}
		if (!message.contains("purchased")) {
			return;
		}
		final String purchasedPattern = message
				.substring(message.indexOf("purchased") + "purchased".length());
		if (message.contains("Trap")) {
			final String trap = purchasedPattern.replace("Trap", "").trim();
			trapQueue.add(trap);
		} else if (message.contains("Reinforced Armor")) {
			protUpgrades.clear();
			final String armor = purchasedPattern.replace("Reinforced Armor", "").trim();
			protUpgrades.add(armor);
		} else if (message.contains("Sharpened Swords")) {
			sharpUpgrades.clear();
			final String sharp = purchasedPattern.replace("Sharpened Swords", "").trim();
			sharpUpgrades.add(sharp);
		}
	}

	@Subscribe
	private void onRender(final RenderOverlayEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (mc.gameSettings.showDebugInfo)
			return;
		if (!GameModeUtil.isInBedWarsGame())
			return;
		final int x = position.getX(getHudPreviewWidth());
		int y = position.getY(getHudPreviewHeight());
		int delta = 0;
		for (final String line : buildLines()) {
			mc.fontRendererObj.drawString(line, x, y, 0xFFFFFFFF, useHudDropShadow());
			y += mc.fontRendererObj.FONT_HEIGHT + 2;
			delta -= 90;
		}
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		if (!GameModeUtil.isInBedWarsGame()) {
			resetTracking();
		}
	}

	private List<String> buildLines() {
		final List<String> lines = new ArrayList<>();
		if (showTrap) {
			if (trapQueue.isEmpty()) {
				lines.add("Trap: §cNone");
			} else {
				lines.add("Trap: §a" + trapQueue.get(0));
			}
		}
		if (showSharp) {
			if (sharpUpgrades.isEmpty()) {
				lines.add("Sharp: §cNone");
			} else {
				lines.add("Sharp: §aI");
			}
		}
		if (showProt) {
			if (protUpgrades.isEmpty()) {
				lines.add("Prot: §cNone");
			} else {
				lines.add("Prot: §a" + protUpgrades.get(0));
			}
		}
		return lines;
	}

	private void resetTracking() {
		trapQueue.clear();
		protUpgrades.clear();
		sharpUpgrades.clear();
	}

	@Override
	protected void onEnable() {
		resetTracking();
	}

	@Override
	protected void onDisable() {
		resetTracking();
	}

	@Override
	public Position getHudPosition() {
		return position;
	}

	@Override
	public String[] getHudPreviewLines() {
		final List<String> lines = new ArrayList<>();
		if (showTrap) {
			lines.add("Trap: §aMiner Fatigue");
		}
		if (showSharp) {
			lines.add("Sharp: §aI");
		}
		if (showProt) {
			lines.add("Prot: §aII");
		}
		return lines.toArray(new String[0]);
	}

	@Override
	public boolean useHudDropShadow() {
		return dropShadow;
	}
}
