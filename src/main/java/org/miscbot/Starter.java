package org.miscbot;



import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Starter {
    public static void main(String[] args) {
        try {
            MiscBot bot = new MiscBot();
            TelegramBotsLongPollingApplication api = new TelegramBotsLongPollingApplication();
            api.registerBot(bot.getToken(), bot);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
