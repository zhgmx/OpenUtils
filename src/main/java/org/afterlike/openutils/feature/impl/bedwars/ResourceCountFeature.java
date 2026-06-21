package org.afterlike.openutils.feature.impl.bedwars;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class ResourceCountFeature extends ToggleableFeature {
	@Option(name = "Enable Resource Count",
			description = "Notify when tracked Bed Wars resources change in your inventory.",
			order = 0)
	public boolean enabled;
	@Option(name = "Track iron", description = "Notify when your iron count changes.", order = 1)
	public boolean trackIron;
	@Option(name = "Track gold", description = "Notify when your gold count changes.", order = 2)
	public boolean trackGold;
	@Option(name = "Track diamonds", description = "Notify when your diamond count changes.",
			order = 3)
	public boolean trackDiamonds = true;
	@Option(name = "Track emeralds", description = "Notify when your emerald count changes.",
			order = 4)
	public boolean trackEmeralds = true;
	@Option(name = "Ping sound", description = "Play a ping when a tracked resource changes.",
			order = 5)
	public boolean pingSound;
	public ResourceCountFeature() {
		super("Resource Count", FeatureCategory.BEDWARS);
	}
	private final Map<Item, Integer> lastCounts = new HashMap<>();
	private boolean initialized;
	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		if (!GameModeUtil.onHypixel() || !GameModeUtil.isInBedWarsGame() || !ClientUtil.notNull()) {
			resetCounts();
			return;
		}
		Map<Item, Integer> current = new HashMap<>();
		initCounts(current);
		for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
			if (stack == null)
				continue;
			Item item = stack.getItem();
			if (!current.containsKey(item))
				continue;
			current.put(item, current.get(item) + stack.stackSize);
		}
		if (!initialized) {
			lastCounts.clear();
			lastCounts.putAll(current);
			initialized = true;
			return;
		}
		handleChanges(current);
		lastCounts.clear();
		lastCounts.putAll(current);
	}

	private void handleChanges(final Map<Item, Integer> current) {
		for (Map.Entry<Item, Integer> entry : current.entrySet()) {
			Item item = entry.getKey();
			int newCount = entry.getValue();
			int oldCount = lastCounts.getOrDefault(item, 0);
			if (newCount == oldCount)
				continue;
			if (!isTracking(item))
				continue;
			boolean gained = newCount > oldCount;
			String prefix = gained
					? EnumChatFormatting.GREEN + "[+] "
					: EnumChatFormatting.RED + "[-] ";
			ClientUtil.sendMessage(prefix + getItemDisplayName(item) + EnumChatFormatting.GRAY
					+ " (" + newCount + ")");
			if (pingSound) {
				mc.thePlayer.playSound("random.orb", 1.0F, 0.8F);
			}
		}
	}

	private boolean isTracking(final Item item) {
		if (item == Items.iron_ingot)
			return trackIron;
		if (item == Items.gold_ingot)
			return trackGold;
		if (item == Items.diamond)
			return trackDiamonds;
		if (item == Items.emerald)
			return trackEmeralds;
		return false;
	}

	private String getItemDisplayName(final Item item) {
		if (item == Items.iron_ingot)
			return EnumChatFormatting.WHITE + "Iron";
		if (item == Items.gold_ingot)
			return EnumChatFormatting.GOLD + "Gold";
		if (item == Items.diamond)
			return EnumChatFormatting.AQUA + "Diamond";
		if (item == Items.emerald)
			return EnumChatFormatting.DARK_GREEN + "Emerald";
		return "Unknown";
	}

	private void initCounts(final Map<Item, Integer> map) {
		map.put(Items.iron_ingot, 0);
		map.put(Items.gold_ingot, 0);
		map.put(Items.diamond, 0);
		map.put(Items.emerald, 0);
	}

	private void resetCounts() {
		lastCounts.clear();
		initialized = false;
	}

	@Subscribe
	private void onWorldLoad(final WorldLoadEvent event) {
		resetCounts();
	}

	@Override
	protected void onDisable() {
		resetCounts();
	}
}
