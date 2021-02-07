
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


import util.Converter;
import pojo.Dancer;

import static java.lang.Math.toIntExact;

public class becomeChampionBot extends TelegramLongPollingBot {
    private String botToken = System.getenv("TOKEN_BECOME_CHAMPION_BOT");
    public static final long CHAT_ID_DETOCHKIN = 238349375L;

    public static long chatWaiting = 0; //статус чата, в котором бот запросил инфу от юзера и ждёт ответа
    public static boolean waitingForLastName;//статус, где юзеру нужно ввести свою фамилию

    public static String dialogDancerSex = null; //юзер при начале общения с ботом указывает свой пол
    public static String updatedListSampo;
    public static ListSampo nextSampoList;
    public static WaitingListSampo nextSampoWaitingList;

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
                DancerBase.createDancer(callbackQuery);
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


    public void reactToRequest(Message message) {
        long chatID = message.getChatId();
        String currentDancerSex = DancerBase.getDancerByChatID(chatID).getSex();
        if (message.hasText() && chatWaiting != 0 && chatID == chatWaiting && !waitingForLastName) {
            parseDancerFromRequest(message);
        }

        if (message.hasText() && waitingForLastName && chatID == chatWaiting) {
            readDancerLastName(message);
        }

        switch (message.getText()) {
            case "Записаться без пары":
                if (currentDancerSex.equals(Dancer.FOLLOWER)) {
                    signUpLonelyGirl(message);
                } else {
                    signUpAlone(message);
                }
                break;
            case "/start":
                askWhatSex(message);
                break;
            case "Я не смогу прийти":
                cancelAlone(message);
                break;
            case "Записаться парой":
                askPartnerName(message);
                break;
            case "Посмотреть список":
                sendListSampo(message.getChatId());
                break;
            case "Не сможем прийти вдвоём":
                cancelPair(message);
                break;
        }

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

    public void signUpLonelyGirl(Message message) {
        long chatID = message.getChatId();
        Dancer dancertoAdd = DancerBase.getDancerByChatID(chatID);
        boolean success = nextSampoWaitingList.addToWaitingList(dancertoAdd);
        if (!success) {
            replyToTelegram(message.getChatId(), "Ты уже в листе ожидания");
        }
        sendMessageAfterAddingToWaitingList(chatID);
    }

    public void signUpAlone(Message message) {
        long chatID = message.getChatId();
        Dancer dancertoAdd = DancerBase.getDancerByChatID(chatID);
        if (dancertoAdd.getSex().equals(Dancer.LEADER)&&nextSampoWaitingList.hasDancersInWaitingList()) {
            Dancer dancer2=nextSampoWaitingList.removeFirstFromWaitingList();
            nextSampoList.addPairToList(dancertoAdd,dancer2);
            replyToTelegram(dancer2.getChatID(),"Перенёс тебя из листа ожидания в основной список," +
                    "в пару к танцору: "+dancertoAdd.getFirstName()+" "+dancertoAdd.getLastName());
            replyToTelegram(chatID,"Записал тебя вместе с партнёршей из листа ожидания: "+
                    dancer2.getFirstName()+" "+dancer2.getLastName());
            return;
        }

        boolean success = nextSampoList.addDancerToList(dancertoAdd);
        if (success) {
            updatedListSampo = nextSampoList.printToString();
        } else {
            replyToTelegram(message.getChatId(), "Ты уже в списке");
        }
        sendMessageAfterSignUp(chatID);
    }

    public void cancelAlone(Message message) {
        long chatID = message.getChatId();
        Dancer dancertoRemove = DancerBase.getDancerByChatID(chatID);

        nextSampoList.removeDancerFromList(dancertoRemove);
        if (dancertoRemove.getSex().equals(Dancer.FOLLOWER) && nextSampoWaitingList.hasDancersInWaitingList()) {
            Dancer dancerToAdd = nextSampoWaitingList.removeFirstFromWaitingList();
            nextSampoList.addFollowerToEmptySlot(dancerToAdd);
            replyToTelegram(dancerToAdd.getChatID(),"Перенёс тебя из списка ожидания в основной список, " +
                    "в пару к танцору: "+ nextSampoList.findPairByDancer(dancerToAdd));
        }
        updatedListSampo = nextSampoList.printToString();
        sendMessageAfterCancelAlone(chatID);
    }

    public void cancelPair(Message message) {
        long chatID = message.getChatId();
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

        Buttons.setButtonsAfterSignUP(messageAfterAfterSignUp);
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
                "Записывайся на следующее сампо (снизу должны быть три крупные кнопки. Если их нет, нажми кнопку рядом с микрофончиком.");
        Buttons.setButtonsBeforeSignUP(messageAfterInit);
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
        Buttons.setButtonsBeforeSignUP(messageAfterInit);
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
        Buttons.setButtonsBeforeSignUP(messageAfterInit);
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
            messageInit.setText("Твоя фамилия в телеграме: " + lastname + "\n Оставить эту фамилию?");
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
            dancer.setSex(dialogDancerSex);
            dancer.setTelegramName(telegramName);
        } else {
            if (DancerBase.hasDancerWithThisLastName(lastName)) {
                dancer = DancerBase.findDancerByLastName(lastName);
                dancer.setChatID(chatID);
                dancer.setTelegramName(telegramName);
            } else {
                DancerBase.dancerBase.add(new Dancer(firstName, lastName, dialogDancerSex, chatID, telegramName));
            }
        }
        try {
            Converter.saveDancerBaseToFile(DancerBase.dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
        waitingForLastName = false;
        chatWaiting = 0;
        dialogDancerSex = null;
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
        messageWithList.setText("*Текущий список на сампо 15 февраля:*\n(пока тестовый режим, список не сохраняется)\n"
                + nextSampoList.printToString()
                + "\n*Лист ожидания:*\n"
                + nextSampoWaitingList.printToString());
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
