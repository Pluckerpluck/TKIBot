package com.pluckerpluck.tkibot.db;

import javax.annotation.Nullable;

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

/**
 * GuildOptions
 */
public abstract class GuildOptions {

    protected final Guild guild;

    public GuildOptions(Guild guild) {
        this.guild = guild;
    }

    @Nullable
    public abstract Role getDiscordStreamersRole();

    @Nullable
    public abstract Channel getDiscordStreamersAnnouncementChannel();

    public abstract void setDiscordStreamersRole(Role role);

}