package org.afterlike.openutils.config.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.config.HudLayoutStore;
import org.afterlike.openutils.config.OpenUtilsConfig;
import org.afterlike.openutils.gui.OpenUtilsConfigScreen;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.forge.internal.ClientTickScheduler;
import re.tsuku.confikure.gui.ConfigGuiState;
import re.tsuku.confikure.model.ConfigCategory;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.OptionListener;
import re.tsuku.confikure.persistence.ConfigStore;

public class ConfigHandler {
	private final ConfigStore store = new ConfigStore();
	private final HudLayoutStore hudLayoutStore = new HudLayoutStore();
	private OpenUtilsConfig config;
	private ConfigDefinition definition;
	private ConfigGuiState guiState;
	private boolean loading = false;
	public void loadAndApply() {
		final Path path = getConfigPath();
		this.config = new OpenUtilsConfig(OpenUtils.get().getFeatureHandler());
		this.definition = Confikure.scan(this.config);
		this.guiState = defaultGuiState(this.definition);
		this.loading = true;
		try {
			this.store.load(this.definition, path, this.guiState);
			this.hudLayoutStore.load(OpenUtils.get().getFeatureHandler().getFeatures(),
					getHudLayoutPath());
		} catch (final IOException ignored) {
		} finally {
			this.loading = false;
		}
		bindOptionListeners();
		applyConfigToFeatures();
	}

	public void saveConfiguration() {
		if (this.loading) {
			return;
		}
		try {
			this.store.save(getDefinition(), getGuiState(), getConfigPath());
			this.hudLayoutStore.save(OpenUtils.get().getFeatureHandler().getFeatures(),
					getHudLayoutPath());
		} catch (final IOException ignored) {
		}
	}

	public OpenUtilsConfigScreen createGuiScreen() {
		return new OpenUtilsConfigScreen(getDefinition(), getGuiState(), this::saveGuiState);
	}

	public void openGui() {
		Minecraft.getMinecraft().displayGuiScreen(createGuiScreen());
	}

	public void openGuiDelayed() {
		ClientTickScheduler.schedule(new Runnable() {
			public void run() {
				openGui();
			}
		});
	}

	public boolean isLoading() {
		return this.loading;
	}

	public ConfigDefinition getDefinition() {
		if (this.definition == null) {
			this.config = new OpenUtilsConfig(OpenUtils.get().getFeatureHandler());
			this.definition = Confikure.scan(this.config);
			bindOptionListeners();
		}
		return this.definition;
	}

	public ConfigGuiState getGuiState() {
		if (this.guiState == null) {
			this.guiState = defaultGuiState(getDefinition());
		}
		return this.guiState;
	}

	public static ConfigGuiState defaultGuiState(final ConfigDefinition definition) {
		final ConfigGuiState state = new ConfigGuiState();
		for (final ConfigCategory category : definition.categories()) {
			for (final ConfigGroup group : category.groups()) {
				state.collapsed(category.id(), group.id(), true);
			}
		}
		return state;
	}

	private void bindOptionListeners() {
		final ConfigDefinition current = getDefinition();
		final OptionListener listener = new OptionListener() {
			public void changed(final ConfigOption option, final Object oldValue,
					final Object newValue) {
				applyConfigToFeatures();
				saveConfiguration();
			}
		};
		for (final ConfigCategory category : current.categories()) {
			for (final ConfigOption option : category.options()) {
				option.addListener(listener);
			}
		}
	}

	private static void applyConfigToFeatures() {
		OpenUtils.get().getFeatureHandler().applyConfiguredStates();
		OpenUtils.get().getFeatureHandler().notifyConfigChanged();
	}

	private void saveGuiState(final ConfigGuiState guiState) {
		this.guiState = guiState;
		saveConfiguration();
	}

	private static Path getConfigPath() {
		return getConfigDir().resolve("config.json");
	}

	private static Path getHudLayoutPath() {
		return getConfigDir().resolve("hud-layout.json");
	}

	private static Path getConfigDir() {
		final Minecraft mc = Minecraft.getMinecraft();
		final Path dir = Paths.get(mc.mcDataDir.getAbsolutePath(), "config", "openutils");
		try {
			Files.createDirectories(dir);
		} catch (final IOException ignored) {
		}
		return dir;
	}
}
