package bot.service;

import bot.react.UserActionReactRouter;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class BecomeChampionBotService extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
    @Value("${app.admin-id}")
    private long adminId;

    private final bot.react.UserActionReactRouter userActionReactRouter = new UserActionReactRouter();

    @Override
    public void onUpdateReceived(Update update) {
        replyToTelegram(adminId, update.toString());

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
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void replyToTelegram(long chatID, String text) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(String.valueOf(chatID));
        message.enableMarkdown(true);
        message.setText(text);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            log.error("send message to admin");
        }
    }
}
