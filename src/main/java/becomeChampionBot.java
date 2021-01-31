
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.nio.file.Files;

import pojo.Dancer;

import static java.lang.Math.toIntExact;

public class becomeChampionBot extends TelegramLongPollingBot {
    private String botToken = System.getenv("TOKEN_BECOME_CHAMPION_BOT");
    public static final String CHAT_ID_DETOCHKIN = "238349375";

    public static HashSet<Dancer> dancerBase = new HashSet<>();
    public static String sourceDancerBase = "dancerBase.xml";

    public static String updatedListSampo;
    public static ListSampo nextSampoList;
    public static Dancer emptyDancer = new Dancer("", "в поиске - пустой", Dancer.LEADER, CHAT_ID_DETOCHKIN, null);
    public static Dancer dan1 = new Dancer("Юрий", "Деточкин", Dancer.LEADER, "123", null);
    public static Dancer dan2 = new Dancer("Евгений", "Скориков", Dancer.LEADER, "123", null);
    public static Dancer dan3 = new Dancer("Юлия", "Яркова", Dancer.FOLLOWER, "123", null);
    public static Dancer dan4 = new Dancer("Юлия", "Прокопьева", Dancer.FOLLOWER, "123", null);
    public static Dancer dan5 = new Dancer("Маша", "Лазарева", Dancer.FOLLOWER, "123", null);
    public static Dancer dan6 = new Dancer("Иннокентий", "Севрюгин", Dancer.LEADER, "123", null);


    @Override
    public void onUpdateReceived(Update update) {
        Message receivedText = update.getMessage();
        if (update.hasMessage() && receivedText.hasText()) {
            reactToRequest(receivedText);
        } else if (update.hasCallbackQuery()) {
            reactToCallBack(update.getCallbackQuery(), update.getMessage());
        }

    }

    public void reactToCallBack(CallbackQuery callbackQuery, Message message) {
        String chatID = callbackQuery.getMessage().getChatId().toString();
        switch (callbackQuery.getData()) {
            case "Leader":
                createDancer(callbackQuery, Dancer.LEADER);
                sendMessageAfterInit(chatID);
                break;
            case "Follower":
                createDancer(callbackQuery, Dancer.FOLLOWER);
                sendMessageAfterInit(chatID);
                break;
        }
    }

/*  public synchronized void answerCallbackQuery(String callbackId, String message) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        answer.setText(message);
        answer.setShowAlert(true);
        try {
            answerCallbackQuery(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }*/


    public void sendListSampo(Message message) {
        SendMessage messageWithList = new SendMessage(); // Create a SendMessage object with mandatory fields
        messageWithList.setChatId(message.getChatId().toString());
        messageWithList.setText("Текущий список на сампо:\n" + updatedListSampo);
        try {
            execute(messageWithList); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void reactToRequest(Message message) {
        switch (message.getText()) {
            case "Записаться без пары":
                signUpAlone(message);
                break;
            case "/start":
                askWhatSex(message);
                break;
            case "Отменить мою запись":
                cancelAlone(message);
                break;
            case "Записаться парой":
                signUpWithPartner(message);
                break;
            case "Посмотреть список":
                sendListSampo(message);
        }

    }

    public boolean isExistingDancer(String chatID) {
        return false;
    }

    public void signUpWithPartner(Message message) {
        String chatID=message.getChatId().toString();
        replyToTelegram(chatID, "Записал! (неизвестно с кем)");
        sendListSampo(message);


        sendMessageAfterSignUp(chatID);
    }

    public void signUpAlone(Message message) {
        String chatID = message.getChatId().toString();
        Dancer dancertoAdd = emptyDancer;
        for (var dancer : dancerBase) {
            if (dancer.getChatID().equals(chatID)) {
                dancertoAdd = dancer;
            }
        }
        boolean success = nextSampoList.addDancerToList(dancertoAdd);
        if (success) {
            updatedListSampo = nextSampoList.printToString();
            replyToTelegram(message.getChatId().toString(), "Записали!");
            sendListSampo(message);
        } else {
            replyToTelegram(message.getChatId().toString(), "Уже был записан!");
        }

        sendMessageAfterSignUp(chatID);
    }

    public void cancelAlone(Message message) {
        String chatID = message.getChatId().toString();
        Dancer dancertoRemove = emptyDancer;
        for (var dancer : dancerBase) {
            if (dancer.getChatID().equals(chatID)) {
                dancertoRemove = dancer;
            }
        }
        nextSampoList.removeDancerFromList(dancertoRemove);
        updatedListSampo = nextSampoList.printToString();
        replyToTelegram(chatID, "Удалил из списка!");
        sendListSampo(message);
        sendMessageAfterInit(chatID);
    }


    public void createDancer(CallbackQuery callbackQuery, String sex) {
        String firstName = callbackQuery.getFrom().getFirstName();
        String lastName = callbackQuery.getFrom().getLastName();
        String chatID = callbackQuery.getMessage().getChatId().toString();
        long messageID = callbackQuery.getMessage().getMessageId();
        String telegramName = callbackQuery.getFrom().getUserName();
        dancerBase.add(new Dancer(firstName, lastName, Dancer.LEADER, chatID, telegramName));

        try {
            util.Converter.saveDancerBaseToFile(dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sex == Dancer.LEADER) {
            replyWithButtonDisappear(chatID, messageID, "Отлично! Партнеры нам нужны. Занёс тебя в базу под фамилией " + lastName);
        } else {
            replyWithButtonDisappear(chatID, messageID, "Отлично! Партнёрши нам нужны. Занёс тебя в базу под фамилией " + lastName);
        }
    }

    public void sendMessageAfterInit(String chatID) {
        SendMessage messageAfterInit = new SendMessage();
        messageAfterInit.setChatId(chatID);
        messageAfterInit.setText("Записывайся на следующее сампо");
        setButtonsBeforeSignUP(messageAfterInit);
        try {
            execute(messageAfterInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageAfterSignUp(String chatID) {
        SendMessage messageAfterAfterSignUp = new SendMessage();
        messageAfterAfterSignUp.setChatId(chatID);
        messageAfterAfterSignUp.setText("До встречи на сампо!");
        setButtonsAfterSignUP(messageAfterAfterSignUp);
        try {
            execute(messageAfterAfterSignUp);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void askWhatSex(Message message) {
        SendMessage messageInit = new SendMessage();
        messageInit.setChatId(message.getChatId().toString());
        messageInit.setText("Привет! Бот пока ещё не умеет определять пол. Подскажи, кто ты?");
        messageInit.setReplyMarkup(setInitButtonsAfterStart());
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

    public void replyWithButtonDisappear(String chatID, long messageID, String text) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatID);
        message.setMessageId(toIntExact(messageID));
        message.setText(text);
        try {
            execute(message);
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

    public void setButtonsBeforeSignUP(SendMessage sendMessage) {
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
        keyboardSecondRow.add(new KeyboardButton("Посмотреть список"));
        keyboardRowList.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public void setButtonsAfterSignUP(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Отменить запись в паре"));
        keyboardFirstRow.add(new KeyboardButton("Отменить мою запись"));
        keyboardRowList.add(keyboardFirstRow);
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton("Посмотреть список"));
        keyboardRowList.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }


    public InlineKeyboardMarkup setInitButtonsAfterStart() {
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


    public static void createFileDancerBase() {
        try {
            Files.createFile(Path.of(sourceDancerBase));
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public static void main(String[] args) {


        HashSet<Dancer> readDancerBase = new HashSet<>();
        try {
            readDancerBase = util.Converter.readDancerBaseFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            //    currentBot.sendListSampo();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }


}
