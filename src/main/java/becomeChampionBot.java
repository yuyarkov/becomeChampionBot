
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
    public static final long CHAT_ID_DETOCHKIN = 238349375L;

    public static long chatWaiting = 0;
    public static boolean waitingForLastName;

    public static String dialogDancerSex = null;
    public static String updatedListSampo;
    public static ListSampo nextSampoList;
    public static Dancer dan1 = new Dancer("Юрий", "Деточкин", Dancer.LEADER, 123L, null);
    public static Dancer dan2 = new Dancer("Евгений", "Полянский", Dancer.LEADER, 123L, null);
    public static Dancer dan3 = new Dancer("Юлия", "Деточкина", Dancer.FOLLOWER, 123L, null);
    public static Dancer dan4 = new Dancer("Юлия", "Ахметгареева", Dancer.FOLLOWER, 123L, null);
    public static Dancer dan5 = new Dancer("Маша", "Загоруйченко", Dancer.FOLLOWER, 123L, null);
    public static Dancer dan6 = new Dancer("Иннокентий", "Иванов", Dancer.LEADER, 123L, null);


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
        long chatID = callbackQuery.getMessage().getChatId();
        switch (callbackQuery.getData()) {
            case "Leader":
                askWhatLastName(callbackQuery);
                dialogDancerSex = Dancer.LEADER;
                break;
            case "Follower":
                askWhatLastName(callbackQuery);
                dialogDancerSex = Dancer.FOLLOWER;
                break;
            case "lastnameok":
                createDancer(callbackQuery);
                sendMessageAfterInit(chatID);
                break;
            case "readmylastname":
                replyToTelegram(chatID, "Понял. Напиши свою фамилию без кавычек:");
                waitingForLastName = true;
                chatWaiting = chatID;
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


    public void sendListSampo(long chatID) {
        SendMessage messageWithList = new SendMessage(); // Create a SendMessage object with mandatory fields
        messageWithList.setChatId(String.valueOf(chatID));
        messageWithList.enableMarkdown(true);
        messageWithList.setText("*Текущий список на сампо:*\n(пока тестовый режим, список не сохраняется)\n" + nextSampoList.printToString());
        try {
            execute(messageWithList); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void reactToRequest(Message message) {
        long chatID = message.getChatId();
        if (message.hasText() && chatWaiting != 0 && chatID == chatWaiting&&!waitingForLastName) {
            parseDancerFromRequest(message);
        }

        if (message.hasText() && waitingForLastName && chatID == chatWaiting){
            readDancerLastName(message);
        }

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
                askPartnerName(message);
                break;
            case "Посмотреть список":
                sendListSampo(message.getChatId());
                break;
        }

    }

    public boolean isExistingDancer(long chatID) {
        return false;
    }

    public void askPartnerName(Message message) {
        long chatID = message.getChatId();
        Dancer firstDancer = DancerBase.getDancerByChatID(chatID);
        replyToTelegram(chatID, "Давай запишу. Скажи, с кем, напиши пожалуйста фамилию:");
        chatWaiting = chatID;
    }

    public void signUpWithPartner(long chatID, Dancer dancer1, Dancer dancer2) {
        if (dancer1.getSex().equals(Dancer.LEADER)) {
            nextSampoList.addPairToList(dancer1, dancer2);
        } else {
            nextSampoList.addPairToList(dancer2, dancer1);
        }
        sendMessageAfterSignUp(chatID);
    }


    public void parseDancerFromRequest(Message message) {
        String messageLastName = message.getText();
        long chatID = message.getChatId();
        Dancer firstDancer = DancerBase.getDancerByChatID(chatID);
        Dancer foundDancer = DancerBase.findDancerByLastName(messageLastName);
        if (!foundDancer.equals(DancerBase.emptyDancer)) {
            signUpWithPartner(chatID, firstDancer, foundDancer);
            chatWaiting = 0;
        } else {
            replyToTelegram(chatID, messageLastName + " — не нашёл такой фамилии в базе танцоров. Напиши фамилию без кавычек");
        }

    }


    public void signUpAlone(Message message) {
        long chatID = message.getChatId();
        Dancer dancertoAdd = DancerBase.getDancerByChatID(chatID);
        boolean success = nextSampoList.addDancerToList(dancertoAdd);
        if (success) {
            updatedListSampo = nextSampoList.printToString();
            replyToTelegram(message.getChatId(), "Записал тебя!");
            sendListSampo(chatID);
        } else {
            replyToTelegram(message.getChatId(), "Ты уже в списке");
        }
        sendMessageAfterSignUp(chatID);
    }

    public void cancelAlone(Message message) {
        long chatID = message.getChatId();
        Dancer dancertoRemove = DancerBase.getDancerByChatID(chatID);

        nextSampoList.removeDancerFromList(dancertoRemove);
        updatedListSampo = nextSampoList.printToString();
        replyToTelegram(chatID, "Удалил из списка!");
        sendListSampo(chatID);
        sendMessageAfterInit(chatID);
    }


    public void createDancer(CallbackQuery callbackQuery) {
        String firstName = callbackQuery.getFrom().getFirstName();
        String lastName = callbackQuery.getFrom().getLastName();
        long chatID = callbackQuery.getMessage().getChatId();
        String telegramName = callbackQuery.getFrom().getUserName();
        Dancer newDancer = new Dancer(firstName, lastName, dialogDancerSex, chatID, telegramName);
        DancerBase.dancerBase.add(newDancer);
        try {
            util.Converter.saveDancerBaseToFile(DancerBase.dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dialogDancerSex=null;
    }

    public void sendMessageAfterInit(long chatID) {
        SendMessage messageAfterInit = new SendMessage();
        messageAfterInit.setChatId(String.valueOf(chatID));
        messageAfterInit.setText("Готово. Занёс тебя в базу. Записывайся на следующее сампо");
        setButtonsBeforeSignUP(messageAfterInit);
        try {
            execute(messageAfterInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageAfterSignUp(long chatID) {
        SendMessage messageAfterAfterSignUp = new SendMessage();
        messageAfterAfterSignUp.setChatId(String.valueOf(chatID));
        messageAfterAfterSignUp.setText("Записал вас обоих. До встречи на сампо!");

        setButtonsAfterSignUP(messageAfterAfterSignUp);
        try {
            execute(messageAfterAfterSignUp);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        sendListSampo(chatID);
    }

    public void askWhatSex(Message message) {
        String firstName = message.getFrom().getFirstName();
        SendMessage messageInit = new SendMessage();
        messageInit.setChatId(message.getChatId().toString());
        messageInit.setText("Привет, " + firstName + "! Бот пока ещё не умеет определять пол. Подскажи, кто ты?");
        messageInit.setReplyMarkup(setInitButtonsAfterStart());
        try {
            execute(messageInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void askWhatLastName(CallbackQuery callbackQuery) {
        String lastname = callbackQuery.getFrom().getLastName();
        SendMessage messageInit = new SendMessage();
        messageInit.setChatId(callbackQuery.getMessage().getChatId().toString());
        if (lastname == null) {
            messageInit.setText("У тебя в телеграме не заполнена фамилия. Давай запишу правильную фамилию, с которой ты будешь записываться на сампо.");
        } else {
            messageInit.setText("Твоя фамилия в телеграме: " + lastname + "\n Оставить эту фамилию?");
        }
        messageInit.setReplyMarkup(setInitButtonsAskUserLastname());
        try {
            execute(messageInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void readDancerLastName(Message message) {
        long chatID = message.getChatId();
        String lastName = message.getText();
                DancerBase.dancerBase.add(new Dancer(message.getFrom().getFirstName(), lastName, dialogDancerSex, chatID, message.getFrom().getUserName()));
        try {
            util.Converter.saveDancerBaseToFile(DancerBase.dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
        waitingForLastName=false;
        chatWaiting=0;
        dialogDancerSex=null;
        sendMessageAfterInit(chatID);

    }


    public void replyToTelegram(long chatID, String text) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(String.valueOf(chatID));
        message.setText(text);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void replyWithButtonDisappear(long chatID, long messageID, String text) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatID));
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

    public InlineKeyboardMarkup setInitButtonsAskUserLastname() {
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


    public static void createFileDancerBase() {
        try {
            Files.createFile(Path.of(DancerBase.sourceDancerBase));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        try {
            DancerBase.dancerBase = util.Converter.readDancerBaseFromFile();
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
