package org.afterlike.openutils.platform;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.afterlike.openutils.platform.command.OpenUtilsCommand;

@Mod(modid = "openutils", useMetadata = true)
public class ForgeModEntry {
	@Mod.EventHandler
	public void initialize(final FMLInitializationEvent event) {
		ClientCommandHandler.instance.registerCommand(new OpenUtilsCommand());
	}
}
