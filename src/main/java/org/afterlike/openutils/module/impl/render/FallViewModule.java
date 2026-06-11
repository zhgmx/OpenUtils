package org.afterlike.openutils.module.impl.render;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.RenderOverlayEvent;
import org.afterlike.openutils.module.api.Module;
import org.afterlike.openutils.module.api.ModuleCategory;
import org.afterlike.openutils.module.api.hud.Anchor;
import org.afterlike.openutils.module.api.hud.HudModule;
import org.afterlike.openutils.module.api.hud.Position;
import org.afterlike.openutils.module.api.setting.Setting;
import org.afterlike.openutils.module.api.setting.impl.BooleanSetting;
import org.afterlike.openutils.module.api.setting.impl.DescriptionSetting;
import org.afterlike.openutils.module.api.setting.impl.NumberSetting;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.fastbus.Subscribe;

public class FallViewModule extends Module implements HudModule {
	private static final int WHITE_ARGB = 0xFFFFFFFF;
	private final Position position = new Position(0, 50, Anchor.CENTER);
	private final DescriptionSetting description;
	private final BooleanSetting editPosition;
	private final BooleanSetting dropShadow;
	private final NumberSetting damageThreshold;
	private final BooleanSetting disableWhileFlying;
	private final BooleanSetting onlyWhileSneaking;
	private final BooleanSetting showAsHearts;
	private final BooleanSetting showHeartSymbol;
	private final BooleanSetting showDistance;
	private double fallStartY = -1;
	private double groundY = -1;
	private float cachedFallDistance = 0;
	private int cachedEnchantmentModifier = -1;
	private final ItemStack[] cachedArmorInventory = new ItemStack[4];
	private boolean armorCacheValid = false;
	private final ItemStack[] armorBuf = new ItemStack[4];
	private boolean shouldDraw = false;
	private String cachedDamageStr = null;
	private String cachedDistanceStr = null;
	private int cachedDistanceColor = WHITE_ARGB;
	private int lastComputedTick = Integer.MIN_VALUE;
	public FallViewModule() {
		super("Fall View", ModuleCategory.RENDER);
		description = this.registerSetting(new DescriptionSetting("Shows fall distance damage."));
		editPosition = this.registerSetting(new BooleanSetting("Edit position", false));
		dropShadow = this.registerSetting(new BooleanSetting("Drop shadow", true));
		damageThreshold = this
				.registerSetting(new NumberSetting("Damage threshold %", 0.0, 0.0, 100.0, 5.0));
		disableWhileFlying = this.registerSetting(new BooleanSetting("Disable while flying", true));
		onlyWhileSneaking = this.registerSetting(new BooleanSetting("Only while sneaking", false));
		showAsHearts = this.registerSetting(new BooleanSetting("Show as hearts", true));
		showHeartSymbol = this.registerSetting(new BooleanSetting("Show heart symbol", true));
		showDistance = this.registerSetting(new BooleanSetting("Show distance", false));
	}

	@Override
	protected void onDisable() {
		resetState();
	}

	private void resetState() {
		fallStartY = -1;
		groundY = -1;
		cachedFallDistance = 0;
		cachedEnchantmentModifier = -1;
		for (int i = 0; i < 4; i++) {
			cachedArmorInventory[i] = null;
			armorBuf[i] = null;
		}
		armorCacheValid = false;
		shouldDraw = false;
		cachedDamageStr = null;
		cachedDistanceStr = null;
		cachedDistanceColor = WHITE_ARGB;
		lastComputedTick = Integer.MIN_VALUE;
	}

	private boolean canRunLogic() {
		if (!ClientUtil.notNull())
			return false;
		if (mc.currentScreen != null)
			return false;
		if (mc.gameSettings.showDebugInfo)
			return false;
		if (mc.thePlayer.capabilities.isCreativeMode)
			return false;
		if (disableWhileFlying.getValue() && mc.thePlayer.capabilities.allowFlying)
			return false;
		return !onlyWhileSneaking.getValue() || mc.thePlayer.isSneaking();
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (!canRunLogic())
			return;
		final int tick = mc.thePlayer.ticksExisted;
		if (tick == lastComputedTick)
			return;
		updateCache();
		lastComputedTick = tick;
	}

	@Subscribe
	private void onRender(final RenderOverlayEvent event) {
		if (!canRunLogic())
			return;
		final int tick = mc.thePlayer.ticksExisted;
		if (tick != lastComputedTick) {
			updateCache();
			lastComputedTick = tick;
		}
		if (!shouldDraw || cachedDamageStr == null)
			return;
		final int x = position.getX();
		final int y = position.getY();
		final boolean shadow = useHudDropShadow();
		mc.fontRendererObj.drawString(cachedDamageStr, x, y, WHITE_ARGB, shadow);
		if (showDistance.getValue() && cachedDistanceStr != null) {
			mc.fontRendererObj.drawString(cachedDistanceStr, x,
					y + mc.fontRendererObj.FONT_HEIGHT + 2, cachedDistanceColor, shadow);
		}
	}

	private void updateCache() {
		shouldDraw = false;
		cachedDamageStr = null;
		cachedDistanceStr = null;
		cachedDistanceColor = WHITE_ARGB;
		final int blockX = MathHelper.floor_double(mc.thePlayer.posX);
		final int blockZ = MathHelper.floor_double(mc.thePlayer.posZ);
		final int startY = MathHelper.floor_double(mc.thePlayer.posY);
		final double groundBelow = findGroundY(blockX, blockZ, startY);
		final boolean onGround = mc.thePlayer.onGround;
		final float fallDistance;
		if (onGround) {
			fallStartY = -1;
			groundY = -1;
			cachedFallDistance = 0;
			if (groundBelow == -1)
				return;
			fallDistance = (float) Math.max(0, mc.thePlayer.posY - groundBelow);
		} else {
			if (fallStartY == -1) {
				fallStartY = mc.thePlayer.posY;
				groundY = groundBelow;
				cachedFallDistance = 0;
			} else if (groundBelow != groundY) {
				groundY = groundBelow;
				cachedFallDistance = 0;
			}
			if (fallStartY == -1 || groundY == -1)
				return;
			if (cachedFallDistance == 0) {
				cachedFallDistance = (float) Math.max(0, fallStartY - groundY);
			}
			fallDistance = cachedFallDistance;
		}
		if (fallDistance <= 2.5f)
			return;
		final PotionEffect jumpEffect = mc.thePlayer.getActivePotionEffect(Potion.jump);
		final float jumpAmplifier = (jumpEffect != null) ? (jumpEffect.getAmplifier() + 1) : 0.0f;
		final PotionEffect resistanceEffect = mc.thePlayer.getActivePotionEffect(Potion.resistance);
		final boolean hasResistance = resistanceEffect != null;
		final int resistanceLevel = hasResistance ? resistanceEffect.getAmplifier() + 1 : 0;
		boolean armorChanged = false;
		for (int i = 0; i < 4; i++) {
			final ItemStack currentArmor = mc.thePlayer.inventory.armorItemInSlot(i);
			armorBuf[i] = currentArmor;
			if (cachedArmorInventory[i] != currentArmor) {
				armorChanged = true;
			}
		}
		int enchantmentModifier = cachedEnchantmentModifier;
		if (armorChanged || !armorCacheValid) {
			long totalModifier = 0;
			for (int i = 0; i < 100; i++) {
				int mod = EnchantmentHelper.getEnchantmentModifierDamage(armorBuf,
						DamageSource.fall);
				if (mod > 20)
					mod = 20;
				totalModifier += mod;
			}
			enchantmentModifier = (int) Math.round(totalModifier / 100.0);
			cachedEnchantmentModifier = enchantmentModifier;
			System.arraycopy(armorBuf, 0, cachedArmorInventory, 0, 4);
			armorCacheValid = true;
		}
		float damagePoints = fallDistance - 3.0f - jumpAmplifier;
		double damage = Math.max(0, MathHelper.ceiling_double_int(damagePoints));
		if (hasResistance && damage > 0) {
			final int i = resistanceLevel * 5;
			final int j = 25 - i;
			damage = j * damage / 25.0;
		}
		if (damage > 0 && enchantmentModifier > 0) {
			damage = (25 - enchantmentModifier) * damage / 25.0;
		}
		final int finalDamage = MathHelper.ceiling_double_int(damage);
		if (finalDamage <= 0)
			return;
		final double currentHealth = mc.thePlayer.getHealth();
		final double damagePercent = (double) finalDamage / currentHealth * 100.0;
		if (damagePercent <= damageThreshold.getValue())
			return;
		float displayDamage = finalDamage;
		if (showAsHearts.getValue()) {
			displayDamage = finalDamage / 2.0f;
			displayDamage = round(displayDamage, 1);
		}
		final double percent = (double) finalDamage / currentHealth;
		final String colorCode;
		if (finalDamage >= currentHealth) {
			colorCode = "§4";
		} else if (percent >= 0.7) {
			colorCode = "§c";
		} else if (percent >= 0.5) {
			colorCode = "§6";
		} else if (percent >= 0.3) {
			colorCode = "§e";
		} else {
			colorCode = "§a";
		}
		String damageStr = colorCode + formatNumber(displayDamage);
		if (showHeartSymbol.getValue()) {
			damageStr += "§c❤§r";
		}
		cachedDamageStr = damageStr;
		shouldDraw = true;
		if (showDistance.getValue()) {
			cachedDistanceColor = getDistanceColorArgb(fallDistance);
			cachedDistanceStr = formatNumber(round(fallDistance, 2)) + "m";
		}
	}

	private double findGroundY(final int x, final int z, final int startY) {
		if (!mc.theWorld.getChunkProvider().chunkExists(x >> 4, z >> 4))
			return -1;
		final Chunk chunk = mc.theWorld.getChunkFromChunkCoords(x >> 4, z >> 4);
		final int lx = x & 15;
		final int lz = z & 15;
		for (int y = startY; y > -1; y--) {
			final Block block = chunk.getBlock(lx, y, lz);
			if (block.getMaterial() != Material.air && block.isCollidable()) {
				return y + 1;
			}
		}
		return -1;
	}

	private static int getDistanceColorArgb(final float distance) {
		final float minDistance = 2.5f;
		final float maxDistance = 20.0f;
		final float normalized = MathHelper
				.clamp_float((distance - minDistance) / (maxDistance - minDistance), 0.0f, 1.0f);
		final int green = (int) (255 * (1.0f - normalized));
		return 0xFF000000 | (255 << 16) | (green << 8);
	}
	private static final float[] POW10 = {1f, 10f, 100f, 1000f};
	private static float round(final float value, final int places) {
		if (places < 0)
			return value;
		final float factor = places < POW10.length ? POW10[places] : (float) Math.pow(10, places);
		return Math.round(value * factor) / factor;
	}

	private static String formatNumber(final float val) {
		return val == (long) val ? Long.toString((long) val) : Float.toString(val);
	}

	@Override
	public void onSettingChanged(final Setting<?> setting) {
		handleHudSettingChanged(setting);
		lastComputedTick = Integer.MIN_VALUE;
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
		return "§c5❤";
	}

	@Override
	public boolean useHudDropShadow() {
		return dropShadow.getValue();
	}
}
