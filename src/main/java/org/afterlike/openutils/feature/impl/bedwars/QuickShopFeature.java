package org.afterlike.openutils.feature.impl.bedwars;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import org.afterlike.openutils.event.impl.WindowClickEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class QuickShopFeature extends ToggleableFeature {
	@Option(name = "Enable Quick Shop",
			description = "Use shift-click behavior for BedWars shop purchases.", order = 0)
	public boolean enabled;
	public QuickShopFeature() {
		super("Quick Shop", FeatureCategory.BEDWARS);
	}
	private static final Set<String> SHOP_TITLES = new HashSet<>(
			Arrays.asList("quick buy", "upgrades & traps", "blocks", "melee", "armor", "tools",
					"ranged", "potions", "utility", "rotating items"));
	@Subscribe
	private void onWindowClick(final WindowClickEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (!GameModeUtil.isInBedWarsGame())
			return;
		Container container = event.getContainer().inventorySlots;
		if (!(container instanceof ContainerChest))
			return;
		IInventory lower = ((ContainerChest) container).getLowerChestInventory();
		String title = TextUtil.stripColorCodes(lower.getDisplayName().getUnformattedText())
				.toLowerCase();
		boolean isShop = false;
		for (String s : SHOP_TITLES) {
			if (title.contains(s)) {
				isShop = true;
				break;
			}
		}
		if (!isShop)
			return;
		if (event.getClickedButton() != 0)
			return;
		if (event.getSlotId() < 0)
			return;
		event.setCancelled(true);
		mc.playerController.windowClick(container.windowId, event.getSlotId(), 2, 3, mc.thePlayer);
	}
}
