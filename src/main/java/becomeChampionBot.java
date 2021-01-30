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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class becomeChampionBot extends TelegramLongPollingBot {
    private String botToken = System.getenv("TOKEN_BECOME_CHAMPION_BOT");
    public static final String CHAT_ID_DETOCHKIN = "238349375";

    public static HashSet<Dancer> dancerBase = new HashSet<>();

    public static String updatedListSampo;
    public static ListSampo nextSampoList;
    public static Dancer emptyDancer=new Dancer("","в поиске - пустой", Dancer.LEADER,CHAT_ID_DETOCHKIN);
    public static Dancer dan1 = new Dancer("Юрий", "Деточкин", Dancer.LEADER, "123");
    public static Dancer dan2 = new Dancer("Евгений", "Скориков", Dancer.LEADER, "123");
    public static Dancer dan3 = new Dancer("Юлия", "Яркова", Dancer.FOLLOWER, "123");
    public static Dancer dan4 = new Dancer("Юлия", "Прокопьева", Dancer.FOLLOWER, "123");
    public static Dancer dan5 = new Dancer("Маша", "Лазарева", Dancer.FOLLOWER, "123");
    public static Dancer dan6 = new Dancer("Иннокентий", "Севрюгин", Dancer.LEADER, "123");


    @Override
    public void onUpdateReceived(Update update) {
        Message receivedText = update.getMessage();
        if (update.hasMessage() && receivedText.hasText()) {
            reactToRequest(receivedText);
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
                signUpAlone(message);
                break;
            case "Я партнер":
                createLeader(message);
                break;
            case "/start":
                askWhatSex(message);
                break;
            case "Отменить мою запись":
                cancelAlone(message);
                break;

        }

    }

    public void signUpAlone(Message message) {
        String chatID = message.getChatId().toString();
        Dancer dancertoAdd=emptyDancer;
        for (var dancer : dancerBase) {
            if (dancer.getIdDancerFromChatID().equals(chatID)) {
                dancertoAdd = dancer;
            }
        }
        boolean success = nextSampoList.addDancerToList(dancertoAdd);
        if (success) {
            updatedListSampo = nextSampoList.printToString();
            replyToTelegram(message.getChatId().toString(), "Записали!");
            sendListSampo();
        } else {
            replyToTelegram(message.getChatId().toString(), "Уже был записан!");
        }
    }

    public void cancelAlone(Message message) {
        String chatID = message.getChatId().toString();
        Dancer dancertoRemove=emptyDancer;
        for (var dancer : dancerBase) {
            if (dancer.getIdDancerFromChatID().equals(chatID)) {
                dancertoRemove = dancer;
            }
        }
        nextSampoList.removeDancerFromList(dancertoRemove);
        updatedListSampo = nextSampoList.printToString();
        replyToTelegram(chatID, "Удалил из списка!");
        sendListSampo();

    }


    public void createLeader(Message message) {
        String sex = Dancer.LEADER;
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName();
        String chatID = message.getChatId().toString();
        dancerBase.add(new Dancer(firstName, lastName, Dancer.LEADER, chatID));
        replyToTelegram(chatID, "Отлично! Партнеры нам нужны. Занёс тебя в базу под фамилией " + lastName);
        SendMessage messageAfterInit = new SendMessage();
        messageAfterInit.setChatId(chatID);
        messageAfterInit.setText("Записывайся на следующее сампо");
        setPermanentButtons(messageAfterInit);
        try {
            execute(messageAfterInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void askWhatSex(Message message) {
        SendMessage messageInit = new SendMessage();
        messageInit.setChatId(message.getChatId().toString());
        messageInit.setText("Привет! Бот пока ещё не умеет определять пол. Подскажи, кто ты?");
        setInitButtons(messageInit);
        try {
            execute(messageInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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

        dancerBase.add(dan1);
        dancerBase.add(dan2);
        dancerBase.add(dan3);
        dancerBase.add(dan4);
        dancerBase.add(dan5);
        dancerBase.add(dan6);

        nextSampoList = new ListSampo(LocalDate.of(2021, Month.FEBRUARY, 1));


        nextSampoList.addPairToList(dan1, dan3);
        nextSampoList.addPairToList(dan2, dan4);
        nextSampoList.addDancerToList(dan5);

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
