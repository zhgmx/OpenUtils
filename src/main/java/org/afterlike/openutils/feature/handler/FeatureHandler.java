package org.afterlike.openutils.feature.handler;

import java.util.*;
import net.minecraft.client.Minecraft;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.event.impl.KeyPressEvent;
import org.afterlike.openutils.feature.api.Feature;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.KeyboundFeature;
import org.afterlike.openutils.feature.impl.bedwars.*;
import org.afterlike.openutils.feature.impl.client.*;
import org.afterlike.openutils.feature.impl.hypixel.*;
import org.afterlike.openutils.feature.impl.movement.*;
import org.afterlike.openutils.feature.impl.player.*;
import org.afterlike.openutils.feature.impl.render.*;
import org.afterlike.openutils.feature.impl.world.*;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class FeatureHandler {
	private final Minecraft mc = Minecraft.getMinecraft();
	private final Map<Class<? extends Feature>, Feature> featureList = new LinkedHashMap<>();
	public void initialize() {
		OpenUtils.get().getEventBus().subscribe(this);
		// movement
		this.register(new NoJumpDelayFeature());
		this.register(new NullMoveFeature());
		this.register(new SprintFeature());
		// player
		this.register(new ActionSoundsFeature());
		this.register(new NoBreakDelayFeature());
		this.register(new NoHitDelayFeature());
		// render
		this.register(new AnimationsFeature());
		this.register(new AntiDebuffFeature());
		this.register(new AntiShuffleFeature());
		this.register(new ArrayListFeature());
		this.register(new CameraFeature());
		this.register(new CapeFeature());
		this.register(new DamageTagsFeature());
		this.register(new FreeLookFeature());
		this.register(new NameHiderFeature());
		this.register(new TargetHudFeature());
		this.register(new ThickRodsFeature());
		// world
		this.register(new TimeChangerFeature());
		this.register(new WeatherFeature());
		// hypixel
		this.register(new AutoGGFeature());
		this.register(new DenickerFeature());
		this.register(new QuickMathFeature());
		// bed wars
		this.register(new ArmorAlertsFeature());
		this.register(new FinalKillsHudFeature());
		this.register(new ItemAlertsFeature());
		this.register(new QuickShopFeature());
		this.register(new ResourceCountFeature());
		this.register(new TimersHudFeature());
		this.register(new UpgradeAlertsFeature());
		this.register(new UpgradesHudFeature());
		// client
		this.register(new DebugFeature());
		this.register(new GuiFeature());
		this.register(new VPNStatusFeature());
	}

	private void register(final Feature feature) {
		featureList.put(feature.getClass(), feature);
	}

	public Collection<Feature> getFeatures() {
		return featureList.values();
	}

	public boolean isEnabled(final Class<? extends Feature> featureClass) {
		final Feature feature = featureList.get(featureClass);
		return feature != null && feature.isEnabled();
	}

	@SuppressWarnings("unchecked")
	public <T extends Feature> T getFeature(final Class<T> featureClass) {
		final Feature feature = featureList.get(featureClass);
		if (feature != null) {
			return (T) feature;
		}
		throw new IllegalStateException("Feature not registered: " + featureClass.getName());
	}

	public List<Feature> getFeaturesInCategory(final FeatureCategory category) {
		final List<Feature> featuresInCategory = new ArrayList<>();
		synchronized (featureList) {
			for (final Feature feature : featureList.values()) {
				if (feature.getCategory().equals(category)) {
					featuresInCategory.add(feature);
				}
			}
		}
		return featuresInCategory;
	}

	public void applyConfiguredStates() {
		for (final Feature feature : getFeatures()) {
			feature.applyConfiguredState();
		}
	}

	public void notifyConfigChanged() {
		for (final Feature feature : getFeatures()) {
			feature.onConfigChanged();
		}
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
		for (final Feature feature : getFeatures()) {
			if (!(feature instanceof KeyboundFeature)) {
				continue;
			}
			final KeyboundFeature keyboundFeature = (KeyboundFeature) feature;
			if (keyboundFeature.getKeybind() == keyCode) {
				keyboundFeature.onKeyInput(pressed);
			}
		}
	}
}
