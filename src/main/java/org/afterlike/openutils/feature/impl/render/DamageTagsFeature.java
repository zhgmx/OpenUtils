package org.afterlike.openutils.feature.impl.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.RenderWorldEvent;
import org.afterlike.openutils.event.impl.ResizeWindowEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.platform.mixin.minecraft.client.renderer.EntityRendererAccessor;
import org.afterlike.openutils.util.client.ClientUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import re.tsuku.confikure.annotations.Mode;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.fastbus.Subscribe;

public class DamageTagsFeature extends ToggleableFeature {
	@Option(name = "Enable Damage Tags",
			description = "Show floating health-change tags above players.", order = 0)
	public boolean enabled;
	@Option(name = "Show healing", description = "Create tags for health gained.", order = 1)
	public boolean showHealing = true;
	@Option(name = "Show damage", description = "Create tags for health lost.", order = 2)
	public boolean showDamage = true;
	@Option(name = "Units", description = "Display health changes as hearts or raw health points.",
			order = 3)
	@Mode(values = {"Hearts", "Health Points"})
	public String mode = "Hearts";
	@Option(name = "Color",
			description = "Use red/green change colors or each player's team color.", order = 4)
	@Mode(values = {"RAG", "Team"})
	public String colorMode = "RAG";
	@Option(name = "Scale", description = "Base size of projected damage tag text.", order = 5)
	@Range(min = 0.0D, max = 10.0D, step = 0.5D)
	public double scale = 5.0D;
	@Option(name = "Duration", description = "How long tags stay fully visible before fading.",
			order = 6)
	@Range(min = 0.0D, max = 5.0D, step = 0.1D)
	public double duration = 1.5D;
	@Option(name = "Y offset", description = "Vertical world-space offset above the player.",
			order = 7)
	@Range(min = -2.0D, max = 3.0D, step = 0.1D)
	public double yOffset = 1.8D;
	private final Map<UUID, Float> playerHealth = new HashMap<>(64);
	private final List<DamageTag> tags = new ArrayList<>(64);
	private final ArrayDeque<DamageTag> pool = new ArrayDeque<>(64);
	private static final int GREEN = 0xFF00FF00;
	private static final int RED = 0xFFFF0000;
	private static final double FADE_DISTANCE = 2.0;
	private static final double MIN_DISTANCE = 0.5;
	private static final long FADE_OUT_TIME = 150;
	private static final double MAX_RENDER_DISTANCE = 25.0;
	private static final double MAX_SCREEN_Z = 1.0003684d;
	private static final FloatBuffer MODEL_VIEW = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer(16);
	private static final IntBuffer VIEWPORT = BufferUtils.createIntBuffer(16);
	private static final FloatBuffer OBJECT_COORDS = BufferUtils.createFloatBuffer(3);
	private int cachedScaleFactor = -1;
	private int lastDisplayW = -1;
	private int lastDisplayH = -1;
	private int lastGuiScale = -1;
	private boolean lastUnicode = false;
	private static final class DamageTag {
		double x, y, z;
		double distance, lastDistance;
		long timeMs;
		int baseColor;
		String text;
	}
	public DamageTagsFeature() {
		super("Damage Tags", FeatureCategory.RENDER);
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		if (!ClientUtil.notNull())
			return;
		if (mc.theWorld == null)
			return;
		final double meX = mc.thePlayer.posX;
		final double meY = mc.thePlayer.posY;
		final double meZ = mc.thePlayer.posZ;
		final long now = System.currentTimeMillis();
		final boolean hearts = "Hearts".equals(mode);
		final boolean teamColor = "Team".equals(colorMode);
		final boolean showHealingValue = showHealing;
		final boolean showDamageValue = showDamage;
		final double yOffsetValue = yOffset;
		// hp -> display units conversion: hearts = hp/2, hp = hp/1
		final float hpToDisplay = hearts ? 0.5f : 1.0f;
		for (final EntityPlayer player : mc.theWorld.playerEntities) {
			if (player == null || player.isDead)
				continue;
			final UUID id = player.getUniqueID();
			final float hp = player.getHealth() + player.getAbsorptionAmount();
			final Float prev = playerHealth.put(id, hp);
			final float lastHp = (prev != null) ? prev : hp;
			final float health = hp * hpToDisplay;
			final float lastHealth = lastHp * hpToDisplay;
			if (player.ticksExisted < 2)
				continue;
			if (health == lastHealth)
				continue;
			if (!showDamageValue && health < lastHealth)
				continue;
			if (!showHealingValue && health > lastHealth)
				continue;
			final float difference = health - lastHealth;
			final long roundedDifference = Math.round(Math.abs(difference));
			if (roundedDifference == 0L)
				continue;
			final int baseColor = (health > lastHealth) ? GREEN : RED;
			final String renderHealth;
			if (teamColor) {
				final String displayName = player.getDisplayName().getFormattedText();
				final String teamColorCode = (displayName.length() >= 2)
						? displayName.substring(0, 2)
						: "§r";
				renderHealth = teamColorCode + formatDoubleStr(Math.round(difference));
			} else {
				renderHealth = formatDoubleStr(roundedDifference);
			}
			final double x = player.posX;
			final double y = player.posY + yOffsetValue;
			final double z = player.posZ;
			final double dist = distance(meX, meY, meZ, x, y, z);
			final DamageTag tag = obtainTag();
			tag.x = x;
			tag.y = y;
			tag.z = z;
			tag.timeMs = now;
			tag.baseColor = baseColor;
			tag.text = renderHealth;
			tag.distance = dist;
			tag.lastDistance = dist;
			tags.add(tag);
		}
		if (!tags.isEmpty()) {
			for (final DamageTag tag : tags) {
				tag.lastDistance = tag.distance;
				tag.distance = distance(meX, meY, meZ, tag.x, tag.y, tag.z);
			}
			if (tags.size() > 1) {
				tags.sort((a, b) -> Double.compare(b.distance, a.distance));
			}
		}
	}

	@Subscribe
	private void onRenderWorld(final RenderWorldEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (tags.isEmpty())
			return;
		final long now = System.currentTimeMillis();
		final float partialTicks = event.getPartialTicks();
		final long durationMs = (long) (duration * 1000.0);
		final double baseScale = scale;
		final int scaleFactor = getScaleFactorCached();
		prepareProjection(partialTicks);
		final double viewX = mc.getRenderManager().viewerPosX;
		final double viewY = mc.getRenderManager().viewerPosY;
		final double viewZ = mc.getRenderManager().viewerPosZ;
		final int displayH = Display.getHeight();
		for (final Iterator<DamageTag> it = tags.iterator(); it.hasNext();) {
			final DamageTag tag = it.next();
			final long elapsed = now - tag.timeMs;
			if (elapsed > durationMs + FADE_OUT_TIME) {
				recycleTag(tag);
				it.remove();
				continue;
			}
			final double distance = tag.distance;
			if (distance > MAX_RENDER_DISTANCE)
				continue;
			int alpha = 255;
			if (elapsed > durationMs) {
				alpha = (int) (255.0
						* (1.0 - ((double) (elapsed - durationMs) / (double) FADE_OUT_TIME)));
			}
			if (alpha <= 5) {
				recycleTag(tag);
				it.remove();
				continue;
			}
			if (distance < FADE_DISTANCE) {
				final double scaledDistance = (distance - MIN_DISTANCE)
						/ (FADE_DISTANCE - MIN_DISTANCE);
				final int proximityAlpha = (int) (5 + (250 * Math.max(scaledDistance, 0.0)));
				alpha = Math.min(alpha, proximityAlpha);
			}
			final int color = (tag.baseColor & 0x00FFFFFF) | (alpha << 24);
			final double relX = tag.x - viewX;
			final double relY = tag.y - viewY;
			final double relZ = tag.z - viewZ;
			final ScreenPos sp = projectToScreen(relX, relY, relZ, scaleFactor, displayH);
			if (sp == null || sp.z < 0.0 || sp.z >= MAX_SCREEN_Z)
				continue;
			final double lastDistance = tag.lastDistance;
			final double interpolatedDistance = lastDistance
					+ (distance - lastDistance) * partialTicks;
			final float scaleValue = (float) (baseScale / interpolatedDistance);
			final String text = tag.text;
			final float textWidth = mc.fontRendererObj.getStringWidth(text) * scaleValue;
			final float textHeight = mc.fontRendererObj.FONT_HEIGHT * scaleValue;
			final float screenX = (float) sp.x - textWidth / 2.0f;
			final float screenY = (float) sp.y - textHeight / 2.0f;
			GL11.glPushMatrix();
			GL11.glScalef(scaleValue, scaleValue, 1.0f);
			mc.fontRendererObj.drawString(text, screenX / scaleValue, screenY / scaleValue, color,
					true);
			GL11.glPopMatrix();
		}
	}
	private static final class ScreenPos {
		final double x, y, z;
		ScreenPos(final double x, final double y, final double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	private void prepareProjection(final float partialTicks) {
		((EntityRendererAccessor) mc.entityRenderer).ou$setupCameraTransform(partialTicks, 0);
		MODEL_VIEW.clear();
		PROJECTION.clear();
		VIEWPORT.clear();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODEL_VIEW);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
		GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT);
		mc.entityRenderer.setupOverlayRendering();
	}

	private ScreenPos projectToScreen(final double relX, final double relY, final double relZ,
			final int scaleFactor, final int displayHeight) {
		try {
			OBJECT_COORDS.clear();
			if (!GLU.gluProject((float) relX, (float) relY, (float) relZ, MODEL_VIEW, PROJECTION,
					VIEWPORT, OBJECT_COORDS)) {
				return null;
			}
			final double x = OBJECT_COORDS.get(0) / (double) scaleFactor;
			final double y = (displayHeight - OBJECT_COORDS.get(1)) / (double) scaleFactor;
			final double z = OBJECT_COORDS.get(2);
			return new ScreenPos(x, y, z);
		} catch (final Exception ignored) {
			return null;
		}
	}

	private int getScaleFactorCached() {
		final int dw = mc.displayWidth;
		final int dh = mc.displayHeight;
		final int gs = mc.gameSettings.guiScale;
		final boolean unicode = mc.fontRendererObj.getUnicodeFlag();
		if (cachedScaleFactor <= 0 || dw != lastDisplayW || dh != lastDisplayH || gs != lastGuiScale
				|| unicode != lastUnicode) {
			final ScaledResolution res = new ScaledResolution(mc);
			cachedScaleFactor = res.getScaleFactor();
			lastDisplayW = dw;
			lastDisplayH = dh;
			lastGuiScale = gs;
			lastUnicode = unicode;
		}
		return cachedScaleFactor;
	}

	private DamageTag obtainTag() {
		final DamageTag tag = pool.pollFirst();
		return (tag != null) ? tag : new DamageTag();
	}

	private void recycleTag(final DamageTag tag) {
		if (pool.size() < 256) {
			tag.text = null;
			pool.addFirst(tag);
		}
	}

	@Subscribe
	private void onWorldLoad(final WorldLoadEvent event) {
		clearAll();
	}

	@Subscribe
	private void onResize(final ResizeWindowEvent event) {
		cachedScaleFactor = -1;
	}

	@Override
	protected void onEnable() {
		clearAll();
	}

	@Override
	protected void onDisable() {
		clearAll();
	}

	private void clearAll() {
		tags.clear();
		pool.clear();
		playerHealth.clear();
	}

	private static double distance(final double ax, final double ay, final double az,
			final double bx, final double by, final double bz) {
		final double dx = ax - bx;
		final double dy = ay - by;
		final double dz = az - bz;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private static String formatDoubleStr(final double val) {
		return val == (long) val ? Long.toString((long) val) : Double.toString(val);
	}
}
