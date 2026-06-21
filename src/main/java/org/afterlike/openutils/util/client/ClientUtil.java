package org.afterlike.openutils.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.feature.impl.client.DebugFeature;

public class ClientUtil {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final String prefix = "§8§l» §r";
	public static void sendMessage(final String message) {
		if (notNull()) {
			mc.thePlayer.addChatMessage(
					new ChatComponentText(TextUtil.replaceColorCodes(prefix + message)));
		}
	}

	public static void sendChatComponent(final ChatComponentText message) {
		if (notNull()) {
			mc.thePlayer.addChatMessage(message);
		}
	}

	public static void sendDebugMessage(final String message) {
		if (notNull() && OpenUtils.get().getFeatureHandler().isEnabled(DebugFeature.class)) {
			sendMessage("&c[DEBUG] &r" + message);
		}
	}

	public static void sendMessageAsPlayer(final String message) {
		if (notNull()) {
			mc.thePlayer.sendChatMessage(message);
		}
	}

	public static boolean notNull() {
		return mc.thePlayer != null && mc.theWorld != null;
	}

	public static String getPrefix() {
		return prefix;
	}
}
