package com.pluckerpluck.tkibot.db;

import net.dv8tion.jda.core.entities.Guild;

/**
 * DataInterface
 */
public interface DataInterface {

    public GuildOptions getGuildOptions(Guild guild);

}