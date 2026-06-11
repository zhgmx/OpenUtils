package org.afterlike.openutils.module.impl.movement;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.setting.impl.BooleanSetting;
import org.afterlike.openutils.platform.mixin.minecraft.entity.EntityLivingBaseAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class NoJumpDelayModule extends Module {
	private final BooleanSetting onlyWhileMoving;
	public NoJumpDelayModule() {
		super("No Jump Delay", ModuleCategory.MOVEMENT);
		onlyWhileMoving = this.registerSetting(new BooleanSetting("Only while moving", false));
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.PRE)
			return;
		if (!ClientUtil.notNull())
			return;
		if (onlyWhileMoving.getValue()) {
			if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0)
				return;
		}
		EntityLivingBaseAccessor accessor = (EntityLivingBaseAccessor) mc.thePlayer;
		accessor.ou$setJumpTicks(0);
	}
}