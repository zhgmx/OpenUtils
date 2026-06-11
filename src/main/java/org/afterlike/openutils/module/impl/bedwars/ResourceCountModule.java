package org.afterlike.openutils.module.impl.bedwars;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.setting.impl.BooleanSetting;
import org.afterlike.openutils.module.api.setting.impl.DescriptionSetting;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.fastbus.Subscribe;

// TODO: rewrite - currently a direct port from meowtils
public class ResourceCountModule extends Module {
	private final DescriptionSetting description;
	private final BooleanSetting trackIron;
	private final BooleanSetting trackGold;
	private final BooleanSetting trackDiamonds;
	private final BooleanSetting trackEmeralds;
	private final BooleanSetting pingSound;
	public ResourceCountModule() {
		super("Resource Count", ModuleCategory.BEDWARS);
		description = this
				.registerSetting(new DescriptionSetting("Tracks resources in your inventory"));
		trackIron = this.registerSetting(new BooleanSetting("Track §fIron", false));
		trackGold = this.registerSetting(new BooleanSetting("Track §6Gold", false));
		trackDiamonds = this.registerSetting(new BooleanSetting("Track §bDiamonds", true));
		trackEmeralds = this.registerSetting(new BooleanSetting("Track §2Emeralds", true));
		pingSound = this.registerSetting(new BooleanSetting("Ping Sound", false));
	}
	private final Map<Item, Integer> lastCounts = new HashMap<>();
	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		if (!GameModeUtil.onHypixel())
			return;
		if (GameModeUtil.getBedWarsStatus() != 3)
			return;
		if (!ClientUtil.notNull())
			return;
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
			if (pingSound.getValue()) {
				mc.thePlayer.playSound("random.orb", 1.0F, 0.8F);
			}
		}
	}

	private boolean isTracking(final Item item) {
		if (item == Items.iron_ingot)
			return trackIron.getValue();
		if (item == Items.gold_ingot)
			return trackGold.getValue();
		if (item == Items.diamond)
			return trackDiamonds.getValue();
		if (item == Items.emerald)
			return trackEmeralds.getValue();
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
		initCounts(lastCounts);
	}

	@Override
	protected void onDisable() {
		resetCounts();
	}
}
