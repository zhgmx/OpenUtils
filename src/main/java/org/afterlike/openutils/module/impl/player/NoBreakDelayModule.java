package org.afterlike.openutils.module.impl.player;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.setting.impl.DescriptionSetting;
import org.afterlike.openutils.module.api.setting.impl.NumberSetting;
import org.afterlike.openutils.platform.mixin.minecraft.client.multiplayer.PlayerControllerMPAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class NoBreakDelayModule extends Module {
	private final DescriptionSetting description;
	private final NumberSetting delay;
	public NoBreakDelayModule() {
		super("No Break Delay", ModuleCategory.PLAYER);
		description = this
				.registerSetting(new DescriptionSetting("Reduces the delay between block breaks"));
		delay = this.registerSetting(new NumberSetting("Delay (ticks)", 0, 0, 5, 1));
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		PlayerControllerMPAccessor accessor = (PlayerControllerMPAccessor) mc.playerController;
		if (accessor.ou$getBlockHitDelay() > delay.getInt()) {
			accessor.ou$setBlockHitDelay(delay.getInt());
		}
	}
}
