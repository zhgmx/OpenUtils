package org.afterlike.openutils.util.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.afterlike.openutils.OpenUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class UpdateUtil {
	private static final Logger logger = LogManager.getLogger(UpdateUtil.class);
	private static final String API_URL = "https://api.github.com/repos/polariscli/OpenUtils/releases?per_page=20";
	private static volatile String latest = null;
	private UpdateUtil() {
	}

	public static void checkAsync() {
		CompletableFuture.runAsync(UpdateUtil::check);
	}

	private static void check() {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(API_URL).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "OpenUtils/" + OpenUtils.get().getVersion());
			conn.setRequestProperty("Accept", "application/vnd.github+json");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
				latest = latestPublishedTag(JsonParser.parseReader(reader).getAsJsonArray());
				if (latest == null)
					return;
				String current = OpenUtils.get().getVersion();
				OpenUtils.get().setOutdated(!current.equals("dev") && !current.equals(latest));
				logger.info("Successfully checked latest version: {}", latest);
			}
		} catch (Throwable e) {
			logger.warn("Update check failed: {}", e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static String getLatest() {
		return latest;
	}

	private static String latestPublishedTag(final JsonArray releases) {
		for (final JsonElement element : releases) {
			if (!element.isJsonObject()) {
				continue;
			}
			final JsonObject release = element.getAsJsonObject();
			if (release.has("draft") && release.get("draft").getAsBoolean()) {
				continue;
			}
			if (release.has("tag_name")) {
				return release.get("tag_name").getAsString();
			}
		}
		return null;
	}
}
