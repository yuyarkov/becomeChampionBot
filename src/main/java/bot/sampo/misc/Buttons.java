package bot.sampo.misc;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class Buttons {

    public static InlineKeyboardMarkup buttonsBeforeSignUP() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonsFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> buttonsSecondRow = new ArrayList<>();

        InlineKeyboardButton buttonShowList = new InlineKeyboardButton();
        buttonShowList.setCallbackData("ShowList");
        buttonShowList.setText("Посмотреть список");
        buttonsFirstRow.add(buttonShowList);
        buttons.add(buttonsFirstRow);

        InlineKeyboardButton buttonPair = new InlineKeyboardButton();
        buttonPair.setCallbackData("SignUpPair");
        buttonPair.setText("Записаться парой");
        InlineKeyboardButton buttonAlone= new InlineKeyboardButton();
        buttonAlone.setCallbackData("SignUpAlone");
        buttonAlone.setText("Записаться без пары");
        buttonsSecondRow.add(buttonPair);
        buttonsSecondRow.add(buttonAlone);
        buttons.add(buttonsSecondRow);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        return markupKeyboard;
    }

    public static InlineKeyboardMarkup buttonsAfterSignUPAlone() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonsFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> buttonsSecondRow = new ArrayList<>();

        InlineKeyboardButton buttonShowList = new InlineKeyboardButton();
        buttonShowList.setCallbackData("ShowList");
        buttonShowList.setText("Посмотреть список");
        buttonsFirstRow.add(buttonShowList);
        buttons.add(buttonsFirstRow);

        InlineKeyboardButton buttonPair = new InlineKeyboardButton();
        buttonPair.setCallbackData("SignUpPair");
        buttonPair.setText("Записаться парой");
        InlineKeyboardButton buttonCancelAlone = new InlineKeyboardButton();
        buttonCancelAlone.setCallbackData("CancelAlone");
        buttonCancelAlone.setText("\u274C" + " Я не смогу прийти");
        buttonsSecondRow.add(buttonPair);
        buttonsSecondRow.add(buttonCancelAlone);
        buttons.add(buttonsSecondRow);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        return markupKeyboard;
    }

    public static InlineKeyboardMarkup buttonsAfterSignUPPair() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonsFirstRow = new ArrayList<>();
        List<InlineKeyboardButton> buttonsSecondRow = new ArrayList<>();

        InlineKeyboardButton buttonShowList = new InlineKeyboardButton();
        buttonShowList.setCallbackData("ShowList");
        buttonShowList.setText("Посмотреть список");
        buttonsFirstRow.add(buttonShowList);
        buttons.add(buttonsFirstRow);

        InlineKeyboardButton buttonCancelPair = new InlineKeyboardButton();
        buttonCancelPair.setCallbackData("CancelPair");
        buttonCancelPair.setText("Не сможем прийти оба");
        InlineKeyboardButton buttonCancelAlone = new InlineKeyboardButton();
        buttonCancelAlone.setCallbackData("CancelAlone");
        buttonCancelAlone.setText("\u274C" + " Я не смогу прийти");
        buttonsSecondRow.add(buttonCancelPair);
        buttonsSecondRow.add(buttonCancelAlone);
        buttons.add(buttonsSecondRow);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        return markupKeyboard;
    }

    public static InlineKeyboardMarkup buttonsAfterStart() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonsFirstRow = new ArrayList<>();
        InlineKeyboardButton buttonLeader = new InlineKeyboardButton();
        buttonLeader.setCallbackData("Leader");
        buttonLeader.setText("Я партнёр");
        InlineKeyboardButton buttonFollower = new InlineKeyboardButton();
        buttonFollower.setCallbackData("Follower");
        buttonFollower.setText("Я партнёрша");
        buttonsFirstRow.add(buttonLeader);
        buttonsFirstRow.add(buttonFollower);
        buttons.add(buttonsFirstRow);
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        return markupKeyboard;
    }

    public static InlineKeyboardMarkup buttonsAskUserLastname() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonsFirstRow = new ArrayList<>();

        InlineKeyboardButton buttonLeader = new InlineKeyboardButton();
        buttonLeader.setCallbackData("readmylastname");
        buttonLeader.setText("Запиши мою правильную фамилию");
        InlineKeyboardButton buttonFollower = new InlineKeyboardButton();
        buttonFollower.setCallbackData("lastnameok");
        buttonFollower.setText("Оставь как есть");
        buttonsFirstRow.add(buttonLeader);
        buttonsFirstRow.add(buttonFollower);
        buttons.add(buttonsFirstRow);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        return markupKeyboard;
    }

}
