package org.afterlike.openutils.feature.impl.bedwars;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.util.EnumChatFormatting;
import org.afterlike.openutils.event.impl.ReceivePacketEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.game.BedWarsUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.confikure.annotations.Dropdown;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class ArmorAlertsFeature extends ToggleableFeature {
	@Option(name = "Enable Armor Alerts",
			description = "Call out enemy armor upgrades during BedWars games.", order = 0)
	public boolean enabled;
	@Option(name = "Ping sound", description = "Choose which armor purchases should play a ping.",
			order = 1)
	@Dropdown(values = {"None", "Diamond Only", "All"})
	public String pingSound = "All";
	@Option(name = "Chainmail armor", description = "Alert when an enemy upgrades to chainmail.",
			order = 2)
	public boolean detectChainmail = true;
	@Option(name = "Iron armor", description = "Alert when an enemy upgrades to iron.", order = 3)
	public boolean detectIron = true;
	@Option(name = "Diamond armor", description = "Alert when an enemy upgrades to diamond.",
			order = 4)
	public boolean detectDiamond = true;
	private final Map<String, ArmorType> playerArmor;
	private enum ArmorType {
		NONE, LEATHER, CHAINMAIL, IRON, DIAMOND
	}
	public ArmorAlertsFeature() {
		super("Armor Alerts", FeatureCategory.BEDWARS);
		playerArmor = new HashMap<>();
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
		if (packet.getEquipmentSlot() != 2) {
			return;
		}
		final ItemStack stack = packet.getItemStack();
		if (stack == null) {
			return;
		}
		final Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());
		if (!(entity instanceof EntityPlayer)) {
			return;
		}
		final EntityPlayer player = (EntityPlayer) entity;
		if (player == mc.thePlayer) {
			return;
		}
		if (BedWarsUtil.isTeammate(mc.thePlayer, player)) {
			return;
		}
		processArmor(player, stack);
	}

	private void processArmor(final EntityPlayer player, final ItemStack stack) {
		final ArmorType armorType = getArmorType(stack);
		if (armorType == ArmorType.NONE || armorType == ArmorType.LEATHER) {
			return;
		}
		if (!isArmorEnabled(armorType)) {
			return;
		}
		final ArmorType previous = playerArmor.getOrDefault(player.getName(), ArmorType.NONE);
		if (armorType.ordinal() <= previous.ordinal()) {
			return;
		}
		playerArmor.put(player.getName(), armorType);
		final String armorMessage = getArmorMessage(armorType);
		if (armorMessage == null) {
			return;
		}
		final String distance = getDistanceString(player);
		ClientUtil.sendMessage(
				player.getDisplayName().getFormattedText() + " §7bought " + armorMessage + " §7("
						+ EnumChatFormatting.AQUA + distance + EnumChatFormatting.GRAY + ")");
		playPingIfEnabled(armorType);
	}

	private void playPingIfEnabled(final ArmorType armorType) {
		final String mode = pingSound;
		if ("None".equalsIgnoreCase(mode)) {
			return;
		}
		if ("Diamond Only".equalsIgnoreCase(mode) && armorType != ArmorType.DIAMOND) {
			return;
		}
		mc.thePlayer.playSound("random.orb", 1.0F, 1.0F);
	}

	private ArmorType getArmorType(final ItemStack stack) {
		if (!(stack.getItem() instanceof ItemArmor)) {
			return ArmorType.NONE;
		}
		final ItemArmor armor = (ItemArmor) stack.getItem();
		if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
			return ArmorType.LEATHER;
		}
		if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.CHAIN) {
			return ArmorType.CHAINMAIL;
		}
		if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.IRON) {
			return ArmorType.IRON;
		}
		if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.DIAMOND) {
			return ArmorType.DIAMOND;
		}
		return ArmorType.NONE;
	}

	private boolean isArmorEnabled(final ArmorType armorType) {
		switch (armorType) {
			case CHAINMAIL :
				return detectChainmail;
			case IRON :
				return detectIron;
			case DIAMOND :
				return detectDiamond;
			default :
				return false;
		}
	}

	private String getArmorMessage(final ArmorType armorType) {
		switch (armorType) {
			case CHAINMAIL :
				return "§fChainmail Armor";
			case IRON :
				return "§fIron Armor";
			case DIAMOND :
				return "§bDiamond Armor";
			default :
				return null;
		}
	}

	private String getDistanceString(final EntityPlayer player) {
		return new DecimalFormat("#").format(player.getDistanceToEntity(mc.thePlayer)) + "m";
	}

	@Subscribe
	private void onWorldLoad(final WorldLoadEvent event) {
		playerArmor.clear();
	}

	@Override
	protected void onEnable() {
		playerArmor.clear();
	}

	@Override
	protected void onDisable() {
		playerArmor.clear();
	}
}
