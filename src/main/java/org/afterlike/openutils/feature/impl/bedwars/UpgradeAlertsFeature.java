package org.afterlike.openutils.feature.impl.bedwars;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import org.afterlike.openutils.event.impl.ReceivePacketEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.game.BedWarsUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class UpgradeAlertsFeature extends ToggleableFeature {
	@Option(name = "Enable Upgrade Alerts",
			description = "Call out enemy sword and protection upgrades during BedWars games.",
			order = 0)
	public boolean enabled;
	@Option(name = "Ping sound",
			description = "Play a ping when an enemy team upgrade is detected.", order = 1)
	public boolean pingSound = true;
	private final Map<String, Set<UpgradeType>> teamUpgrades;
	public UpgradeAlertsFeature() {
		super("Upgrade Alerts", FeatureCategory.BEDWARS);
		teamUpgrades = new HashMap<>();
	}

	@Subscribe
	private void onPacket(final ReceivePacketEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (!GameModeUtil.isInBedWarsGame())
			return;
		if (!(event.getPacket() instanceof S04PacketEntityEquipment))
			return;
		final S04PacketEntityEquipment packet = (S04PacketEntityEquipment) event.getPacket();
		final ItemStack stack = packet.getItemStack();
		if (stack == null || !stack.isItemEnchanted())
			return;
		final Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());
		if (!(entity instanceof EntityPlayer))
			return;
		final EntityPlayer player = (EntityPlayer) entity;
		if (player == mc.thePlayer)
			return;
		if (BedWarsUtil.isTeammate(mc.thePlayer, player)) {
			return;
		}
		final String teamKey = BedWarsUtil.getFormattedTeamName(player);
		if (teamKey == null)
			return;
		final Set<UpgradeType> upgrades = teamUpgrades.computeIfAbsent(teamKey,
				k -> EnumSet.noneOf(UpgradeType.class));
		if (packet.getEquipmentSlot() == 0 && stack.getItem() instanceof ItemSword) {
			if (upgrades.add(UpgradeType.SHARPENED_SWORDS)) {
				notifyUpgrade(teamKey, UpgradeType.SHARPENED_SWORDS.getLabel());
			}
		} else if (packet.getEquipmentSlot() == 3 && stack.getItem() instanceof ItemArmor) {
			if (upgrades.add(UpgradeType.REINFORCED_ARMOR)) {
				notifyUpgrade(teamKey, UpgradeType.REINFORCED_ARMOR.getLabel());
			}
		}
	}

	@Subscribe
	private void onWorldLoad(final WorldLoadEvent event) {
		teamUpgrades.clear();
	}

	private void notifyUpgrade(final String teamKey, final String upgrade) {
		ClientUtil.sendMessage(teamKey + " Team §7purchased §b" + upgrade);
		if (pingSound) {
			mc.thePlayer.playSound("random.orb", 1.0F, 1.0F);
		}
	}

	@Override
	protected void onEnable() {
		teamUpgrades.clear();
	}

	@Override
	protected void onDisable() {
		teamUpgrades.clear();
	}
	private enum UpgradeType {
		SHARPENED_SWORDS("Sharpened Swords"), REINFORCED_ARMOR("Reinforced Armor");
		private final String label;
		UpgradeType(final String label) {
			this.label = label;
		}

		private String getLabel() {
			return label;
		}
	}
}
