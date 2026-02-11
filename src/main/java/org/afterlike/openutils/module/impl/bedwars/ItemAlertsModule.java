package org.afterlike.openutils.module.impl.bedwars;

import java.text.DecimalFormat;
import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.util.EnumChatFormatting;
import org.afterlike.openutils.event.handler.EventHandler;
import org.afterlike.openutils.event.impl.ReceivePacketEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.setting.impl.BooleanSetting;
import org.afterlike.openutils.module.api.setting.impl.DescriptionSetting;
import org.afterlike.openutils.module.api.setting.impl.ModeSetting;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import org.afterlike.openutils.util.game.BedWarsUtil;
import org.afterlike.openutils.util.game.GameModeUtil;

// TODO: we should probably rewrite at some point...
public class ItemAlertsModule extends Module {
	private static final long COOLDOWN_TIME = 10_000L;
	private final Item potionJump, potionSpeed, potionInvis, machineGunBow, charlieUnicorn,
			iceBridge, sleepingDust, devastatorBow, miracleStars, bridgeZapper, shuriken,
			blockShuffler, unstableTeleport, snowman, popupTower, dreamDefender, megaTnt,
			windCharge;
	private final DescriptionSetting desc;
	private final ModeSetting pingSound;
	private final BooleanSetting detectIronSword, detectDiamondSword, detectFireball,
			detectKnockbackStick, detectBow, detectTnt, detectObsidian, detectPearl,
			detectBridgeEgg, detectPotions, detectGoldenApple, detectWaterBucket, detectGoldPickaxe,
			detectDiamondPickaxe, detectIronGolem, detectBedbug, detectPopupTower,
			detectSpecialItems;
	private final Map<Item, ItemRule> rulesByItem;
	private final Map<String, AlertState> alerts;
	private final List<ItemRule> rules;
	public ItemAlertsModule() {
		super("Item Alerts", ModuleCategory.BEDWARS);
		potionJump = new Item();
		potionSpeed = new Item();
		potionInvis = new Item();
		machineGunBow = new Item();
		charlieUnicorn = new Item();
		iceBridge = new Item();
		sleepingDust = new Item();
		devastatorBow = new Item();
		miracleStars = new Item();
		bridgeZapper = new Item();
		shuriken = new Item();
		blockShuffler = new Item();
		unstableTeleport = new Item();
		snowman = new Item();
		popupTower = new Item();
		dreamDefender = new Item();
		megaTnt = new Item();
		windCharge = new Item();
		desc = this
				.registerSetting(new DescriptionSetting("Alerts you when players purchase items"));
		pingSound = this
				.registerSetting(new ModeSetting("Ping Sound", "All", "None", "Important", "All"));
		detectIronSword = this.registerSetting(new BooleanSetting("§fIron Sword", true));
		detectDiamondSword = this.registerSetting(new BooleanSetting("§bDiamond Sword", true));
		detectFireball = this.registerSetting(new BooleanSetting("§cFireball", true));
		detectKnockbackStick = this.registerSetting(new BooleanSetting("§dKnockback Stick", true));
		detectBow = this.registerSetting(new BooleanSetting("§2Bow", true));
		detectTnt = this.registerSetting(new BooleanSetting("§cT§fN§cT", true));
		detectObsidian = this.registerSetting(new BooleanSetting("§5Obsidian", true));
		detectPearl = this.registerSetting(new BooleanSetting("§3Ender Pearl", true));
		detectBridgeEgg = this.registerSetting(new BooleanSetting("§eBridge Egg", true));
		detectPotions = this.registerSetting(new BooleanSetting("§bPotions", true));
		detectGoldenApple = this.registerSetting(new BooleanSetting("§6Golden Apple", true));
		detectWaterBucket = this.registerSetting(new BooleanSetting("§bWater Bucket", true));
		detectGoldPickaxe = this.registerSetting(new BooleanSetting("§6Gold Pickaxe", true));
		detectDiamondPickaxe = this.registerSetting(new BooleanSetting("§bDiamond Pickaxe", true));
		detectIronGolem = this.registerSetting(new BooleanSetting("§fDream Defender", true));
		detectBedbug = this.registerSetting(new BooleanSetting("§fBedbug", true));
		detectPopupTower = this.registerSetting(new BooleanSetting("§6Pop-up Tower", true));
		detectSpecialItems = this.registerSetting(new BooleanSetting("§dRotation Items", true));
		rulesByItem = new HashMap<>();
		alerts = new HashMap<>();
		rules = new ArrayList<>();
		// build rules
		addRule(new ItemRule(Items.iron_sword, "§fIron Sword", false, detectIronSword));
		addRule(new ItemRule(Items.diamond_sword, "§bDiamond Sword", true, detectDiamondSword));
		addRule(new ItemRule(Items.fire_charge, "§cFireball", false, detectFireball));
		addRule(new ItemRule(Items.stick, "§dKnockback Stick", false, detectKnockbackStick));
		addRule(new ItemRule(Items.bow, "§2Bow", false, detectBow));
		addRule(new ItemRule(Item.getItemFromBlock(Blocks.tnt), "§cT§fN§cT", false, detectTnt));
		addRule(new ItemRule(Items.golden_apple, "§6Golden Apple", false, detectGoldenApple));
		addRule(new ItemRule(Items.water_bucket, "§bWater Bucket", false, detectWaterBucket));
		addRule(new ItemRule(Items.golden_pickaxe, "§6Gold Pickaxe", false, detectGoldPickaxe));
		addRule(new ItemRule(Items.diamond_pickaxe, "§bDiamond Pickaxe", false,
				detectDiamondPickaxe));
		addRule(new ItemRule(Items.snowball, "§fBedbug", false, detectBedbug));
		addRule(new ItemRule(Items.milk_bucket, "§fMilk", false, detectPotions));
		addRule(new ItemRule(Items.ender_pearl, "§3Ender Pearl", true, detectPearl));
		addRule(new ItemRule(Items.egg, "§eBridge Egg", true, detectBridgeEgg));
		addRule(new ItemRule(Item.getItemFromBlock(Blocks.obsidian), "§5Obsidian", true,
				detectObsidian));
		// potions (display-name substring)
		addRule(new ItemRule(potionJump, "§aJump Boost Potion", true, detectPotions,
				MatchType.POTION_SUBSTRING, "jump"));
		addRule(new ItemRule(potionSpeed, "§bSpeed Potion", true, detectPotions,
				MatchType.POTION_SUBSTRING, "speed"));
		addRule(new ItemRule(potionInvis, "§fInvisibility Potion", true, detectPotions,
				MatchType.POTION_SUBSTRING, "invisibility"));
		// specials (display-name substring)
		addRule(new ItemRule(popupTower, "§6Pop-up Tower", true, detectPopupTower,
				MatchType.NAME_SUBSTRING, "pop-up tower"));
		addRule(new ItemRule(dreamDefender, "§fDream Defender", true, detectIronGolem,
				MatchType.NAME_SUBSTRING, "dream defender"));
		// rotation items (share rotation setting, not important)
		addRule(new ItemRule(machineGunBow, "§4Machine Gun Bow", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "machine gun bow"));
		addRule(new ItemRule(charlieUnicorn, "§dCharlie the Unicorn", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "charlie"));
		addRule(new ItemRule(iceBridge, "§bIce Bridge", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "ice bridge"));
		addRule(new ItemRule(sleepingDust, "§cSleeping Dust", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "sleeping dust"));
		addRule(new ItemRule(devastatorBow, "§2Devastator Bow", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "devastator"));
		addRule(new ItemRule(miracleStars, "§eMiracle of the Stars", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "miracle"));
		addRule(new ItemRule(bridgeZapper, "§2Bridge Zapper", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "bridge zapper"));
		addRule(new ItemRule(shuriken, "§fShuriken", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "shuriken"));
		addRule(new ItemRule(blockShuffler, "§8Block Shuffler", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "block shuffler"));
		addRule(new ItemRule(unstableTeleport, "§cUnstable Teleportation Device", false,
				detectSpecialItems, MatchType.NAME_SUBSTRING, "unstable teleportation device"));
		addRule(new ItemRule(snowman, "§6Snowman", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "snowman"));
		addRule(new ItemRule(megaTnt, "§fMega §cT§fN§cT", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "mega tnt"));
		addRule(new ItemRule(windCharge, "§fWind Charge", false, detectSpecialItems,
				MatchType.NAME_SUBSTRING, "wind charge"));
	}

	private void addRule(final ItemRule rule) {
		rules.add(rule);
		rulesByItem.put(rule.item, rule);
	}

	@EventHandler
	private void onPacket(final ReceivePacketEvent event) {
		if (!ClientUtil.notNull()) {
			return;
		}
		if (GameModeUtil.getBedWarsStatus() != 3) {
			return;
		}
		if (!(event.getPacket() instanceof S04PacketEntityEquipment)) {
			return;
		}
		final S04PacketEntityEquipment packet = (S04PacketEntityEquipment) event.getPacket();
		if (packet.getEquipmentSlot() != 0) {
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
		processItem(player, stack);
	}

	private void processItem(final EntityPlayer player, final ItemStack stack) {
		final Item item = getEffectiveItem(stack);
		final ItemRule rule = rulesByItem.get(item);
		if (rule == null)
			return;
		if (!rule.setting.getValue())
			return;
		final String displayName = player.getDisplayName().getFormattedText();
		final long now = System.currentTimeMillis();
		final AlertState state = alerts.computeIfAbsent(player.getName(), k -> new AlertState());
		if (state.lastItem == item && (now - state.lastAlertTime) < COOLDOWN_TIME) {
			return;
		}
		final String itemMessage = createItemMessage(player, item, stack);
		ClientUtil.sendMessage(displayName + " §7has " + itemMessage);
		if (shouldPlaySound(item)) {
			mc.thePlayer.playSound("random.orb", 1.0F, 1.0F);
		}
		state.lastItem = item;
		state.lastAlertTime = now;
	}

	private Item getEffectiveItem(final ItemStack stack) {
		final Item rawItem = stack.getItem();
		final String displayName = TextUtil.stripColorCodes(stack.getDisplayName())
				.toLowerCase(Locale.getDefault());
		if (rawItem instanceof ItemPotion) {
			for (final ItemRule rule : rules) {
				if (rule.matchType == MatchType.POTION_SUBSTRING
						&& rule.matches(rawItem, displayName)) {
					return rule.item;
				}
			}
		}
		for (final ItemRule rule : rules) {
			if (rule.matchType == MatchType.NAME_SUBSTRING && rule.matches(rawItem, displayName)) {
				return rule.item;
			}
		}
		return rawItem;
	}

	private String createItemMessage(final EntityPlayer player, final Item item,
			final ItemStack stack) {
		if (item == Items.bow && stack.isItemEnchanted()) {
			final NBTTagList enchantments = stack.getEnchantmentTagList();
			final int enchantmentCount = enchantments != null ? enchantments.tagCount() : 0;
			if (enchantmentCount == 1) {
				return "§dPower Bow §7(" + EnumChatFormatting.AQUA + getDistanceString(player)
						+ EnumChatFormatting.GRAY + ")";
			} else if (enchantmentCount == 2) {
				return "§6Punch Bow §7(" + EnumChatFormatting.AQUA + getDistanceString(player)
						+ EnumChatFormatting.GRAY + ")";
			}
		}
		final ItemRule rule = rulesByItem.get(item);
		final String base = rule != null ? rule.message : null;
		if (base == null) {
			return "§fUnknown Item §7(" + EnumChatFormatting.AQUA + getDistanceString(player)
					+ EnumChatFormatting.GRAY + ")";
		}
		return base + " §7(" + EnumChatFormatting.AQUA + getDistanceString(player)
				+ EnumChatFormatting.GRAY + ")";
	}

	private String getDistanceString(final EntityPlayer player) {
		final double distance = player.getDistanceToEntity(mc.thePlayer);
		return new DecimalFormat("#").format(distance) + "m";
	}

	private boolean shouldPlaySound(final Item item) {
		final String mode = pingSound.getValue();
		if ("None".equalsIgnoreCase(mode)) {
			return false;
		}
		if ("Important".equalsIgnoreCase(mode)) {
			final ItemRule rule = rulesByItem.get(item);
			return rule != null && rule.important;
		}
		return true;
	}

	@EventHandler
	private void onWorldLoad(final WorldLoadEvent event) {
		alerts.clear();
	}

	@Override
	protected void onEnable() {
		alerts.clear();
	}

	@Override
	protected void onDisable() {
		alerts.clear();
	}
	private enum MatchType {
		EXACT, NAME_SUBSTRING, POTION_SUBSTRING
	}
	private static final class ItemRule {
		private final Item item;
		private final String message;
		private final boolean important;
		private final BooleanSetting setting;
		private final MatchType matchType;
		private final String matchText;
		ItemRule(final Item item, final String message, final boolean important,
				final BooleanSetting setting) {
			this(item, message, important, setting, MatchType.EXACT, null);
		}

		ItemRule(final Item item, final String message, final boolean important,
				final BooleanSetting setting, final MatchType matchType, final String matchText) {
			this.item = item;
			this.message = message;
			this.important = important;
			this.setting = setting;
			this.matchType = matchType;
			this.matchText = matchText;
		}

		boolean matches(final Item rawItem, final String lowerName) {
			if (matchType == MatchType.EXACT) {
				return rawItem == item;
			}
			return matchText == null || lowerName.contains(matchText);
		}
	}
	private static final class AlertState {
		private long lastAlertTime;
		private Item lastItem;
	}
}
