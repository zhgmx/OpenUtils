package org.afterlike.openutils.util.game;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;

public final class RenderUtil {
	private RenderUtil() {
	}

	public static void drawChromaText(final String text, final char lineSplit, int x, int y,
			final long speed, final long shift, final boolean shadow,
			final FontRenderer fontRenderer) {
		final int bX = x;
		int l = 0;
		long r = 0L;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == lineSplit) {
				l++;
				x = bX;
				y += fontRenderer.FONT_HEIGHT + 5;
				r = shift * l;
			} else {
				fontRenderer.drawString(String.valueOf(c), x, y, getChromaColor(speed, r), shadow);
				x += fontRenderer.getCharWidth(c);
				if (c != ' ') {
					r -= 90L;
				}
			}
		}
	}

	public static int getChromaColor(final long speed, final long... delay) {
		final long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
		float hue = (float) (time % (15000L / speed)) / (15000.0F / (float) speed);
		return Color.HSBtoRGB(hue, 1.0F, 1.0F);
	}

	public static void drawRect(float left, float top, float right, float bottom, int color) {
		if (left < right) {
			float i = left;
			left = right;
			right = i;
		}
		if (top < bottom) {
			float j = top;
			top = bottom;
			bottom = j;
		}
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(f, f1, f2, f3);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION);
		worldrenderer.pos(left, bottom, 0.0D).endVertex();
		worldrenderer.pos(right, bottom, 0.0D).endVertex();
		worldrenderer.pos(right, top, 0.0D).endVertex();
		worldrenderer.pos(left, top, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public static void renderPlayerHead(EntityPlayer player, int x, int y, int size) {
		if (!(player instanceof AbstractClientPlayer))
			return;
		Minecraft mc = Minecraft.getMinecraft();
		AbstractClientPlayer clientPlayer = (AbstractClientPlayer) player;
		mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
		GlStateManager.color(1, 1, 1, 1);
		Gui.drawScaledCustomSizeModalRect(x, y, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
		Gui.drawScaledCustomSizeModalRect(x, y, 40.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
	}

	public static float lerp(float start, float end, float t) {
		return start + (end - start) * t;
	}

	public static int getHealthColor(float healthPercent) {
		float r, g;
		if (healthPercent <= 0.5f) {
			r = 1.0f;
			g = healthPercent * 2.0f;
		} else {
			r = 1.0f - (healthPercent - 0.5f) * 2.0f;
			g = 1.0f;
		}
		return 0xFF000000 | ((int) (r * 255) << 16) | ((int) (g * 255) << 8);
	}

	public static int darkenColor(int color, float factor) {
		int a = (color >> 24) & 0xFF;
		int r = (int) (((color >> 16) & 0xFF) * factor);
		int g = (int) (((color >> 8) & 0xFF) * factor);
		int b = (int) ((color & 0xFF) * factor);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
}
