package bot.sampo.util;

import bot.sampo.service.UserActionReactRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BotRequestHelper {

    private final UserActionReactRouter reactRouter;

    public BotPluralResponse sendMsg(String msg, TelegramUser telegramUser) {
        printAction(null, msg, telegramUser);
        var user = ClassCreationUtil.createUser(telegramUser.getFirstName(), telegramUser.getLastName());
        var update = ClassCreationUtil.createUpdate(ClassCreationUtil.createMessage(telegramUser.getId(), msg, user), null);
        var reactions = reactRouter.getReaction(update);
        return mergeReactions(reactions);
    }

    public BotPluralResponse pushButton(BotResponse botResponse, String buttonId, TelegramUser telegramUser) {
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

    private BotPluralResponse mergeReactions(List<SendMessage> reactions) {
        var responseMap = new HashMap<Long, BotResponse>();

        reactions.forEach(v -> {
            responseMap.computeIfAbsent(Long.parseLong(v.getChatId()), BotResponse::new);
            var resp = responseMap.get(Long.parseLong(v.getChatId()));
            resp.setButtons(ClassCreationUtil.extractCallback(v.getReplyMarkup()));
            resp.setMessage((resp.getMessage() == null ? "" : resp.getMessage() + "\n") + v.getText());
        });

        printReactions(responseMap);

        return BotPluralResponse.builder().responseMap(responseMap).build();
    }

    private void printReactions(Map<Long, BotResponse> reactions) {
        reactions.forEach((key, value) -> {
            System.out.println("\n ---------RESPONSE--------");
            System.out.println("\n Client: " + key);
            System.out.println("\n TEXT: ");
            System.out.println("\n" + value.getMessage());
            System.out.println("\n Buttons: ");
            System.out.println("\n" + value.getButtons());
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
