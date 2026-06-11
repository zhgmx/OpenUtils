package org.afterlike.openutils.module.handler;

import java.util.*;
import net.minecraft.client.Minecraft;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.event.impl.KeyPressEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.impl.bedwars.*;
import org.afterlike.openutils.module.impl.client.*;
import org.afterlike.openutils.module.impl.hypixel.*;
import org.afterlike.openutils.module.impl.movement.*;
import org.afterlike.openutils.module.impl.player.*;
import org.afterlike.openutils.module.impl.render.*;
import org.afterlike.openutils.module.impl.world.*;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class ModuleHandler {
	private final Minecraft mc = Minecraft.getMinecraft();
	private final Map<Class<? extends Module>, Module> moduleList = new LinkedHashMap<>();
	public void initialize() {
		OpenUtils.get().getEventBus().subscribe(this);
		// movement
		this.register(new NoJumpDelayModule());
		this.register(new NullMoveModule());
		this.register(new SprintModule());
		// player
		this.register(new ActionSoundsModule());
		this.register(new GhostLiquidFixModule());
		this.register(new NoBreakDelayModule());
		this.register(new NoHitDelayModule());
		// render
		this.register(new AnimationsModule());
		this.register(new AntiDebuffModule());
		this.register(new AntiShuffleModule());
		this.register(new ArrayListModule());
		this.register(new CameraModule());
		this.register(new CapeModule());
		this.register(new DamageTagsModule());
		this.register(new FallViewModule());
		this.register(new FreeLookModule());
		this.register(new NameHiderModule());
		this.register(new TargetHudModule());
		this.register(new ThickRodsModule());
		// world
		this.register(new TimeChangerModule());
		this.register(new WeatherModule()); // TODO: impl
		// hypixel
		this.register(new AutoGGModule());
		this.register(new DenickerModule());
		this.register(new NickBotModule()); // TODO: impl
		this.register(new QuickMathModule());
		// bed wars
		this.register(new ArmorAlertsModule());
		this.register(new FinalKillsHudModule());
		this.register(new ItemAlertsModule());
		this.register(new QuickShopModule());
		this.register(new ResourceCountModule());
		this.register(new TimersHudModule());
		this.register(new UpgradeAlertsModule());
		this.register(new UpgradesHudModule());
		// client
		this.register(new DebugModule());
		this.register(new GuiModule());
		this.register(new VPNStatusModule());
	}

	private void register(final Module module) {
		moduleList.put(module.getClass(), module);
	}

	public Collection<Module> getModules() {
		return moduleList.values();
	}

	public boolean isEnabled(final Class<? extends Module> moduleClass) {
		final Module module = moduleList.get(moduleClass);
		return module != null && module.isEnabled();
	}

	@SuppressWarnings("unchecked")
	public <T extends Module> T getModuleClass(final Class<T> moduleClass) {
		final Module module = moduleList.get(moduleClass);
		if (module != null) {
			return (T) module;
		}
		throw new IllegalStateException("Module not registered: " + moduleClass.getName());
	}

	public List<Module> getModulesInCategory(final ModuleCategory category) {
		final List<Module> modulesInCategory = new ArrayList<>();
		synchronized (moduleList) {
			for (final Module module : moduleList.values()) {
				if (module.getCategory().equals(category)) {
					modulesInCategory.add(module);
				}
			}
		}
		return modulesInCategory;
	}

	@Subscribe
	private void onKeyPress(final KeyPressEvent event) {
		if (mc.currentScreen != null || !ClientUtil.notNull()) {
			return;
		}
		final int keyCode = event.getKeyCode();
		if (keyCode == 0) {
			return;
		}
		final boolean pressed = event.isPressed();
		for (final Module module : getModules()) {
			if (module.getKeybind() == keyCode) {
				// Free Look requires keybind to be held
				if (module instanceof FreeLookModule) {
					module.setEnabled(pressed);
				} else {
					if (pressed) {
						module.toggle();
					}
				}
			}
		}
	}
}
