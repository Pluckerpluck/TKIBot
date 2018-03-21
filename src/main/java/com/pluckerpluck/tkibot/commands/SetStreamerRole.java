package com.pluckerpluck.tkibot.commands;

import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.pluckerpluck.tkibot.db.DataInterface;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

public class SetStreamerRole extends Command {

    private final DataInterface dataInterface;

    public SetStreamerRole(DataInterface dataInterface) {
        this.dataInterface = dataInterface;

        this.name = "setStreamerRole";
        this.help = "Sets the default role given to live streamers";
        this.arguments = "@roleNameHere";

        this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
    }

	@Override
	protected void execute(CommandEvent event) {
        List<Role> roles = event.getMessage().getMentionedRoles();
        if (roles.size() > 0) {
        dataInterface.getGuildOptions(event.getGuild()).setDiscordStreamersRole(roles.get(0));
        event.getChannel().sendMessage("New streamer role set: " + roles.get(0).getName()).queue();
        } else {
        dataInterface.getGuildOptions(event.getGuild()).setDiscordStreamersRole(null);
        event.getChannel().sendMessage("Streamer role removed").queue();
        }
	}

}