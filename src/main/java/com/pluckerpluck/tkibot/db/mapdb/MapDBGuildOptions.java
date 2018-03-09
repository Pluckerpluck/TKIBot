package com.pluckerpluck.tkibot.db.mapdb;

import java.util.Map;

import com.pluckerpluck.tkibot.db.GuildOptions;

import org.mapdb.DB;
import org.mapdb.Serializer;

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

public class MapDBGuildOptions extends GuildOptions {

    private final DB db;
    private final Map<String, String> options;

    public MapDBGuildOptions(DB db, Guild guild) {
        super(guild);
        this.db = db;
        this.options = db.hashMap(guild.getId() + "_options", Serializer.STRING, Serializer.STRING)
            .createOrOpen();
    }

	@Override
	public Role getDiscordStreamersRole() {
        String roleID = options.get("streamerRole");

        if (roleID == null) {
            return null;
        }

        return guild.getRoleById(roleID);
	}

	@Override
	public Channel getDiscordStreamersAnnouncementChannel() {
        String channelID = options.get("streamerAnnouncementChannel");

        if (channelID == null) {
            return null;
        }

        return guild.getTextChannelById(channelID);
    }
    
    @Override
    public void setDiscordStreamersRole(Role role) {
        if (role == null) {
            options.remove("streamerRole");
        } else {
            options.put("streamerRole", role.getId());
        }
        db.commit();
    }

}