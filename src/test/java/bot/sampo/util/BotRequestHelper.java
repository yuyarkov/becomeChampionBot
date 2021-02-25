package bot.sampo.util;

import bot.sampo.service.UserActionReactRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotRequestHelper {

    private final UserActionReactRouter reactRouter;

    public BotResponse sendMsg(String msg, TelegramUser telegramUser) {
        printAction(null, msg, telegramUser);
        var user = ClassCreationUtil.createUser(telegramUser.getFirstName(), telegramUser.getLastName());
        var update = ClassCreationUtil.createUpdate(ClassCreationUtil.createMessage(telegramUser.getId(), msg, user), null);
        var reactions = reactRouter.getReaction(update);
        return mergeReactions(reactions);
    }

    public BotResponse pushButton(BotResponse botResponse, String buttonId, TelegramUser telegramUser) {
        printAction(buttonId, null, telegramUser);
        if (!botResponse.getButtons().contains(buttonId))
            throw new RuntimeException("missing test button exception. Expect " + buttonId + " but was " + botResponse.getButtons());

        var user = ClassCreationUtil.createUser(telegramUser.getFirstName(), telegramUser.getLastName());
        var msg = ClassCreationUtil.createMessage(telegramUser.getId(), null, user);
        var callBack = ClassCreationUtil.createCallbackQuery(buttonId, msg);
        var update = ClassCreationUtil.createUpdate(msg, callBack);

        var reactions = reactRouter.getReaction(update);
        return mergeReactions(reactions);

    }

    private BotResponse mergeReactions(List<SendMessage> reactions) {
        printReactions(reactions);
        var resp = new BotResponse();
        reactions.forEach(v -> {
            resp.setButtons(ClassCreationUtil.extractCallback(v.getReplyMarkup()));
            resp.setMessage(resp.getMessage() + "\n" + v.getText());
        });
        return resp;
    }

    private void printReactions(List<SendMessage> reactions) {
        reactions.forEach(r -> {

            System.out.println("\n ---------RESPONSE--------");
            System.out.println("\n TEXT: ");
            System.out.println("\n" + r.getText());
            System.out.println("\n Buttons: ");
            System.out.println("\n" + ClassCreationUtil.extractCallback(r.getReplyMarkup()));

        });
    }

    private void printAction(String buttonId, String message, TelegramUser telegramUser) {

        System.out.println("\n --------REQUEST---------");
        System.out.println("\n User: " + telegramUser);
        if (message != null) {
            System.out.println("\n TEXT: ");
            System.out.println("\n" + message);
        }
        if (buttonId != null) {
            System.out.println("\n Button: " + buttonId);
        }

    }

}
