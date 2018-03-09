package com.pluckerpluck.tkibot.db.mapdb;

import com.pluckerpluck.tkibot.db.DataInterface;
import com.pluckerpluck.tkibot.db.GuildOptions;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import net.dv8tion.jda.core.entities.Guild;

public class MapDBInterface implements DataInterface {

    private final DB db;

    public MapDBInterface(String fileName) {
        db = DBMaker
            .fileDB(fileName)
            .transactionEnable()
            .make();
    }

	@Override
	public GuildOptions getGuildOptions(Guild guild) {
		return new MapDBGuildOptions(db, guild);
    }
    
    public void close() {
        db.close();
    }

}