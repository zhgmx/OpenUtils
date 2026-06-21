package org.afterlike.openutils.platform.mixin.minecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.afterlike.openutils.OpenUtils;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.*;
import org.afterlike.openutils.feature.impl.render.FreeLookFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.UpdateUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Unique boolean ou$pendingUpdateNotification;
	@Unique String ou$url = "https://github.com/polariscli/OpenUtils/releases/latest";
	@Inject(method = "startGame", at = @At("HEAD"))
	private void startGame$head(final CallbackInfo callbackInfo) {
		OpenUtils.get().initialize();
	}

	@Inject(method = "startGame", at = @At(value = "CONSTANT", args = "stringValue=Post startup"))
	private void ou$startGame$postStartup(final CallbackInfo ci) {
		OpenUtils.get().lateInitialize();
	}

	@Inject(method = "updateFramebufferSize", at = @At("RETURN"))
	private void ou$updateFramebufferSize(final CallbackInfo ci) {
		OpenUtils.get().getEventBus().post(new ResizeWindowEvent());
	}

	@Inject(method = "loadWorld*", at = @At("HEAD"))
	private void onWorldLoad(WorldClient worldClient, CallbackInfo ci) {
		if (worldClient != null) {
			OpenUtils.get().getEventBus().post(new WorldLoadEvent(worldClient));
		}
		if (UpdateUtil.getLatest() != null && OpenUtils.get().isOutdated()
				&& !OpenUtils.get().isNotified()) {
			ou$pendingUpdateNotification = true;
		}
	}

	@Inject(method = "runTick", at = @At(value = "INVOKE",
			target = "Lorg/lwjgl/input/Keyboard;next()Z", shift = At.Shift.AFTER, remap = false))
	private void ou$runTick$Keyboard$next(CallbackInfo ci) {
		int keyCode = Keyboard.getEventKey();
		boolean pressed = Keyboard.getEventKeyState();
		OpenUtils.get().getEventBus().post(new KeyPressEvent(keyCode, pressed));
	}

	@Inject(method = "runTick", at = @At(value = "INVOKE",
			target = "Lorg/lwjgl/input/Mouse;next()Z", shift = At.Shift.AFTER, remap = false))
	private void ou$runTick$Mouse$next(CallbackInfo ci) {
		int button = Mouse.getEventButton();
		boolean state = Mouse.getEventButtonState();
		int dWheel = Mouse.getEventDWheel();
		if (dWheel != 0) {
			OpenUtils.get().getEventBus().post(new MouseScrollEvent(dWheel));
		}
		if (button >= 0) {
			OpenUtils.get().getEventBus().post(new MouseButtonEvent(button, state));
		}
	}

	@Inject(method = "runTick", at = @At("HEAD"))
	private void ou$runTick$head(final CallbackInfo ci) {
		OpenUtils.get().getEventBus().post(new GameTickEvent(EventPhase.PRE));
		if (ou$pendingUpdateNotification && ClientUtil.notNull()) {
			ClientUtil.sendMessage("&fUpdate available: &aOpenUtils " + UpdateUtil.getLatest());
			ClientUtil.sendMessage(
					"&7You are currently on: &eOpenUtils " + OpenUtils.get().getVersion());
			ChatComponentText component = new ChatComponentText(
					ClientUtil.getPrefix() + "§b§n" + ou$url);
			component.getChatStyle()
					.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
							"https://github.com/polariscli/OpenUtils/releases/latest"))
					.setChatHoverEvent(
							new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
									EnumChatFormatting.GREEN + "Click to view latest release ↗")));
			ClientUtil.sendChatComponent(component);
			OpenUtils.get().setNotified(true);
			ou$pendingUpdateNotification = false;
		}
	}

	@Inject(method = "runTick", at = @At("RETURN"))
	private void ou$runTick$return(final CallbackInfo ci) {
		OpenUtils.get().getEventBus().post(new GameTickEvent(EventPhase.POST));
	}

	@Redirect(method = "runTick",
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/client/settings/GameSettings;thirdPersonView:I",
					opcode = Opcodes.PUTFIELD))
	private void ou$runTick$thirdPersonView(final GameSettings instance, final int value) {
		if (OpenUtils.get().getFeatureHandler().isEnabled(FreeLookFeature.class)) {
			OpenUtils.get().getFeatureHandler().getFeature(FreeLookFeature.class).reset();
		} else {
			instance.thirdPersonView = value;
		}
	}
}
