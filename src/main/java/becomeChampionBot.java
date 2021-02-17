
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;


import util.Converter;
import pojo.Dancer;

import static java.lang.Math.toIntExact;

public class becomeChampionBot extends TelegramLongPollingBot {
    private String botToken = System.getenv("TOKEN_BECOME_CHAMPION_BOT");
    public static final long CHAT_ID_DETOCHKIN = 238349375L;//id моего чата, где я получаю все уведомления от программы

    //   public static long chatWaiting = 0; //статус чата, в котором бот запросил инфу от юзера и ждёт ответа
    public static Map<Long, Boolean> waitingForPartnerName; //статус, где записывается парой и указывае фамилию партнера/партнерши*/
    public static Map<Long, Boolean> waitingForLastName; //статус, где юзеру нужно ввести свою фамилию
    //ключ - chatID, 0 - имя тг, 1 - фамилия тг, 2 - пол, 3 - ник в телеграме, 4 - жду фамилию самого юзера, 5 - жду фамилию партнера/партнерши

    public static Map<Long, String> dialogDancerSex; //юзер при начале общения с ботом указывает свой пол
    public static String updatedListSampo;
    public static ListSampo nextSampoList;
    public static WaitingListSampo nextSampoWaitingList;


    @Override
    public void onUpdateReceived(Update update) {
        Message receivedText = update.getMessage();
        if (update.hasMessage() && receivedText.hasText()) {
            replyToTelegram(CHAT_ID_DETOCHKIN, "log: " + receivedText.getText() + " chatID: " + receivedText.getChatId() +
                    " firstname: " + receivedText.getFrom().getFirstName());
            reactToRequest(receivedText);
        } else if (update.hasCallbackQuery()) {
            reactToCallBack(update.getCallbackQuery(), update.getMessage());
        }

    }

    public void reactToCallBack(CallbackQuery callbackQuery, Message message) {
        long chatID = callbackQuery.getMessage().getChatId();
        String currentDancerSex = DancerBase.getDancerByChatID(chatID).getSex();
        replyToTelegram(CHAT_ID_DETOCHKIN, "log callback: " + callbackQuery.getData());
        switch (callbackQuery.getData()) {
            case "Leader":
                askWhatLastName(callbackQuery);
                dialogDancerSex.put(chatID, Dancer.LEADER);
                break;
            case "Follower":
                askWhatLastName(callbackQuery);
                dialogDancerSex.put(chatID, Dancer.FOLLOWER);
                break;
            case "lastnameok":
                DancerBase.createDancer(callbackQuery);
                sendMessageAfterInit(chatID);
                waitingForLastName.put(chatID, false);
                break;
            case "readmylastname":
                replyToTelegram(chatID, "Понял. Напиши свою фамилию без кавычек:");
                waitingForLastName.put(chatID, true);
                break;
            case "ShowList":
                sendListSampo(chatID);
                break;
            case "SignUpPair":
                askPartnerName(chatID);
                break;
            case "SignUpAlone":
                if (currentDancerSex.equals(Dancer.FOLLOWER)) {
                    signUpLonelyGirl(message);
                } else {
                    signUpAlone(chatID);
                }
                break;
            case "CancelAlone":
                cancelAlone(chatID);
                break;
            case "CancelPair":
                cancelPair(chatID);
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


    public void reactToRequest(Message message) {
        long chatID = message.getChatId();
        String currentDancerSex = DancerBase.getDancerByChatID(chatID).getSex();
        if (waitingForLastName.get(chatID) == null) {
            waitingForLastName.put(chatID, false);
        }
        if (waitingForPartnerName.get(chatID) == null) {
            waitingForPartnerName.put(chatID, false);
        }

        if (message.getText().equals("/start")) {
            askWhatSex(message);
        }

        if (message.hasText() && !waitingForPartnerName.isEmpty() && waitingForPartnerName.get(chatID)) {
            parseDancerFromRequest(message);
        }

        if (message.hasText() && !waitingForLastName.isEmpty() && waitingForLastName.get(chatID)) {
            readDancerLastName(message);
        }

    }

    public void askPartnerName(long chatID) {

        Dancer firstDancer = DancerBase.getDancerByChatID(chatID);
        replyToTelegram(chatID, "Давай запишу. Скажи, с кем, напиши пожалуйста фамилию:");
        waitingForPartnerName.put(chatID, true);
    }

    public void signUpWithPartner(long chatID, Dancer dancer1, Dancer dancer2) {
        if (nextSampoList.isAlreadySigned(dancer1)) {
            replyToTelegram(chatID, "Ошибка: " + dancer1.getFirstName() + " " + dancer1.getLastName() + " уже есть в списке.");
            return;
        }
        if (nextSampoList.isAlreadySigned(dancer2)) {
            replyToTelegram(chatID, "Ошибка: " + dancer2.getFirstName() + " " + dancer2.getLastName() + " уже есть в списке.");
            return;
        }

        if (dancer1.getSex().equals(Dancer.LEADER)) {
            nextSampoList.addPairToList(dancer1, dancer2);
            replyToTelegram(dancer2.getChatID(), "Ты записана на следующее сампо в паре с партнёром: "
                    + dancer1.getFirstName() + " " + dancer1.getLastName());
        } else {
            nextSampoList.addPairToList(dancer2, dancer1);
            replyToTelegram(dancer2.getChatID(), "Ты записан на следующее сампо в паре с партнёршей: "
                    + dancer1.getFirstName() + " " + dancer1.getLastName());
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
            waitingForPartnerName.put(chatID, false);
        } else {
            replyToTelegram(chatID, messageLastName + " — не нашёл такой фамилии в базе танцоров. Либо опечатка (тогда нажми ещё раз" +
                    " «Записаться в паре» и введи фамилию." +
                    " Или напиши Деточкину, он добавит: @yuyarkov.\n" +
                    "Вариант — пусть самостоятельно напишет боту.");
        }

    }


    public void signUpLonelyGirl(Message message) {
        long chatID = message.getChatId();
        Dancer dancertoAdd = DancerBase.getDancerByChatID(chatID);
        if (nextSampoList.isAlreadySigned(dancertoAdd)) {
            replyToTelegram(chatID, "Ты уже есть в основном списке))");
            return;
        }
        boolean success = nextSampoWaitingList.addToWaitingList(dancertoAdd);
        if (!success) {
            replyToTelegram(message.getChatId(), "Ты уже в листе ожидания");
        }
        sendMessageAfterAddingToWaitingList(chatID);
    }

    public void signUpAlone(long chatID) {

        Dancer dancertoAdd = DancerBase.getDancerByChatID(chatID);
        if (nextSampoList.isAlreadySigned(dancertoAdd)) {
            replyToTelegram(chatID, "Ошибка: " + dancertoAdd.getFirstName() + " " + dancertoAdd.getLastName() +
                    " уже есть в списке");
            return;
        }
        if (dancertoAdd.getSex().equals(Dancer.LEADER) && nextSampoList.hasEmptySlotForLeader()) {
            Dancer dancer2 = nextSampoList.getFollowerWithEmptySlot();
            nextSampoList.addLeaderToEmptySlot(dancertoAdd);
            replyToTelegram(dancer2.getChatID(), "Записал к тебе в пару танцора: " +
                    dancertoAdd.getFirstName() + " " + dancertoAdd.getLastName());
            replyToTelegram(chatID, "Записал тебя вместе с партнёршей: " +
                    dancer2.getFirstName() + " " + dancer2.getLastName());
            updatedListSampo = nextSampoList.printToString();
            return;
        }

        if (dancertoAdd.getSex().equals(Dancer.LEADER) && nextSampoWaitingList.hasDancersInWaitingList()) {
            Dancer dancer2 = nextSampoWaitingList.removeFirstFromWaitingList();
            nextSampoList.addPairToList(dancertoAdd, dancer2);
            replyToTelegram(dancer2.getChatID(), "Перенёс тебя из листа ожидания в основной список," +
                    "в пару к танцору: " + dancertoAdd.getFirstName() + " " + dancertoAdd.getLastName());
            replyToTelegram(chatID, "Записал тебя вместе с партнёршей из листа ожидания: " +
                    dancer2.getFirstName() + " " + dancer2.getLastName());
            updatedListSampo = nextSampoList.printToString();
            return;
        }

        boolean success = nextSampoList.addDancerToList(dancertoAdd);
        if (success) {
            updatedListSampo = nextSampoList.printToString();
        } else {
            replyToTelegram(chatID, "Ты уже в списке");
        }
        sendMessageAfterSignUp(chatID);
    }

    public void cancelAlone(long chatID) {

        Dancer dancertoRemove = DancerBase.getDancerByChatID(chatID);

        nextSampoList.removeDancerFromList(dancertoRemove);
        if (dancertoRemove.getSex().equals(Dancer.FOLLOWER) && nextSampoWaitingList.hasDancersInWaitingList()) {
            Dancer dancerToAdd = nextSampoWaitingList.removeFirstFromWaitingList();
            nextSampoList.addFollowerToEmptySlot(dancerToAdd);
            replyToTelegram(dancerToAdd.getChatID(), "Перенёс тебя из списка ожидания в основной список, " +
                    "в пару к танцору: " + nextSampoList.findPairByDancer(dancerToAdd));
        }
        updatedListSampo = nextSampoList.printToString();
        sendMessageAfterCancelAlone(chatID);
    }

    public void cancelPair(long chatID) {

        Dancer firstDancerToRemove = DancerBase.getDancerByChatID(chatID);

        Dancer secondDancerToRemove = nextSampoList.findPairByDancer(firstDancerToRemove);
        System.out.println("буду удалять пару: " + firstDancerToRemove.getLastName() + " - " + secondDancerToRemove.getLastName());

        if (secondDancerToRemove == null) {
            replyToTelegram(chatID, "Не вижу в списке вашу пару. Чтобы отменить запись, надо сначала записаться))");
        }

        nextSampoList.removePairFromList(firstDancerToRemove, secondDancerToRemove);

        updatedListSampo = nextSampoList.printToString();
        replyToTelegram(chatID, "Удалил из списка вашу пару: " + firstDancerToRemove.getLastName() + " - " + secondDancerToRemove.getLastName());
        sendMessageAfterCancelPair(chatID);
        replyToTelegram(CHAT_ID_DETOCHKIN, firstDancerToRemove.getLastName() + " попросил удалить их пару с " +
                secondDancerToRemove.getLastName() + " Удалил");
        if (firstDancerToRemove.getSex().equals(Dancer.LEADER)) {
            replyToTelegram(secondDancerToRemove.getChatID(), firstDancerToRemove.getFirstName() +
                    " " + firstDancerToRemove.getLastName() + " попросил отменить вашу запись на сампо. Удалил вас из списка!");
        } else {
            replyToTelegram(secondDancerToRemove.getChatID(), firstDancerToRemove.getFirstName() +
                    " " + firstDancerToRemove.getLastName() + " попросила отменить вашу запись на сампо. Удалил вас из списка!");
        }
    }

    public void sendMessageAfterSignUp(long chatID) {
        SendMessage messageAfterAfterSignUp = new SendMessage();
        messageAfterAfterSignUp.setChatId(String.valueOf(chatID));
        messageAfterAfterSignUp.setText("До встречи на сампо! Захвати с собой водички");

        messageAfterAfterSignUp.setReplyMarkup(Buttons.buttonsBeforeSignUP());
        try {
            execute(messageAfterAfterSignUp);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        sendListSampo(chatID);
    }

    public void sendMessageAfterAddingToWaitingList(long chatID) {
        SendMessage messageAfterAfterSignUp = new SendMessage();
        messageAfterAfterSignUp.setChatId(String.valueOf(chatID));
        messageAfterAfterSignUp.setText("Записал тебя в лист ожидания! Если кто-то из партнёрш отвалится, " +
                "я автоматически запишу тебя к освободившемуся партнёру." +
                " Ну лучше договорись с партнёром и запишитесь тогда вместе.");
        Buttons.setButtonsAfterAddingToWaitingList(messageAfterAfterSignUp);
        try {
            execute(messageAfterAfterSignUp);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        sendListSampo(chatID);
    }

    public void sendMessageAfterInit(long chatID) {
        SendMessage messageAfterInit = new SendMessage();
        messageAfterInit.setChatId(String.valueOf(chatID));
        messageAfterInit.setText("Готово. Обновил информацию о тебе в нашей маленькой базе будущих чемпионов.\n" +
                "Больше эту процедуру проходить не нужно.\n" +
                "Записывайся на следующее сампо");
        messageAfterInit.setReplyMarkup(Buttons.buttonsBeforeSignUP());
        try {
            execute(messageAfterInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageAfterCancelAlone(long chatID) {
        SendMessage messageAfterInit = new SendMessage();
        messageAfterInit.setChatId(String.valueOf(chatID));
        messageAfterInit.setText("Удалил тебя из списка");
        messageAfterInit.setReplyMarkup(Buttons.buttonsBeforeSignUP());
        try {
            execute(messageAfterInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageAfterCancelPair(long chatID) {
        SendMessage messageAfterInit = new SendMessage();
        messageAfterInit.setChatId(String.valueOf(chatID));
        messageAfterInit.setText("Удалил вашу пару из списка");
        messageAfterInit.setReplyMarkup(Buttons.buttonsBeforeSignUP());
        try {
            execute(messageAfterInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void askWhatSex(Message message) {
        String firstName = message.getFrom().getFirstName();
        SendMessage messageInit = new SendMessage();
        messageInit.setChatId(message.getChatId().toString());
        messageInit.setText("Привет, " + firstName + "! Бот пока ещё не умеет определять пол. Подскажи, кто ты?");
        messageInit.setReplyMarkup(Buttons.setInitButtonsAfterStart());
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
            messageInit.setText("Твоя фамилия в телеграме: " + lastname + "\n Оставить эту фамилию? (нажми одну из кнопок ниже)");
        }
        messageInit.setReplyMarkup(Buttons.setInitButtonsAskUserLastname());
        try {
            execute(messageInit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void readDancerLastName(Message message) {
        long chatID = message.getChatId();
        String lastName = message.getText();
        Dancer dancer;
        String firstName = message.getFrom().getFirstName();
        String telegramName = message.getFrom().getUserName();
        if (DancerBase.isDancerWithThisChatID(chatID)) {
            dancer = DancerBase.getDancerByChatID(chatID);
            dancer.setFirstName(firstName);
            dancer.setLastName(lastName);
            dancer.setSex(dialogDancerSex.get(chatID));
            dancer.setTelegramName(telegramName);
        } else {
            if (DancerBase.hasDancerWithThisLastName(lastName)) {
                dancer = DancerBase.findDancerByLastName(lastName);
                dancer.setChatID(chatID);
                dancer.setTelegramName(telegramName);
            } else {
                DancerBase.dancerBase.add(new Dancer(firstName, lastName, dialogDancerSex.get(chatID), chatID, telegramName));
            }
        }
        try {
            Converter.saveDancerBaseToFile(DancerBase.dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
        waitingForLastName.put(chatID, false);
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

    public void sendListSampo(long chatID) {
        SendMessage messageWithList = new SendMessage(); // Create a SendMessage object with mandatory fields
        messageWithList.setChatId(String.valueOf(chatID));
        messageWithList.enableMarkdown(true);
        messageWithList.setText("*Текущий список на сампо 22 февраля:*\n(пока тестовый режим, список не сохраняется)\n\n"
                + nextSampoList.printToString()
                + "\n*Лист ожидания:*\n"
                + nextSampoWaitingList.printToString());
        messageWithList.setReplyMarkup(Buttons.buttonsBeforeSignUP());
        try {
            execute(messageWithList); // Call method to send the message
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


    private static void setupLog4J() {
        try {
            System.setProperty("log4j.configuration", new File(".", File.separatorChar + "log4j.properties").toURL().toString());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            System.setProperty("log4j.debug", new File(".", File.separatorChar + "log4j.debug").toURL().toString());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        setupLog4J();
        waitingForLastName = new HashMap<>();
        waitingForPartnerName = new HashMap<>();
        dialogDancerSex = new HashMap<>();

        try {
            DancerBase.dancerBase = util.Converter.readDancerBaseFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }


        nextSampoList = new ListSampo(LocalDate.of(2021, Month.FEBRUARY, 1));
        nextSampoWaitingList = new WaitingListSampo(LocalDate.of(2021, Month.FEBRUARY, 1));


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
