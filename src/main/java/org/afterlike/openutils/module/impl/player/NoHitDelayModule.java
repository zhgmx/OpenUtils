package org.afterlike.openutils.module.impl.player;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.platform.mixin.minecraft.client.MinecraftAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class NoHitDelayModule extends Module {
	public NoHitDelayModule() {
		super("No Hit Delay", ModuleCategory.PLAYER);
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		MinecraftAccessor accessor = (MinecraftAccessor) mc;
		accessor.ou$setLeftClickCounter(0);
	}
}
