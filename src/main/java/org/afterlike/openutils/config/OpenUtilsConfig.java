package org.afterlike.openutils.config;

import org.afterlike.openutils.feature.handler.FeatureHandler;
import org.afterlike.openutils.feature.impl.bedwars.ArmorAlertsFeature;
import org.afterlike.openutils.feature.impl.bedwars.FinalKillsHudFeature;
import org.afterlike.openutils.feature.impl.bedwars.ItemAlertsFeature;
import org.afterlike.openutils.feature.impl.bedwars.QuickShopFeature;
import org.afterlike.openutils.feature.impl.bedwars.ResourceCountFeature;
import org.afterlike.openutils.feature.impl.bedwars.TimersHudFeature;
import org.afterlike.openutils.feature.impl.bedwars.UpgradeAlertsFeature;
import org.afterlike.openutils.feature.impl.bedwars.UpgradesHudFeature;
import org.afterlike.openutils.feature.impl.client.DebugFeature;
import org.afterlike.openutils.feature.impl.client.GuiFeature;
import org.afterlike.openutils.feature.impl.client.VPNStatusFeature;
import org.afterlike.openutils.feature.impl.hypixel.AutoGGFeature;
import org.afterlike.openutils.feature.impl.hypixel.DenickerFeature;
import org.afterlike.openutils.feature.impl.hypixel.QuickMathFeature;
import org.afterlike.openutils.feature.impl.movement.NoJumpDelayFeature;
import org.afterlike.openutils.feature.impl.movement.NullMoveFeature;
import org.afterlike.openutils.feature.impl.movement.SprintFeature;
import org.afterlike.openutils.feature.impl.player.ActionSoundsFeature;
import org.afterlike.openutils.feature.impl.player.NoBreakDelayFeature;
import org.afterlike.openutils.feature.impl.player.NoHitDelayFeature;
import org.afterlike.openutils.feature.impl.render.AnimationsFeature;
import org.afterlike.openutils.feature.impl.render.AntiDebuffFeature;
import org.afterlike.openutils.feature.impl.render.AntiShuffleFeature;
import org.afterlike.openutils.feature.impl.render.CameraFeature;
import org.afterlike.openutils.feature.impl.render.CapeFeature;
import org.afterlike.openutils.feature.impl.render.DamageTagsFeature;
import org.afterlike.openutils.feature.impl.render.FreeLookFeature;
import org.afterlike.openutils.feature.impl.render.NameHiderFeature;
import org.afterlike.openutils.feature.impl.render.TargetHudFeature;
import org.afterlike.openutils.feature.impl.render.ThickRodsFeature;
import org.afterlike.openutils.feature.impl.world.TimeChangerFeature;
import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Group;

@Config(name = "OpenUtils", id = "openutils", description = "Mod settings", version = 1)
public final class OpenUtilsConfig {
	@Category(name = "Movement", description = "Movement quality-of-life features.", order = 0)
	public final Movement movement;
	@Category(name = "Player", description = "Player input, combat, and interaction fixes.",
			order = 1)
	public final Player player;
	@Category(name = "Render", description = "Camera, HUD, and visual rendering features.",
			order = 2)
	public final Render render;
	@Category(name = "World", description = "World presentation controls.", order = 3)
	public final World world;
	@Category(name = "Hypixel", description = "Hypixel chat and game helpers.", order = 4)
	public final Hypixel hypixel;
	@Category(name = "Bed Wars", id = "bedwars", description = "Bed Wars alerts and HUDs.",
			order = 5)
	public final BedWars bedWars;
	@Category(name = "Client", description = "Client controls and diagnostics.", order = 6)
	public final Client client;
	public OpenUtilsConfig(final FeatureHandler features) {
		this.movement = new Movement(features);
		this.player = new Player(features);
		this.render = new Render(features);
		this.world = new World(features);
		this.hypixel = new Hypixel(features);
		this.bedWars = new BedWars(features);
		this.client = new Client(features);
	}
	public static final class Movement {
		@Group(name = "No Jump Delay", description = "Removes Minecraft's repeated-jump cooldown.",
				order = 0)
		public final NoJumpDelayFeature noJumpDelay;
		@Group(name = "Null Move",
				description = "Resolves opposing movement keys by honoring the newest press.",
				order = 1)
		public final NullMoveFeature nullMove;
		@Group(name = "Sprint", description = "Keeps sprint held while movement is active.",
				order = 2)
		public final SprintFeature sprint;
		private Movement(final FeatureHandler features) {
			this.noJumpDelay = features.getFeature(NoJumpDelayFeature.class);
			this.nullMove = features.getFeature(NullMoveFeature.class);
			this.sprint = features.getFeature(SprintFeature.class);
		}
	}
	public static final class Player {
		@Group(name = "Action Sounds", description = "Adds small audio cues for combat actions.",
				order = 0)
		public final ActionSoundsFeature actionSounds;
		@Group(name = "No Break Delay", description = "Reduces the cooldown between block breaks.",
				order = 1)
		public final NoBreakDelayFeature noBreakDelay;
		@Group(name = "No Hit Delay", description = "Removes the client-side left-click cooldown.",
				order = 2)
		public final NoHitDelayFeature noHitDelay;
		private Player(final FeatureHandler features) {
			this.actionSounds = features.getFeature(ActionSoundsFeature.class);
			this.noBreakDelay = features.getFeature(NoBreakDelayFeature.class);
			this.noHitDelay = features.getFeature(NoHitDelayFeature.class);
		}
	}
	public static final class Render {
		@Group(name = "Animations", description = "Tweaks first-person item animations.", order = 0)
		public final AnimationsFeature animations;
		@Group(name = "Anti Debuff", description = "Suppresses intrusive potion visuals.",
				order = 1)
		public final AntiDebuffFeature antiDebuff;
		@Group(name = "Anti Shuffle",
				description = "Removes obfuscated text formatting while rendering.", order = 2)
		public final AntiShuffleFeature antiShuffle;
		@Group(name = "Camera", description = "Adjust third-person distance and hurt-camera shake.",
				order = 3)
		public final CameraFeature camera;
		@Group(name = "Cape", description = "Renders the selected cape texture.", order = 4)
		public final CapeFeature cape;
		@Group(name = "Damage Tags", description = "Floating world-space health change tags.",
				order = 5)
		public final DamageTagsFeature damageTags;
		@Group(name = "Free Look", description = "Hold-to-look camera controls for third person.",
				order = 6)
		public final FreeLookFeature freeLook;
		@Group(name = "Name Hider",
				description = "Replaces your visible username in rendered text.", order = 7)
		public final NameHiderFeature nameHider;
		@Group(name = "Target HUD", description = "Compact combat target health HUD.", order = 8)
		public final TargetHudFeature targetHud;
		@Group(name = "Thick Rods", description = "Draws cast fishing lines with extra width.",
				order = 9)
		public final ThickRodsFeature thickRods;
		private Render(final FeatureHandler features) {
			this.animations = features.getFeature(AnimationsFeature.class);
			this.antiDebuff = features.getFeature(AntiDebuffFeature.class);
			this.antiShuffle = features.getFeature(AntiShuffleFeature.class);
			this.camera = features.getFeature(CameraFeature.class);
			this.cape = features.getFeature(CapeFeature.class);
			this.damageTags = features.getFeature(DamageTagsFeature.class);
			this.freeLook = features.getFeature(FreeLookFeature.class);
			this.nameHider = features.getFeature(NameHiderFeature.class);
			this.targetHud = features.getFeature(TargetHudFeature.class);
			this.thickRods = features.getFeature(ThickRodsFeature.class);
		}
	}
	public static final class World {
		@Group(name = "Time Changer", description = "Changes the displayed world time.", order = 0)
		public final TimeChangerFeature timeChanger;
		private World(final FeatureHandler features) {
			this.timeChanger = features.getFeature(TimeChangerFeature.class);
		}
	}
	public static final class Hypixel {
		@Group(name = "Auto GG",
				description = "Automatically sends a short message after games end.", order = 0)
		public final AutoGGFeature autoGG;
		@Group(name = "Denicker",
				description = "Attempts to identify nicked players from skin data.", order = 1)
		public final DenickerFeature denicker;
		@Group(name = "Quick Math", description = "Solves Hypixel Quick Maths prompts.", order = 2)
		public final QuickMathFeature quickMath;
		private Hypixel(final FeatureHandler features) {
			this.autoGG = features.getFeature(AutoGGFeature.class);
			this.denicker = features.getFeature(DenickerFeature.class);
			this.quickMath = features.getFeature(QuickMathFeature.class);
		}
	}
	public static final class BedWars {
		@Group(name = "Armor Alerts", description = "Alerts when enemies buy stronger armor.",
				order = 0)
		public final ArmorAlertsFeature armorAlerts;
		@Group(name = "Final Kills HUD", description = "Tracks final kills during Bed Wars games.",
				order = 1)
		public final FinalKillsHudFeature finalKillsHud;
		@Group(name = "Item Alerts", description = "Alerts when enemies reveal important items.",
				order = 2)
		public final ItemAlertsFeature itemAlerts;
		@Group(name = "Quick Shop", description = "Middle-clicks Bed Wars shop purchases.",
				order = 3)
		public final QuickShopFeature quickShop;
		@Group(name = "Resource Count", description = "Tracks inventory resource count changes.",
				order = 4)
		public final ResourceCountFeature resourceCount;
		@Group(name = "Timers HUD", description = "Shows generator and game timers.", order = 5)
		public final TimersHudFeature timersHud;
		@Group(name = "Upgrade Alerts", description = "Alerts when enemies purchase team upgrades.",
				order = 6)
		public final UpgradeAlertsFeature upgradeAlerts;
		@Group(name = "Upgrades HUD", description = "Shows your team upgrade state.", order = 7)
		public final UpgradesHudFeature upgradesHud;
		private BedWars(final FeatureHandler features) {
			this.armorAlerts = features.getFeature(ArmorAlertsFeature.class);
			this.finalKillsHud = features.getFeature(FinalKillsHudFeature.class);
			this.itemAlerts = features.getFeature(ItemAlertsFeature.class);
			this.quickShop = features.getFeature(QuickShopFeature.class);
			this.resourceCount = features.getFeature(ResourceCountFeature.class);
			this.timersHud = features.getFeature(TimersHudFeature.class);
			this.upgradeAlerts = features.getFeature(UpgradeAlertsFeature.class);
			this.upgradesHud = features.getFeature(UpgradesHudFeature.class);
		}
	}
	public static final class Client {
		@Group(name = "Debug", description = "Diagnostic logging controls.", order = 0)
		public final DebugFeature debug;
		@Group(name = "Config Screen", description = "Config menu controls.", order = 1)
		public final GuiFeature gui;
		@Group(name = "VPN Status", description = "One-shot network status check.", order = 2)
		public final VPNStatusFeature vpnStatus;
		private Client(final FeatureHandler features) {
			this.debug = features.getFeature(DebugFeature.class);
			this.gui = features.getFeature(GuiFeature.class);
			this.vpnStatus = features.getFeature(VPNStatusFeature.class);
		}
	}
}
