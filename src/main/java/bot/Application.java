package bot;

import bot.repository.Converter;
import bot.service.BecomeChampionBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;


@SpringBootApplication
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    public static BecomeChampionBotService exchange = new BecomeChampionBotService();

    public static void main(String[] args) {
        setupLog4J();

        try {
            DancerBase.dancerBase = Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(exchange);
        } catch (TelegramApiException e) {
            log.error("error main", e);
        }

    }

    private static void setupLog4J() {
        try {
            System.setProperty("log4j.configuration", new File(".", File.separatorChar + "log4j.properties").toURL().toString());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            System.setProperty("log4j.debug", new File(".", File.separatorChar + "log4j.debug").toURL().toString());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
