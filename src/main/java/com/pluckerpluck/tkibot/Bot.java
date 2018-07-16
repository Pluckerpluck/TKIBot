package com.pluckerpluck.tkibot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

import com.pluckerpluck.tkibot.commands.SetStreamerRole;
import com.pluckerpluck.tkibot.db.DataInterface;
import com.pluckerpluck.tkibot.db.mapdb.MapDBInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.message.MessageEmbedEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.handle.EventCache.Type;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Bot extends ListenerAdapter {
  static Logger logger = LoggerFactory.getLogger(Bot.class);


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

    CommandClientBuilder builder = new CommandClientBuilder();
    builder.setOwnerId("118825758184439808");

    // Set your bot's prefix
    builder.setPrefix("^");

    builder.addCommand(new SetStreamerRole(dataInterface));

    CommandClient client = builder.build();

    JDA jda = new JDABuilder(AccountType.BOT).setToken(DISCORD_TOKEN).buildBlocking();
    jda.addEventListener(new Bot(dataInterface));
    jda.addEventListener(client);

    logger.info("TKI Bot Ready!");

    // Multithread safe exit
    System.in.read();
    dataInterface.close();
    jda.shutdown();
    System.exit(0);
  }

  private final DataInterface dataInterface;

  public Bot(DataInterface dataInterface) {
    this.dataInterface = dataInterface;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    // Looking for any uploaded files
    if (event.getMessage().getAttachments().size() > 0) {
      Thread thread = new Thread(new ParseMessageAttachements(event));
      thread.start();
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
      if (url != null && !game.equals(event.getPreviousGame())) {
        logger.info("{} is now streaming", event.getMember().getNickname());
        guild.getController().addSingleRoleToMember(event.getMember(), role).queue();
        return;
      }
    }
    if(event.getMember().getRoles().contains(role)) {
      logger.info("{} is no longer streaming", event.getMember().getNickname());
      guild.getController().removeSingleRoleFromMember(event.getMember(), role).queue();
    }
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
