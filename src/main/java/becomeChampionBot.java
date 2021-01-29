import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class becomeChampionBot extends TelegramLongPollingBot {
    private String botToken = System.getenv("TOKEN_BECOME_CHAMPION_BOT");
    public static final String CHAT_ID_DETOCHKIN = "238349375";

    public static ArrayList<Dancer> dancerBase = new ArrayList<>();

    public static String updatedListSampo;
    public static ListSampo nextSampoList;
    public static Dancer dan1 = new Dancer("Юрий", "Деточкин", Dancer.LEADER, CHAT_ID_DETOCHKIN);
    public static Dancer dan2 = new Dancer("Евгений", "Скориков", Dancer.LEADER, CHAT_ID_DETOCHKIN);
    public static Dancer dan3 = new Dancer("Юлия", "Яркова", Dancer.FOLLOWER, CHAT_ID_DETOCHKIN);
    public static Dancer dan4 = new Dancer("Юлия", "Прокопьева", Dancer.FOLLOWER, CHAT_ID_DETOCHKIN);
    public static Dancer dan5 = new Dancer("Маша", "Без партнера", Dancer.FOLLOWER, CHAT_ID_DETOCHKIN);
    public static Dancer dan6 = new Dancer("Иннокентий", "Севрюгин", Dancer.LEADER, CHAT_ID_DETOCHKIN);


    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        Message receivedText = update.getMessage();
        if (update.hasMessage() && receivedText.hasText()) {
/*            SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
            message.setChatId(update.getMessage().getChatId().toString());
            */
            reactToRequest(receivedText);

/*            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }*/
        }
    }

    public void sendListSampo() {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(CHAT_ID_DETOCHKIN);
        message.setText("Текущий список на сампо:\n" + updatedListSampo);
        try {
            setPermanentButtons(message);
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void reactToRequest(Message message) {
        switch (message.getText()) {
            case "Записаться без пары":
                boolean success = nextSampoList.addDancerToList(dan6);
                if (success) {
                    updatedListSampo = nextSampoList.printToString();
                    replyToTelegram(message.getChatId().toString(), "Записали!");
                    sendListSampo();
                } else {
                    replyToTelegram(message.getChatId().toString(), "Уже был записан!");
                }
                break;
            case "Я партнер":
                String sex = Dancer.LEADER;
                replyToTelegram(message.getChatId().toString(), "Отлично! Партнеры нам нужны");
                SendMessage messageAfterInit = new SendMessage();
                messageAfterInit.setChatId(message.getChatId().toString());
                messageAfterInit.setText("Записал. Постараюсь не забыть");
                setPermanentButtons(messageAfterInit);
                try {
                    execute(messageAfterInit);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "/start":
                SendMessage messageInit = new SendMessage();
                messageInit.setChatId(message.getChatId().toString());
                messageInit.setText("Привет! Бот пока ещё не умеет определять пол. Подскажи, кто ты?");
                setInitButtons(messageInit);
                try {
                    execute(messageInit);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;

        }

    }

    public void replyToTelegram(String chatID, String text) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(chatID);
        message.setText(text);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
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

    public void setPermanentButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Записаться парой"));
        keyboardFirstRow.add(new KeyboardButton("Записаться без пары"));
        keyboardRowList.add(keyboardFirstRow);
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton("Отменить запись парой"));
        keyboardSecondRow.add(new KeyboardButton("Отменить мою запись"));
        keyboardRowList.add(keyboardSecondRow);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public void setInitButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Я партнер"));
        keyboardFirstRow.add(new KeyboardButton("Я партнерша"));
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }


    public static void main(String[] args) {


        nextSampoList = new ListSampo(LocalDate.of(2021, Month.FEBRUARY, 1));


        nextSampoList.addPairToList(dan1, dan3);
        nextSampoList.addPairToList(dan2, dan4);
        nextSampoList.addDancerToList(dan5);

        nextSampoList.removeDancerFromList(dan4);

        nextSampoList.printListSampo();


        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            var currentBot = new becomeChampionBot();
            botsApi.registerBot(currentBot);
            updatedListSampo = nextSampoList.printToString();
            currentBot.sendListSampo();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }


}
