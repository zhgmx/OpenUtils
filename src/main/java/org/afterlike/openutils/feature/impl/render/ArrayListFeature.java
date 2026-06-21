package org.afterlike.openutils.feature.impl.render;

import java.util.*;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.event.impl.RenderOverlayEvent;
import org.afterlike.openutils.feature.api.Feature;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.feature.api.hud.HudFeature;
import org.afterlike.openutils.feature.api.hud.Position;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.game.RenderUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class ArrayListFeature extends ToggleableFeature implements HudFeature {
	private final Position position = new Position(5, 70);
	@Option(name = "Enable Array List",
			description = "Show enabled features as a draggable HUD list.", order = 0)
	public boolean enabled;
	@Option(name = "Drop shadow", description = "Draw feature names with Minecraft's text shadow.",
			order = 1)
	public boolean dropShadow = true;
	@Option(name = "Chroma", description = "Cycle feature name colors through a rainbow.",
			order = 2)
	public boolean chroma;
	@Option(name = "Sort alphabetically",
			description = "Sort shown features by name instead of text width.", order = 3)
	public boolean alphabeticalSort;
	private final List<Feature> sortedAlpha = new ArrayList<>(64);
	private final List<Feature> sortedWidth = new ArrayList<>(64);
	private final Map<Feature, Integer> widthByFeature = new IdentityHashMap<>(64);
	private boolean cacheBuilt = false;
	private static final Comparator<Feature> ALPHA_COMPARATOR = Comparator
			.comparing(Feature::getName);
	public ArrayListFeature() {
		super("Array List", FeatureCategory.RENDER);
	}

	@Subscribe
	private void onRenderOverlay(final RenderOverlayEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (mc.currentScreen != null || mc.gameSettings.showDebugInfo)
			return;
		ensureCache();
		final int x = position.getX();
		int y = position.getY();
		int delta = 0;
		final boolean shadow = useHudDropShadow();
		final int lineStep = mc.fontRendererObj.FONT_HEIGHT + 2;
		final List<Feature> list = alphabeticalSort ? sortedAlpha : sortedWidth;
		for (final Feature feature : list) {
			if (feature == this || !feature.isEnabled())
				continue;
			int color = chroma ? RenderUtil.getChromaColor(2L, delta) : 0xFFFFFFFF;
			mc.fontRendererObj.drawString(feature.getName(), x, y, color, shadow);
			y += lineStep;
			delta -= 120;
		}
	}

	private void ensureCache() {
		if (cacheBuilt)
			return;
		final Collection<Feature> all = OpenUtils.get().getFeatureHandler().getFeatures();
		sortedAlpha.clear();
		sortedAlpha.addAll(all);
		sortedAlpha.sort(ALPHA_COMPARATOR);
		widthByFeature.clear();
		for (final Feature m : all) {
			widthByFeature.put(m, mc.fontRendererObj.getStringWidth(m.getName()));
		}
		sortedWidth.clear();
		sortedWidth.addAll(all);
		sortedWidth.sort((a, b) -> Integer.compare(widthByFeature.get(b), widthByFeature.get(a)));
		cacheBuilt = true;
	}

	@Override
	public Position getHudPosition() {
		return position;
	}

	@Override
	public String getHudPlaceholderText() {
		return "This is an-Array-List";
	}

	@Override
	public boolean useHudDropShadow() {
		return dropShadow;
	}
}
