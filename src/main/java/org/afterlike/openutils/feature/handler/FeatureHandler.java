package org.afterlike.openutils.feature.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.event.impl.KeyPressEvent;
import org.afterlike.openutils.feature.api.Feature;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.KeyboundFeature;
import org.afterlike.openutils.feature.impl.bedwars.ArmorAlertsFeature;
import org.afterlike.openutils.feature.impl.bedwars.FinalKillsHudFeature;
import org.afterlike.openutils.feature.impl.bedwars.ItemAlertsFeature;
import org.afterlike.openutils.feature.impl.bedwars.QuickShopFeature;
import org.afterlike.openutils.feature.impl.bedwars.ResourceCountFeature;
import org.afterlike.openutils.feature.impl.bedwars.TimersHudFeature;
import org.afterlike.openutils.feature.impl.bedwars.UpgradeAlertsFeature;
import org.afterlike.openutils.feature.impl.bedwars.UpgradesHudFeature;
import org.afterlike.openutils.feature.impl.client.DebugFeature;
import org.afterlike.openutils.feature.impl.client.GuiFeature;
import org.afterlike.openutils.feature.impl.client.VPNStatusFeature;
import org.afterlike.openutils.feature.impl.hypixel.AutoGGFeature;
import org.afterlike.openutils.feature.impl.hypixel.DenickerFeature;
import org.afterlike.openutils.feature.impl.hypixel.QuickMathFeature;
import org.afterlike.openutils.feature.impl.movement.NoJumpDelayFeature;
import org.afterlike.openutils.feature.impl.movement.NullMoveFeature;
import org.afterlike.openutils.feature.impl.movement.SprintFeature;
import org.afterlike.openutils.feature.impl.player.ActionSoundsFeature;
import org.afterlike.openutils.feature.impl.player.NoBreakDelayFeature;
import org.afterlike.openutils.feature.impl.player.NoHitDelayFeature;
import org.afterlike.openutils.feature.impl.render.AnimationsFeature;
import org.afterlike.openutils.feature.impl.render.AntiDebuffFeature;
import org.afterlike.openutils.feature.impl.render.AntiShuffleFeature;
import org.afterlike.openutils.feature.impl.render.CameraFeature;
import org.afterlike.openutils.feature.impl.render.CapeFeature;
import org.afterlike.openutils.feature.impl.render.DamageTagsFeature;
import org.afterlike.openutils.feature.impl.render.FreeLookFeature;
import org.afterlike.openutils.feature.impl.render.NameHiderFeature;
import org.afterlike.openutils.feature.impl.render.TargetHudFeature;
import org.afterlike.openutils.feature.impl.render.ThickRodsFeature;
import org.afterlike.openutils.feature.impl.world.TimeChangerFeature;
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
		this.register(new CameraFeature());
		this.register(new CapeFeature());
		this.register(new DamageTagsFeature());
		this.register(new FreeLookFeature());
		this.register(new NameHiderFeature());
		this.register(new TargetHudFeature());
		this.register(new ThickRodsFeature());
		// world
		this.register(new TimeChangerFeature());
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
