package com.pluckerpluck.tkibot;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.pluckerpluck.tkibot.dpsreport.DPSReport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    static Logger logger = LoggerFactory.getLogger(ParseMessageAttachements.class);
    
    private MessageReceivedEvent event;

    public ParseMessageAttachements(MessageReceivedEvent event) {
        this.event = event;
    }

    @Nullable
    private DPSReport processAttachement(Attachment attachment) {

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
                logger.error("Response not successful: {}", response);
                response.close();
                return null;
            }
            logger.info("{}", response);
            Gson gson = new Gson();
            String responseBody = response.body().string();
            DPSReport report = gson.fromJson(responseBody, DPSReport.class);

            if (report == null) {
                System.out.println(responseBody);
                response.close();
                return null;
            } else {
                response.close();
                return report;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void run() {
        MessageBuilder message = new MessageBuilder();
        Attachment attachment = event.getMessage().getAttachments().get(0);
        // Look for correct extensions
        if (attachment.getFileName().endsWith(".evtc") || attachment.getFileName().endsWith(".evtc.zip")) {
            event.getTextChannel().sendTyping().queue();
            DPSReport report = processAttachement(attachment);
            
            if (report.hasError()) {
                message.append("An error has occurred: \n").append(report.getError());
            } else {
                String url = report.getPermalink();
                String bossName = report.getBossName();
                message.append(bossName).append(": ").append(url);
                // No Delete Method
                if (!event.getMessage().getContentRaw().contains("noDelete")) {
                    event.getMessage().delete().queue();
                }
            }
            event.getTextChannel().sendMessage(message.build()).queue();
        }
    }

}