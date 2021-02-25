package bot.sampo.util;

import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassCreationUtil {

    public static Set<String> extractCallback(ReplyKeyboard keyboardMarkup) {
        if (keyboardMarkup == null)
            return Set.of();

        return ((InlineKeyboardMarkup) keyboardMarkup).getKeyboard().stream()
                .flatMap(List::stream)
                .map(InlineKeyboardButton::getCallbackData)
                .collect(Collectors.toSet());
    }

    public static Message createMessage(long chatId, String msg, User from) {

        var c = new Chat();
        c.setId(chatId);

        var m = new Message();
        m.setChat(c);
        m.setText(msg);
        m.setFrom(from);
        return m;
    }

    public static Update createUpdate(Message message, CallbackQuery callbackQuery) {
        var u = new Update();
        u.setMessage(message);
        u.setCallbackQuery(callbackQuery);

        return u;
    }

    public static CallbackQuery createCallbackQuery(String callbackQuery, Message message) {
        var c = new CallbackQuery();
        c.setData(callbackQuery);
        c.setMessage(message);
        c.setFrom(message.getFrom());
        return c;
    }

    public static User createUser(String firstName, String lastName) {
        var c = new User();
        c.setFirstName(firstName);
        c.setUserName(firstName);
        c.setLastName(lastName);
        return c;
    }

}
