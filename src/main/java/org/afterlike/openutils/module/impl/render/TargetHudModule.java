package org.afterlike.openutils.module.impl.render;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.entity.player.EntityPlayer;
import org.afterlike.openutils.event.impl.AttackEntityEvent;
import org.afterlike.openutils.event.impl.RenderOverlayEvent;
import org.afterlike.openutils.event.impl.WorldLoadEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.hud.HudModule;
import org.afterlike.openutils.module.api.hud.Position;
import org.afterlike.openutils.module.api.setting.Setting;
import org.afterlike.openutils.module.api.setting.impl.BooleanSetting;
import org.afterlike.openutils.module.api.setting.impl.NumberSetting;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.game.RenderUtil;
import re.tsuku.fastbus.Subscribe;

public class TargetHudModule extends Module implements HudModule {
	private static final DecimalFormat HEALTH_FORMAT = new DecimalFormat("0.0",
			new DecimalFormatSymbols(Locale.US));
	private static final DecimalFormat DIFF_FORMAT = new DecimalFormat("+0.0;-0.0",
			new DecimalFormatSymbols(Locale.US));
	private static final long ANIM_DURATION_MS = 150L;
	private final Position position = new Position(400, 300);
	private final BooleanSetting editPosition;
	private final BooleanSetting dropShadow;
	private final NumberSetting persistTime;
	private final NumberSetting backgroundAlpha;
	private final BooleanSetting showHead;
	private final BooleanSetting showOutline;
	private final BooleanSetting showIndicator;
	private EntityPlayer target;
	private EntityPlayer previousTarget;
	private long lastAttackTime;
	private long animStartTime;
	private float oldHealth;
	private float newHealth;
	private float maxHealth;
	public TargetHudModule() {
		super("Target HUD", ModuleCategory.RENDER);
		editPosition = this.registerSetting(new BooleanSetting("Edit position", false));
		dropShadow = this.registerSetting(new BooleanSetting("Drop shadow", true));
		persistTime = this
				.registerSetting(new NumberSetting("Persist Time (s)", 3.0, 0.5, 10.0, 0.5));
		backgroundAlpha = this
				.registerSetting(new NumberSetting("Background Alpha", 64.0, 0.0, 255.0, 1.0));
		showHead = this.registerSetting(new BooleanSetting("Show Head", true));
		showOutline = this.registerSetting(new BooleanSetting("Show Outline", false));
		showIndicator = this.registerSetting(new BooleanSetting("Show Indicator", true));
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
				|| (currentTime - lastAttackTime > persistTime.getValue() * 1000)) {
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
		final int statusTextWidth = showIndicator.getValue()
				? mc.fontRendererObj.getStringWidth(statusText)
				: 0;
		final int healthDiffWidth = showIndicator.getValue()
				? mc.fontRendererObj.getStringWidth(healthDiffText)
				: 0;
		final float headOffset = showHead.getValue() ? 25.0f : 0.0f;
		float contentWidth = Math.max(
				nameWidth + (showIndicator.getValue() ? 2.0f + statusTextWidth + 2.0f : 0.0f),
				healthTextWidth
						+ (showIndicator.getValue() ? 2.0f + healthDiffWidth + 2.0f : 0.0f));
		final int width = (int) Math.max(headOffset + 70.0f,
				headOffset + 2.0f + contentWidth + 2.0f);
		final int height = 27;
		int healthBarColor = RenderUtil.getHealthColor(healthPercent);
		int healthBarBgColor = RenderUtil.darkenColor(healthBarColor, 0.2f);
		int indicatorColor = RenderUtil.getHealthColor(healthDeltaRatio);
		int indicatorDarkColor = RenderUtil.darkenColor(indicatorColor, 0.8f);
		int alpha = backgroundAlpha.getValue().intValue();
		int outlineColor = showOutline.getValue() ? healthBarColor : 0;
		// background
		RenderUtil.drawRect(x, y, x + width, y + height, (alpha << 24));
		// outline
		if (showOutline.getValue()) {
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
		if (showHead.getValue()) {
			RenderUtil.renderPlayerHead(entity, x + 2, y + 2, 23);
		}
		// name
		mc.fontRendererObj.drawString(name, textX, y + 2, 0xFFFFFFFF, shadow);
		// health text
		mc.fontRendererObj.drawString(healthText, textX, y + 12, 0xFFFFFFFF, shadow);
		// indicator
		if (showIndicator.getValue()) {
			// W/L/D
			mc.fontRendererObj.drawString(statusText, x + width - 2 - statusTextWidth, y + 2,
					indicatorColor, shadow);
			// health diff
			mc.fontRendererObj.drawString(healthDiffText, x + width - 2 - healthDiffWidth, y + 12,
					indicatorDarkColor, shadow);
		}
	}

	@Override
	public void onSettingChanged(final Setting<?> setting) {
		handleHudSettingChanged(setting);
		super.onSettingChanged(setting);
	}

	@Override
	public Position getHudPosition() {
		return position;
	}

	@Override
	public BooleanSetting getHudEditSetting() {
		return editPosition;
	}

	@Override
	public String getHudPlaceholderText() {
		return "Target HUD";
	}

	@Override
	public boolean useHudDropShadow() {
		return dropShadow.getValue();
	}
}
