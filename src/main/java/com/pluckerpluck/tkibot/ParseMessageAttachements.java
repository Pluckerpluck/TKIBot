package com.pluckerpluck.tkibot;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ParseMessageAttachements implements Runnable {

    private MessageReceivedEvent event;

    public ParseMessageAttachements(MessageReceivedEvent event) {
        this.event = event;
    }

    @Nullable
    private String processAttachement(Attachment attachment) {

        byte[] fileData = new byte[] {};
        try (InputStream stream = attachment.getInputStream()) {
            fileData = stream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient.Builder()
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file",
                attachment.getFileName(), RequestBody.create(MediaType.parse("application/octet-stream"), fileData))
                .build();

        Request request = new Request.Builder().url("https://dps.report/uploadContent?json=1").post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println(response);
                response.close();
                return null;
            }
            JsonObject jsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();
            JsonElement jsonURL = jsonObject.get("permalink");
            if (jsonURL == null) {
                System.out.println(response);
                response.close();
                return null;
            } else {
                response.close();
                return jsonURL.getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void run() {
        MessageBuilder message = new MessageBuilder();
        message.append("DPS Log: ");
        Attachment attachment = event.getMessage().getAttachments().get(0);
        // Look for correct extensions
        if (attachment.getFileName().endsWith(".evtc") || attachment.getFileName().endsWith(".evtc.zip")) {
            String url = processAttachement(attachment);
            if (url == null) {
                event.getTextChannel().sendMessage("An error has occurred :(").queue();
                return;
            } else {
                message.append(url).append("\n");
            }
        
            // No Delete Method
            if (!event.getMessage().getContentRaw().contains("noDelete")) {
                event.getMessage().delete().queue();
            }
            event.getTextChannel().sendMessage(message.build()).queue();
        }
    }

}