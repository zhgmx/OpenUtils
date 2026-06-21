package org.afterlike.openutils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.afterlike.openutils.feature.api.Feature;
import org.afterlike.openutils.feature.api.hud.Anchor;
import org.afterlike.openutils.feature.api.hud.HudFeature;
import org.afterlike.openutils.feature.api.hud.Position;

public final class HudLayoutStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type DATA_TYPE = new TypeToken<Map<String, Entry>>() {
	}.getType();
	public void load(final Iterable<Feature> features, final Path path) throws IOException {
		if (!Files.isRegularFile(path)) {
			return;
		}
		final Map<String, Entry> entries;
		try (Reader reader = Files.newBufferedReader(path)) {
			entries = GSON.fromJson(reader, DATA_TYPE);
		}
		if (entries == null) {
			return;
		}
		for (final Feature feature : features) {
			if (!(feature instanceof HudFeature)) {
				continue;
			}
			final Entry entry = entries.get(feature.getName());
			if (entry == null) {
				continue;
			}
			final Position position = ((HudFeature) feature).getHudPosition();
			try {
				position.setAnchor(Anchor.valueOf(entry.anchor));
			} catch (final RuntimeException ignored) {
				position.setAnchor(Anchor.TOP_LEFT);
			}
			position.setPosition(entry.x, entry.y);
		}
	}

	public void save(final Iterable<Feature> features, final Path path) throws IOException {
		final Map<String, Entry> entries = new LinkedHashMap<>();
		for (final Feature feature : features) {
			if (!(feature instanceof HudFeature)) {
				continue;
			}
			final Position position = ((HudFeature) feature).getHudPosition();
			final Entry entry = new Entry();
			entry.x = position.getOffsetX();
			entry.y = position.getOffsetY();
			entry.anchor = position.getAnchor().name();
			entries.put(feature.getName(), entry);
		}
		final Path parent = path.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(entries, writer);
		}
	}
	private static final class Entry {
		private int x;
		private int y;
		private String anchor = Anchor.TOP_LEFT.name();
	}
}
