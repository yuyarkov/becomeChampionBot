package bot;

import bot.react.UserActionReactRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BecomeChampionBotService extends TelegramLongPollingBot {

    private String botToken = System.getenv("TOKEN_BECOME_CHAMPION_BOT");
    public static final long CHAT_ID_DETOCHKIN = 238349375L;//id моего чата, где я получаю все уведомления от программы

    private final bot.react.UserActionReactRouter userActionReactRouter = new UserActionReactRouter();
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Override
    public void onUpdateReceived(Update update) {
        replyToTelegram(CHAT_ID_DETOCHKIN, update.toString());

        try {

            var messages = userActionReactRouter.getReaction(update);
            for (var mes : messages) {
                execute(mes);
            }


        } catch (TelegramApiException e) {
            log.error("get error onUpdateReceived {}", update, e);
        }
    }

    @Override
    public String getBotUsername() {
        return "becomeChampionBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void replyToTelegram(long chatID, String text) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(String.valueOf(chatID));
        message.enableMarkdown(true);
        message.setText(text);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
