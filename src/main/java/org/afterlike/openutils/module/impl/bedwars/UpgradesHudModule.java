package org.afterlike.openutils.module.impl.bedwars;

import java.util.ArrayList;
import java.util.List;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.ReceiveChatEvent;
import org.afterlike.openutils.event.impl.RenderOverlayEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.hud.HudModule;
import org.afterlike.openutils.module.api.hud.Position;
import org.afterlike.openutils.module.api.setting.Setting;
import org.afterlike.openutils.module.api.setting.impl.BooleanSetting;
import org.afterlike.openutils.module.api.setting.impl.DescriptionSetting;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.fastbus.Subscribe;

public class UpgradesHudModule extends Module implements HudModule {
	private final Position position = new Position(5, 100);
	private final DescriptionSetting disclaimer;
	private final BooleanSetting editPosition;
	private final BooleanSetting dropShadow;
	private final BooleanSetting showTrap;
	private final BooleanSetting showSharp;
	private final BooleanSetting showProt;
	private final List<String> trapQueue = new ArrayList<>();
	private final List<String> protUpgrades = new ArrayList<>();
	private final List<String> sharpUpgrades = new ArrayList<>();
	public UpgradesHudModule() {
		super("Upgrades HUD", ModuleCategory.BEDWARS);
		disclaimer = this
				.registerSetting(new DescriptionSetting("Hypixel language must be ENGLISH!"));
		editPosition = this.registerSetting(new BooleanSetting("Edit position", false));
		dropShadow = this.registerSetting(new BooleanSetting("Drop shadow", true));
		showTrap = this.registerSetting(new BooleanSetting("Show trap", true));
		showSharp = this.registerSetting(new BooleanSetting("Show sharpness", true));
		showProt = this.registerSetting(new BooleanSetting("Show protection", true));
	}

	@Subscribe
	private void onChatReceived(final ReceiveChatEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (GameModeUtil.getBedWarsStatus() != 3)
			return;
		final String message = TextUtil.stripColorCodes(event.getMessage());
		if (message.contains(":"))
			return;
		final String purchasedPattern = message
				.substring(message.indexOf("purchased") + "purchased".length());
		if (message.contains("purchased") && message.contains("Trap")) {
			final String trap = purchasedPattern.replace("Trap", "").trim();
			trapQueue.add(trap);
		} else if (message.contains("purchased") && message.contains("Reinforced Armor")) {
			protUpgrades.clear();
			final String armor = purchasedPattern.replace("Reinforced Armor", "").trim();
			protUpgrades.add(armor);
		} else if (message.contains("purchased") && message.contains("Sharpened Swords")) {
			sharpUpgrades.clear();
			final String sharp = purchasedPattern.replace("Sharpened Swords", "").trim();
			sharpUpgrades.add(sharp);
		} else if (message.contains("Trap was set off!")) {
			if (!trapQueue.isEmpty()) {
				trapQueue.remove(0);
			}
		} else if (message.contains("Trap from the queue!")) {
			if (!trapQueue.isEmpty()) {
				trapQueue.remove(0);
			}
		}
	}

	@Subscribe
	private void onRender(final RenderOverlayEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (mc.gameSettings.showDebugInfo)
			return;
		if (GameModeUtil.getBedWarsStatus() != 3)
			return;
		int y = position.getY();
		int delta = 0;
		for (final String line : buildLines()) {
			mc.fontRendererObj.drawString(line, position.getX(), y, 0xFFFFFFFF, useHudDropShadow());
			y += mc.fontRendererObj.FONT_HEIGHT + 2;
			delta -= 90;
		}
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		if (GameModeUtil.getBedWarsStatus() != 3) {
			resetTracking();
		}
	}

	private List<String> buildLines() {
		final List<String> lines = new ArrayList<>();
		if (showTrap.getValue()) {
			if (trapQueue.isEmpty()) {
				lines.add("Trap: §cNone");
			} else {
				lines.add("Trap: §a" + trapQueue.get(0));
			}
		}
		if (showSharp.getValue()) {
			if (sharpUpgrades.isEmpty()) {
				lines.add("Sharp: §cNone");
			} else {
				lines.add("Sharp: §aI");
			}
		}
		if (showProt.getValue()) {
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
	public void onSettingChanged(final Setting<?> setting) {
		handleHudSettingChanged(setting);
		super.onSettingChanged(setting);
	}

	@Override
	public Position getHudPosition() {
		return position;
	}

	@Override
	public BooleanSetting getHudEditSetting() {
		return editPosition;
	}

	@Override
	public String getHudPlaceholderText() {
		return "Trap:-Sharp:-Prot:";
	}

	@Override
	public boolean useHudDropShadow() {
		return dropShadow.getValue();
	}
}
