package org.afterlike.openutils.feature.impl.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import org.afterlike.openutils.event.impl.AttackEntityEvent;
import org.afterlike.openutils.event.impl.ReceivePacketEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.fastbus.Subscribe;

public class ActionSoundsFeature extends ToggleableFeature {
	@Option(name = "Enable Action Sounds",
			description = "Play extra audio cues for combat actions you care about.", order = 0)
	public boolean enabled;
	@Option(name = "Blocked damage",
			description = "Play an anvil cue when your block absorbs incoming damage.", order = 1)
	public boolean blocked = true;
	@Option(name = "Critical hit",
			description = "Play a crit cue when you land a falling melee hit.", order = 2)
	public boolean crit = true;
	public ActionSoundsFeature() {
		super("Action Sounds", FeatureCategory.PLAYER);
	}

	@Subscribe
	private void onPacketReceived(final ReceivePacketEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (!blocked)
			return;
		if (event.getPacket() instanceof S19PacketEntityStatus && blocked) {
			S19PacketEntityStatus packet = (S19PacketEntityStatus) event.getPacket();
			if (packet.getOpCode() != 2)
				return;
			if (packet.getEntity(mc.theWorld) != mc.thePlayer)
				return;
			if (!mc.thePlayer.isBlocking())
				return;
			mc.thePlayer.playSound("random.anvil_land", 0.7F, 1.8F);
		}
	}

	@Subscribe
	public void onAttackEntity(final AttackEntityEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (!crit)
			return;
		if (event.getTarget() instanceof EntityPlayer && event.getPlayerIn() == mc.thePlayer) {
			if (!mc.thePlayer.onGround && mc.thePlayer.fallDistance > 0.0F
					&& !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava()
					&& !mc.thePlayer.isRiding()) {
				mc.thePlayer.playSound("openutils:crit", 0.7F, 1.0F);
			}
		}
	}
}
