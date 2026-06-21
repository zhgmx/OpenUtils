package org.afterlike.openutils.platform.command;

import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.afterlike.openutils.OpenUtils;

public final class OpenUtilsCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "openutils";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList("ou");
	}

	@Override
	public String getCommandUsage(final ICommandSender sender) {
		return "/openutils";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void processCommand(final ICommandSender sender, final String[] args) {
		OpenUtils.get().getConfigHandler().openGuiDelayed();
	}
}
