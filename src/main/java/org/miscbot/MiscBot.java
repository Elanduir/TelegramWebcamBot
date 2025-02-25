package org.miscbot;


import org.miscbot.services.GeocodingService;
import org.miscbot.services.WebcamService;
import org.miscbot.util.Webcam;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class MiscBot implements LongPollingSingleThreadUpdateConsumer {
    private final String botToken = System.getenv("MISC_BOT_TOKEN");
    private final String windyToken = System.getenv("WINDY_TOKEN");
    private final String geocodingToken = System.getenv("GEOCODING_TOKEN");
    TelegramClient client = new OkHttpTelegramClient(botToken);
    WebcamService webcamService = new WebcamService(windyToken);
    GeocodingService geocodingService = new GeocodingService(geocodingToken);

    @Override
    public void consume(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            logRequest(update);
            switch (update.getMessage().getText()) {
                case String s when s.startsWith("/webcam ") -> handleWebcam(update);
                case String s when s.startsWith("/start") -> handleStart(update);
                default -> System.out.println("invalid command detected: " + update.getMessage().getText());
            }
        }
    }

    private void logRequest(Update update) {
        System.out.printf("Incoming Request:\nFrom: %s\nUsername: %s\nCommand: %s\n",
                update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName(),
                update.getMessage().getFrom().getUserName(),
                update.getMessage().getText()
        );
        System.out.println("-------------------");
    }

    private void handleStart(Update update) {
        SendMessage message = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("You can query for webcams by using the command: /webcam {location}")
                .build();
        try {
            client.execute(message);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleWebcam(Update update) {
        SendMessage message = null;
        SendMediaGroup mediaGroup = null;
        SendPhoto sendPhoto = null;
        List<Webcam> webcams = new ArrayList<>();
        try {
            var inbMessage = update.getMessage().getText().replace("/webcam ", "").replace(" ", "%20");
            var location = geocodingService.getLocation(inbMessage);
            webcams = webcamService.getImage(location);
            var inputMedia = webcams.stream().map(webcam -> {
                var iM = new InputMediaPhoto(webcam.getCurrentImage(), webcam.getTitle());
                iM.setCaption(getCaption(webcam));
                return iM;
            }).toList();
            if (inputMedia.size() == 1) {
                sendPhoto = SendPhoto.builder()
                        .caption(getCaption(webcams.getFirst()))
                        .chatId(update.getMessage().getChatId())
                        .photo(new InputFile(webcams.getFirst().getCurrentImage())).build();
            } else {
                mediaGroup = SendMediaGroup.builder()
                        .chatId(update.getMessage().getChatId())
                        .medias(inputMedia).build();
            }
            if(sendPhoto == null && mediaGroup == null) {
                throw new NoSuchElementException("Could not find webcams");
            }

        } catch (Exception e) {
            message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(e.getMessage())
                    .build();
        }
        try {
            if(sendPhoto != null) {
                client.execute(sendPhoto);
            } else if (mediaGroup != null) {
                client.execute(mediaGroup);
            } else {
                client.execute(message);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }finally {
            webcams.forEach(Webcam::cleanup);
        }
    }

    private String getCaption(Webcam webcam) {
        return webcam.getTitle() + " @ " + webcam.getLastUpdatedOn() + "\n" + webcam.getImages().getCurrent().getPreview();
    }

    public String sanitize(String input) {
        return input.replace(">", "\\>")
                .replace("<", "\\<")
                .replace("-", "\\-");
    }

    protected String getToken() {
        return botToken;
    }
}
