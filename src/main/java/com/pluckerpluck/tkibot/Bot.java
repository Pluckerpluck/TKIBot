package com.pluckerpluck.tkibot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.security.auth.login.LoginException;
import com.google.common.base.Strings;
import com.pluckerpluck.tkibot.db.DataInterface;
import com.pluckerpluck.tkibot.db.mapdb.MapDBInterface;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {
  public static void main(String[] args)
      throws LoginException, RateLimitedException, InterruptedException, IOException {

    // Get properties file
    Properties prop = loadProperties();

    String DISCORD_TOKEN = prop.getProperty("DISCORD_TOKEN");
    if (Strings.isNullOrEmpty(DISCORD_TOKEN)) {
      System.out.println("Discord token has not been set");
      System.exit(0);
    }

    MapDBInterface dataInterface = new MapDBInterface("data.db");

    JDA jda = new JDABuilder(AccountType.BOT).setToken(DISCORD_TOKEN).buildBlocking();
    jda.addEventListener(new Bot(dataInterface));

    // Multithread safe exit
    System.in.read();
    dataInterface.close();
    jda.shutdown();
  }

  private final DataInterface dataInterface;

  public Bot(DataInterface dataInterface) {
    this.dataInterface = dataInterface;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.isFromType(ChannelType.PRIVATE)) {
      System.out.printf("[PM] %s: %s\n", event.getAuthor().getName(), event.getMessage().getContentRaw());
    } else {
      System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(), event.getTextChannel().getName(),
          event.getMember().getEffectiveName(), event.getMessage().getContentRaw());

      // Hardcode setting this for now
      if (event.getMessage().getContentRaw().startsWith("!setStreamerRole")){
        // Hardcode need for "ModifyRoles" permission
        if(event.getAuthor() == null || !event.getMember().hasPermission(Permission.MANAGE_ROLES)){
          event.getChannel().sendMessage("You don't have permission to change this").queue();
          return;
        }

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
  }

  // Code to update on streamers doing stuff
  @Override
  public void onUserGameUpdate(UserGameUpdateEvent event) {
    Guild guild = event.getGuild();

    Role role = dataInterface.getGuildOptions(guild).getDiscordStreamersRole();

    if (role == null) {
      return;
    }

    Game game = event.getCurrentGame();
    if (game != null) {
      String url = event.getMember().getGame().getUrl();
      if (url != null) {
        guild.getController()
            .addSingleRoleToMember(event.getMember(), role).queue();
        return;
      }
    }
    guild.getController()
        .removeSingleRoleFromMember(event.getMember(), role).queue();
  }

  public static Properties loadProperties() {
    Properties prop = new Properties();

    try (InputStream input = new FileInputStream("config.properties")) {
      prop.load(input);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return prop;
  }
}
