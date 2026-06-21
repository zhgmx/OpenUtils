package org.afterlike.openutils.feature.impl.render;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.entity.player.EntityPlayer;
import org.afterlike.openutils.event.impl.AttackEntityEvent;
import org.afterlike.openutils.event.impl.RenderOverlayEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.feature.api.hud.HudFeature;
import org.afterlike.openutils.feature.api.hud.Position;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.game.RenderUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.fastbus.Subscribe;

public class TargetHudFeature extends ToggleableFeature implements HudFeature {
	private static final DecimalFormat HEALTH_FORMAT = new DecimalFormat("0.0",
			new DecimalFormatSymbols(Locale.US));
	private static final DecimalFormat DIFF_FORMAT = new DecimalFormat("+0.0;-0.0",
			new DecimalFormatSymbols(Locale.US));
	private static final long ANIM_DURATION_MS = 150L;
	private final Position position = new Position(400, 300);
	@Option(name = "Enable Target HUD",
			description = "Show target health and hit status after attacking a player.", order = 0)
	public boolean enabled;
	@Option(name = "Drop shadow", description = "Draw target text with Minecraft's text shadow.",
			order = 1)
	public boolean dropShadow = true;
	@Option(name = "HUD hold time",
			description = "How long the target HUD remains after your last hit.", order = 2)
	@Range(min = 0.5D, max = 10.0D, step = 0.5D)
	public double persistTime = 3.0D;
	@Option(name = "Background opacity", description = "Opacity of the target HUD background.",
			order = 3)
	@Range(min = 0.0D, max = 255.0D, step = 1.0D)
	public int backgroundAlpha = 64;
	@Option(name = "Show head", description = "Render the target's player head inside the HUD.",
			order = 4)
	public boolean showHead = true;
	@Option(name = "Show outline", description = "Draw a health-colored border around the HUD.",
			order = 5)
	public boolean showOutline;
	@Option(name = "Show indicator",
			description = "Show win, loss, or draw status and health difference.", order = 6)
	public boolean showIndicator = true;
	private EntityPlayer target;
	private EntityPlayer previousTarget;
	private long lastAttackTime;
	private long animStartTime;
	private float oldHealth;
	private float newHealth;
	private float maxHealth;
	public TargetHudFeature() {
		super("Target HUD", FeatureCategory.RENDER);
	}

	@Override
	protected void onDisable() {
		target = null;
		previousTarget = null;
	}

	@Subscribe
	private void onWorldLoad(final WorldLoadEvent event) {
		target = null;
		previousTarget = null;
	}

	@Subscribe
	private void onAttack(AttackEntityEvent event) {
		if (event.getTarget() instanceof EntityPlayer) {
			target = (EntityPlayer) event.getTarget();
			lastAttackTime = System.currentTimeMillis();
		}
	}

	@Subscribe
	private void onRender(RenderOverlayEvent event) {
		if (!ClientUtil.notNull() || target == null) {
			return;
		}
		final EntityPlayer entity = target;
		final long currentTime = System.currentTimeMillis();
		if (entity.isDead || mc.thePlayer.getDistanceToEntity(entity) > 15
				|| (currentTime - lastAttackTime > persistTime * 1000)) {
			target = null;
			previousTarget = null;
			return;
		}
		float playerHealth = (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / 2.0f;
		float targetAbsorption = entity.getAbsorptionAmount() / 2.0f;
		float targetHealth = entity.getHealth() / 2.0f + targetAbsorption;
		if (entity != previousTarget) {
			previousTarget = entity;
			animStartTime = currentTime;
			oldHealth = targetHealth;
			newHealth = targetHealth;
			maxHealth = entity.getMaxHealth() / 2.0f;
		}
		long elapsed = currentTime - animStartTime;
		if (elapsed >= ANIM_DURATION_MS) {
			if (oldHealth != newHealth) {
				oldHealth = newHealth;
			}
			if (newHealth != targetHealth) {
				animStartTime = currentTime;
				oldHealth = newHealth;
				newHealth = targetHealth;
				elapsed = 0;
			}
		}
		maxHealth = entity.getMaxHealth() / 2.0f;
		float t = Math.min(1.0f, (float) elapsed / ANIM_DURATION_MS);
		float displayHealth = RenderUtil.lerp(oldHealth, newHealth, t);
		float healthPercent = Math.min(1.0f, Math.max(0.0f, displayHealth / maxHealth));
		float healthDelta = playerHealth - targetHealth;
		float healthDeltaRatio = Math.min(1.0f, Math.max(0.0f, (healthDelta + 1.0f) / 2.0f));
		final int x = position.getX();
		final int y = position.getY();
		final boolean shadow = useHudDropShadow();
		final String name = entity.getDisplayName().getFormattedText();
		final String healthText = HEALTH_FORMAT.format(targetHealth)
				+ (targetAbsorption > 0 ? "§6" : "§c") + "❤";
		final String statusText = targetHealth == playerHealth
				? "§e§lD"
				: (targetHealth < playerHealth ? "§a§lW" : "§c§lL");
		final String healthDiffText = targetHealth == playerHealth
				? "0.0"
				: DIFF_FORMAT.format(healthDelta);
		final int nameWidth = mc.fontRendererObj.getStringWidth(name);
		final int healthTextWidth = mc.fontRendererObj.getStringWidth(healthText);
		final int statusTextWidth = showIndicator
				? mc.fontRendererObj.getStringWidth(statusText)
				: 0;
		final int healthDiffWidth = showIndicator
				? mc.fontRendererObj.getStringWidth(healthDiffText)
				: 0;
		final float headOffset = showHead ? 25.0f : 0.0f;
		float contentWidth = Math.max(
				nameWidth + (showIndicator ? 2.0f + statusTextWidth + 2.0f : 0.0f),
				healthTextWidth + (showIndicator ? 2.0f + healthDiffWidth + 2.0f : 0.0f));
		final int width = (int) Math.max(headOffset + 70.0f,
				headOffset + 2.0f + contentWidth + 2.0f);
		final int height = 27;
		int healthBarColor = RenderUtil.getHealthColor(healthPercent);
		int healthBarBgColor = RenderUtil.darkenColor(healthBarColor, 0.2f);
		int indicatorColor = RenderUtil.getHealthColor(healthDeltaRatio);
		int indicatorDarkColor = RenderUtil.darkenColor(indicatorColor, 0.8f);
		int alpha = backgroundAlpha;
		int outlineColor = showOutline ? healthBarColor : 0;
		// background
		RenderUtil.drawRect(x, y, x + width, y + height, (alpha << 24));
		// outline
		if (showOutline) {
			RenderUtil.drawRect(x - 1, y - 1, x + width + 1, y, outlineColor);
			RenderUtil.drawRect(x - 1, y + height, x + width + 1, y + height + 1, outlineColor);
			RenderUtil.drawRect(x - 1, y, x, y + height, outlineColor);
			RenderUtil.drawRect(x + width, y, x + width + 1, y + height, outlineColor);
		}
		// health bar
		float barLeft = x + headOffset + 2;
		float barRight = x + width - 2;
		float barWidth = barRight - barLeft;
		RenderUtil.drawRect(barLeft, y + 22, barRight, y + 25, healthBarBgColor);
		RenderUtil.drawRect(barLeft, y + 22, barLeft + barWidth * healthPercent, y + 25,
				healthBarColor);
		// head
		float textX = x + headOffset + 2;
		if (showHead) {
			RenderUtil.renderPlayerHead(entity, x + 2, y + 2, 23);
		}
		// name
		mc.fontRendererObj.drawString(name, textX, y + 2, 0xFFFFFFFF, shadow);
		// health text
		mc.fontRendererObj.drawString(healthText, textX, y + 12, 0xFFFFFFFF, shadow);
		// indicator
		if (showIndicator) {
			// W/L/D
			mc.fontRendererObj.drawString(statusText, x + width - 2 - statusTextWidth, y + 2,
					indicatorColor, shadow);
			// health diff
			mc.fontRendererObj.drawString(healthDiffText, x + width - 2 - healthDiffWidth, y + 12,
					indicatorDarkColor, shadow);
		}
	}

	@Override
	public Position getHudPosition() {
		return position;
	}

	@Override
	public String getHudPlaceholderText() {
		return "Target HUD";
	}

	@Override
	public boolean useHudDropShadow() {
		return dropShadow;
	}
}
