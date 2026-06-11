package org.afterlike.openutils;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.afterlike.openutils.config.handler.ConfigHandler;
import org.afterlike.openutils.gui.ClickGuiScreen;
import org.afterlike.openutils.module.handler.ModuleHandler;
import org.afterlike.openutils.util.client.UpdateUtil;
import org.afterlike.openutils.util.lang.ReflectionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import re.tsuku.fastbus.FastBus;

public class OpenUtils {
	private static final Logger logger = LogManager.getLogger(OpenUtils.class);
	private static final OpenUtils instance = new OpenUtils();
	private static final String VERSION = org.afterlike.openutils.BuildConstants.VERSION;
	// check for updates
	private static volatile boolean outdated = false;
	private static volatile boolean notified = false;
	private final ModuleHandler moduleHandler;
	private final ConfigHandler configHandler;
	private final FastBus eventBus;
	private ClickGuiScreen clickGuiScreen;
	public OpenUtils() {
		this.moduleHandler = new ModuleHandler();
		this.configHandler = new ConfigHandler();
		this.eventBus = new FastBus();
	}

	public void initialize() {
		final long startTime = System.nanoTime();
		moduleHandler.initialize();
		configHandler.loadAndApply();
		logger.info("Initialized in {}ms.", (System.nanoTime() - startTime) / 1_000_000);
	}

	public void lateInitialize() {
		final FMLCommonHandler commonHandler = FMLCommonHandler.instance();
		commonHandler.computeBranding();
		for (final String field : new String[]{"brandings", "brandingsNoMC"}) {
			final List<String> list = Objects
					.requireNonNull(ReflectionUtil.getField(commonHandler, field));
			final ImmutableList.Builder<String> builder = ImmutableList.builder();
			builder.add(String.format("%sOpenUtils %s", EnumChatFormatting.WHITE, VERSION));
			builder.addAll(list);
			ReflectionUtil.setField(commonHandler, field, builder.build());
		}
		UpdateUtil.checkAsync();
	}

	public FastBus getEventBus() {
		return eventBus;
	}

	public ModuleHandler getModuleHandler() {
		return moduleHandler;
	}

	public ConfigHandler getConfigHandler() {
		return configHandler;
	}

	public ClickGuiScreen getClickGuiScreen() {
		if (clickGuiScreen == null) {
			clickGuiScreen = new ClickGuiScreen();
		}
		return clickGuiScreen;
	}

	public String getVersion() {
		return VERSION;
	}

	public void setOutdated(boolean isOutdated) {
		outdated = isOutdated;
	}

	public boolean isOutdated() {
		return outdated;
	}

	public void setNotified(boolean isNotified) {
		notified = isNotified;
	}

	public boolean isNotified() {
		return notified;
	}

	public static OpenUtils get() {
		return Objects.requireNonNull(instance);
	}
}
