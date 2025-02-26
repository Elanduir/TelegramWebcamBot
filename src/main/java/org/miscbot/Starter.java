package org.miscbot;



import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Starter {
    public static void main(String[] args) {
        try{
            TelegramBotsLongPollingApplication api = new TelegramBotsLongPollingApplication();
            MiscBot bot = new MiscBot();
            api.registerBot(bot.getToken(), bot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
