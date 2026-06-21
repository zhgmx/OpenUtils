package org.afterlike.openutils.util.client;

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
	private static final String API_URL = "https://api.github.com/repos/polariscli/OpenUtils/releases/latest";
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
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				if (!json.has("tag_name"))
					return;
				latest = json.get("tag_name").getAsString();
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
}
