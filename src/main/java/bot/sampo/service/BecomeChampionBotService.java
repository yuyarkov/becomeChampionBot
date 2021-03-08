package bot.sampo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedList;
import java.util.Queue;

@Service
@Slf4j
@RequiredArgsConstructor
public class BecomeChampionBotService extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
    @Value("${app.admin-id}")
    private long adminId;

    private final UserActionReactRouter userActionReactRouter;
    private final Queue<Update> updateQueue = new LinkedList<>();

    @Scheduled(fixedDelay = 50)
    private void handleQueue() {

        Update update;

        while ((update = updateQueue.poll()) != null) {
            try {
                var messages = userActionReactRouter.getReaction(update);
                for (var mes : messages) {
                    execute(mes);
                }
            } catch (TelegramApiException e) {
                log.error("get error onUpdateReceived {}", update, e);
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        replyToTelegram(adminId, update);
        updateQueue.add(update);//чтобы не решать проблемы конкурентного доступа, сделаем невозможным одновременную обработку запросов.
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void replyToTelegram(long chatID, Update update) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(String.valueOf(chatID));
        message.enableMarkdown(true);
        message.setText(updateToString(update));
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            log.error("send message to admin", e);
        }
    }

    private String updateToString(Update update) {

        var callBack = update.getCallbackQuery() == null ? "" : update.getCallbackQuery().getData();
        var user = update.getMessage()==null?update.getCallbackQuery().getFrom():update.getMessage().getFrom();
        var msg = update.getMessage()==null?"":update.getMessage().getText();

        return "new request\n" + userToString(user) + "\n" + String.format("msg: %s,\ncb: %s", msg, callBack);
    }

    private String userToString(User user) {
        return String.format("first name: %s,\nlastName: %s,\nid: %s", user.getFirstName(), user.getLastName(), user.getId());
    }
}
