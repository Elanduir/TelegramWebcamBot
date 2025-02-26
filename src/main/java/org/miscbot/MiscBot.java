package org.miscbot;


import org.miscbot.services.GeocodingService;
import org.miscbot.services.SrfMeteoService;
import org.miscbot.services.WeatherService;
import org.miscbot.services.WebcamService;
import org.miscbot.util.Forecast;
import org.miscbot.util.Mode;
import org.miscbot.util.Webcam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class MiscBot implements LongPollingSingleThreadUpdateConsumer {
    private final String botToken = System.getenv("MISC_BOT_TOKEN");
    private final String botDebugToken = System.getenv("MISC_BOT_TOKEN_DEBUG");
    private final String windyToken = System.getenv("WINDY_TOKEN");
    private final String geocodingToken = System.getenv("GEOCODING_TOKEN");
    private final String weatherToken = System.getenv("WEATHER_TOKEN");
    private final String COMMAND_WEBCAM = "/webcam";
    private final String COMMAND_WEBCAM_SHORT = "/w";
    private final String COMMAND_WEBCAM_NR = "/wn";
    private final String COMMAND_START = "/start";
    private final String COMMAND_FORECAST = "/forecast";
    private final Mode mode = Mode.getMode(botDebugToken);

    private static final Logger logger = LoggerFactory.getLogger(MiscBot.class);

    TelegramClient client;
    WebcamService webcamService = new WebcamService(windyToken);
    GeocodingService geocodingService = new GeocodingService(geocodingToken);
    WeatherService weatherService = new WeatherService(weatherToken);

    public MiscBot() {
        logger.info("BotToken: {}", botToken);
        logger.info("WindyToken: {}", windyToken);
        logger.info("GeocodingToken: {}", geocodingToken);
        logger.info("WeatherToken: {}", weatherToken);
        logger.info("Bot running in {} mode", mode.toString());

        this.client = new OkHttpTelegramClient(getToken());
    }

    @Override
    public void consume(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            logRequest(update);
            switch (update.getMessage().getText()) {
                case String s when s.startsWith(COMMAND_START) -> handleStart(update);
                case String s when s.startsWith(COMMAND_WEBCAM_NR) -> handleWebcamNr(update);
                case String s when s.startsWith(COMMAND_WEBCAM) || s.startsWith(COMMAND_WEBCAM_SHORT) -> handleWebcam(update);
                case String s when s.startsWith(COMMAND_FORECAST) -> handleForecast(update);
                default -> logger.info("invalid command detected: {}", update.getMessage().getText());
            }
        }
    }

    private void handleStart(Update update) {
        var message = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("""
                        You can start querying for webcams by using:
                        /webcam {location}
                        /w {location}
                        /wn {limit} {location}""")
                .build();
        try {
            client.execute(message);
        }catch (Exception e) {
            logger.error("Error during /start command: {}", e.getMessage());
        }
    }

    private void handleWebcam(Update update) {
        handleWebcamLimit(update, 1);
    }

    private void handleWebcamNr(Update update) {
         var split = update.getMessage().getText().replace(COMMAND_WEBCAM_NR, "").trim().split(" ");
         if(split.length != 0){
             try{
                 var nr = Integer.parseInt(split[0]);
                 update.getMessage().setText("/wn " + Arrays.stream(split).skip(1).collect(Collectors.joining(" ")));
                 handleWebcamLimit(update, nr);
             }catch (NumberFormatException e){
                 logger.info("Invalid command: {}", update.getMessage().getText());
             }
         }
    }

    private void handleWebcamLimit(Update update, int limit) {
        Object message;
        List<Webcam> webcams = new ArrayList<>();
        var inbMessage = update.getMessage().getText()
                .replace(COMMAND_WEBCAM_NR, "")
                .replace(COMMAND_WEBCAM, "")
                .replace(COMMAND_WEBCAM_SHORT, "")
                .trim()
                .replace(" ", "%20");
        try {
            webcams = getAllWebcams(inbMessage, limit);
            var inputMedia = webcams.stream().map(webcam -> {
                var iM = new InputMediaPhoto(webcam.getCurrentImage(), webcam.getTitle());
                iM.setCaption(getCaption(webcam));
                iM.setParseMode(ParseMode.HTML);
                return iM;
            }).toList();
            if(inputMedia.isEmpty()){
                throw new NoSuchElementException("No webcams found");
            }else if (inputMedia.size() == 1) {
                message = SendPhoto.builder()
                        .caption(getCaption(webcams.getFirst()))
                        .parseMode(ParseMode.HTML)
                        .chatId(update.getMessage().getChatId())
                        .photo(new InputFile(webcams.getFirst().getCurrentImage())).build();
            } else {
                message = SendMediaGroup.builder()
                        .chatId(update.getMessage().getChatId())
                        .medias(inputMedia).build();
            }
        } catch (Exception e) {
            message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(e.getMessage())
                    .build();
        }
        try {
            if (message instanceof SendPhoto) {
                client.execute((SendPhoto) message);
            } else if (message instanceof SendMediaGroup) {
                client.execute((SendMediaGroup) message);
            } else if (message != null) {
                client.execute((SendMessage) message);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }finally {
            webcams.forEach(Webcam::cleanup);
        }
    }

    private void handleForecast(Update update) {
        var inbLocation = update.getMessage().getText().replace(COMMAND_FORECAST, "").trim().replace(" ", "%20");
        SendMessage message;
        var location = geocodingService.getLocation(inbLocation);



        try {
            var forecasts = weatherService.getForecasts(location);
            String template = """
                    <strong>Date</strong>: %s
                    <strong>Temp °C</strong> l|h|a: %s | %s | %s
                    <strong>Rain</strong> %%|mm: %s | %s
                    <strong>Windspeed max</strong> kmh: %s
                    """;
            logger.debug("forecasts: {}", forecasts);
            List<String> texts = forecasts.stream().map(forecast -> String.format(
                    template,
                    forecast.getDate(),
                    forecast.getDay().getMaxtemp_c(), forecast.getDay().getMintemp_c(), forecast.getDay().getAvgtemp_c(),
                    forecast.getDay().getDaily_chance_of_rain(), forecast.getDay().getTotalprecip_mm(),
                    forecast.getDay().getMaxwind_kph()
            )).toList();

            var msgText =
                    String.format("<strong>%s</strong>", location.getDisplay_name())  + "\n" +
                            String.join("\n", texts);
            message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(msgText)
                    .parseMode(ParseMode.HTML)
                    .build();


        }catch (Exception e){
            e.printStackTrace();
            message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Could not find forecast for requested location")
                    .build();
        }
        try{
            client.execute(message);
        }catch (Exception e){
            logger.error("Error during /forecast command: {}", e.getMessage());
        }
    }

    private List<Webcam> getAllWebcams(String location, int limit) throws Exception {
        return webcamService.getImage(geocodingService.getLocation(location), limit);

    }

    private String getCaption(Webcam webcam) {
        return String.format("<strong>Location found</strong>:\n%s\n\n<strong>Webcam Location</strong>:\n%s @ %s\n\n<strong>Links</strong>:\n%s\n%s",
                webcam.getOriginalLocation().getDisplay_name(),
                webcam.getTitle(),
                webcam.getLastUpdatedOn(),
                webcam.getWebcamUrlDetail(),
                webcam.getWebcamUrlProvider()
        );
    }

    private void logRequest(Update update) {
        logger.info(
                "\nIncoming Request:\nFrom: {}\nUsername: {}\nCommand: {}\n",
                update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName(),
                update.getMessage().getFrom().getUserName(),
                update.getMessage().getText());
    }

    protected String getToken() {
        return mode == Mode.PROD ? botToken : botDebugToken;
    }
}
