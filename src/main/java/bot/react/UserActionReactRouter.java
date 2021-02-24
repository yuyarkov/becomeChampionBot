package bot.react;

import bot.*;
import bot.repository.DancerRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserActionReactRouter {

    private static Map<Long, Boolean> waitingForPartnerName = new HashMap<>(); //статус, где записывается парой и указывае фамилию партнера/партнерши*/
    private static Map<Long, Boolean> waitingForLastName = new HashMap<>(); //статус, где юзеру нужно ввести свою фамилию
    //ключ - chatID, 0 - имя тг, 1 - фамилия тг, 2 - пол, 3 - ник в телеграме, 4 - жду фамилию самого юзера, 5 - жду фамилию партнера/партнерши
    private static Map<Long, String> dialogDancerSex = new HashMap<>(); //юзер при начале общения с ботом указывает свой пол
    private static String updatedListSampo;
    private static ListSampo nextSampoList = new ListSampo(LocalDate.of(2021, Month.MARCH, 1));
    ;
    private static WaitingListSampo nextSampoWaitingList = new WaitingListSampo(LocalDate.of(2021, Month.MARCH, 1));
    private static DancerRepository dancerRepository = null;

    public List<SendMessage> getReaction(Update request) {

        Message receivedText = request.getMessage();
        if (request.hasMessage() && receivedText.hasText()) {
            return reactToRequest(receivedText);
        } else if (request.hasCallbackQuery()) {
            return reactToCallBack(request.getCallbackQuery(), receivedText);
        }

        throw new RuntimeException("unexpected format");
    }

    private List<SendMessage> reactToCallBack(CallbackQuery callbackQuery, Message message) {
        long chatID = callbackQuery.getMessage().getChatId();
        String currentDancerSex = dancerRepository.getByChatId(chatID).getSex();
        String firstName = callbackQuery.getFrom().getFirstName();
        String lastName = callbackQuery.getFrom().getLastName();
        switch (callbackQuery.getData()) {
            case "Leader":
                dialogDancerSex.put(chatID, Dancer.LEADER);
                return askWhatLastName(callbackQuery);
            case "Follower":
                dialogDancerSex.put(chatID, Dancer.FOLLOWER);
                return askWhatLastName(callbackQuery);
            case "lastnameok":
                dancerRepository.save(parseCallBackToDancer(callbackQuery));
                waitingForLastName.put(chatID, false);
                return sendMessageAfterInit(chatID);
            case "readmylastname":
                waitingForLastName.put(chatID, true);
                return List.of(replyToTelegramMsg(chatID, "Понял. Напиши свою фамилию русскими буквами без кавычек:", null));
            case "ShowList":
                return sendListSampo(chatID);
            case "SignUpPair":
                return askPartnerName(chatID);
            case "SignUpAlone":
                if (currentDancerSex.equals(Dancer.FOLLOWER)) {
                    return signUpLonelyGirl(message);
                } else {
                    return signUpAlone(chatID);
                }
            case "CancelAlone":
                return cancelAlone(chatID);
            case "CancelPair":
                return cancelPair(chatID);
        }

        return List.of(replyToTelegramMsg(chatID, "Такому не обучен", null));
    }

    private Dancer parseCallBackToDancer(CallbackQuery callbackQuery) {
        String firstName = callbackQuery.getFrom().getFirstName();
        String lastName = callbackQuery.getFrom().getLastName();
        long chatID = callbackQuery.getMessage().getChatId();
        String telegramName = callbackQuery.getFrom().getUserName();
        return new Dancer(firstName, lastName, dialogDancerSex.get(chatID), chatID, telegramName);
    }

    private List<SendMessage> reactToRequest(Message message) {
        long chatID = message.getChatId();
        String currentDancerSex = DancerBase.getDancerByChatID(chatID).getSex();
        if (waitingForLastName.get(chatID) == null) {
            waitingForLastName.put(chatID, false);
        }
        if (waitingForPartnerName.get(chatID) == null) {
            waitingForPartnerName.put(chatID, false);
        }

        if (message.getText().equals("/start")) {
            if (DancerBase.isDancerWithThisChatID(chatID)) {
                Dancer dancer = DancerBase.getDancerByChatID(chatID);

                var res = new ArrayList<SendMessage>();

                res.add(replyToTelegramMsg(chatID, "Привет, " + message.getFrom().getFirstName() + "!" +
                        " Бот уже записал тебя в базу под именем *" + dancer.getFirstName() + " " + dancer.getLastName() + "*\n" +
                        "Больше эту процедуру проходить не нужно, записывайся на сампо:", null));
                res.addAll(sendListSampo(chatID));

                return res;
            } else {
                return List.of(askWhatSex(message));
            }
        }

        if (message.getText().equals("Посмотреть список")) {
            return sendListSampo(chatID);
        }

        if (message.hasText() && !waitingForPartnerName.isEmpty() && waitingForPartnerName.get(chatID)) {
            return parseDancerFromRequest(message);
        }

        if (message.hasText() && !waitingForLastName.isEmpty() && waitingForLastName.get(chatID)) {
            return readDancerLastName(message);
        }

        return List.of(replyToTelegramMsg(chatID, "Такому не обучен", null));

    }

    private SendMessage replyToTelegramMsg(long chatID, String text, ReplyKeyboard replyMarkup) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(String.valueOf(chatID));
        message.enableMarkdown(true);
        message.setText(text);
        message.setReplyMarkup(replyMarkup);
        message.enableMarkdown(true);
        return message;
    }


    public List<SendMessage> askPartnerName(long chatID) {
        waitingForPartnerName.put(chatID, true);
        return List.of(replyToTelegramMsg(chatID, "Давай запишу. Скажи, с кем, напиши пожалуйста фамилию:", null));
    }

    public List<SendMessage> signUpWithPartner(long chatID, Dancer dancer1, Dancer dancer2) {
        if (nextSampoList.isAlreadySigned(dancer1)) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancer1.getFirstName() + " " + dancer1.getLastName() + " уже есть в списке.", null));
        }
        if (nextSampoList.isAlreadySigned(dancer2)) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancer2.getFirstName() + " " + dancer2.getLastName() + " уже есть в списке.", null));
        }

        var res = new ArrayList<SendMessage>();

        if (dancer1.getSex().equals(Dancer.LEADER)) {
            nextSampoList.addPairToList(dancer1, dancer2);
            res.add(replyToTelegramMsg(dancer2.getChatID(), "Ты записана на следующее сампо в паре с партнёром: "
                    + dancer1.getFirstName() + " " + dancer1.getLastName(), null));
        } else {
            nextSampoList.addPairToList(dancer2, dancer1);
            res.add(replyToTelegramMsg(dancer2.getChatID(), "Ты записан на следующее сампо в паре с партнёршей: "
                    + dancer1.getFirstName() + " " + dancer1.getLastName(), null));
        }

        res.add(sendMessageAfterSignUp(chatID));

        return res;
    }


    public List<SendMessage> parseDancerFromRequest(Message message) {
        String messageLastName = message.getText();
        long chatID = message.getChatId();
        Dancer firstDancer = DancerBase.getDancerByChatID(chatID);
        Dancer foundDancer = DancerBase.findDancerByLastName(messageLastName);
        if (!foundDancer.equals(DancerBase.emptyDancer)) {
            waitingForPartnerName.put(chatID, false);
            return signUpWithPartner(chatID, firstDancer, foundDancer);
        } else {
            return List.of(replyToTelegramMsg(chatID, messageLastName + " — не нашёл такой фамилии в базе танцоров. Либо опечатка (тогда нажми ещё раз" +
                    " «Записаться в паре» и введи фамилию." +
                    " Или пусть самостоятельно напишет боту и заполнит информацию о себе.", null));
        }

    }


    public List<SendMessage> signUpLonelyGirl(Message message) {
        long chatID = message.getChatId();
        Dancer dancertoAdd = DancerBase.getDancerByChatID(chatID);
        if (nextSampoList.isAlreadySigned(dancertoAdd)) {
            return List.of(replyToTelegramMsg(chatID, "Ты уже есть в основном списке))", null));
        }
        boolean success = nextSampoWaitingList.addToWaitingList(dancertoAdd);
        if (!success) {
            return List.of(replyToTelegramMsg(message.getChatId(), "Ты уже в листе ожидания", null));
        } else {
            return List.of(sendMessageAfterAddingToWaitingList(chatID));
        }
    }

    public List<SendMessage> signUpAlone(long chatID) {

        Dancer dancertoAdd = DancerBase.getDancerByChatID(chatID);
        if (nextSampoList.isAlreadySigned(dancertoAdd)) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancertoAdd.getFirstName() + " " + dancertoAdd.getLastName() +
                    " уже есть в списке", null));
        }
        if (dancertoAdd.getSex().equals(Dancer.LEADER) && nextSampoList.hasEmptySlotForLeader()) {
            Dancer dancer2 = nextSampoList.getFollowerWithEmptySlot();
            nextSampoList.addLeaderToEmptySlot(dancertoAdd);
            updatedListSampo = nextSampoList.printToString();
            return List.of(replyToTelegramMsg(dancer2.getChatID(), "Записал к тебе в пару танцора: " +
                            dancertoAdd.getFirstName() + " " + dancertoAdd.getLastName(), null),
                    replyToTelegramMsg(chatID, "Записал тебя вместе с партнёршей: " +
                            dancer2.getFirstName() + " " + dancer2.getLastName(), null));
        }

        if (dancertoAdd.getSex().equals(Dancer.LEADER) && nextSampoWaitingList.hasDancersInWaitingList()) {
            Dancer dancer2 = nextSampoWaitingList.removeFirstFromWaitingList();
            nextSampoList.addPairToList(dancertoAdd, dancer2);
            updatedListSampo = nextSampoList.printToString();
            return List.of(replyToTelegramMsg(dancer2.getChatID(), "Перенёс тебя из листа ожидания в основной список," +
                            "в пару к танцору: " + dancertoAdd.getFirstName() + " " + dancertoAdd.getLastName(), null),
                    replyToTelegramMsg(chatID, "Записал тебя вместе с партнёршей из листа ожидания: " +
                            dancer2.getFirstName() + " " + dancer2.getLastName(), null));
        }

        boolean success = nextSampoList.addDancerToList(dancertoAdd);
        var res = new ArrayList<SendMessage>();
        if (success) {
            updatedListSampo = nextSampoList.printToString();
        } else {
            res.add(replyToTelegramMsg(chatID, "Ты уже в списке", null));
        }
        res.add(sendMessageAfterSignUp(chatID));

        return res;
    }

    public List<SendMessage> cancelAlone(long chatID) {

        var res = new ArrayList<SendMessage>();

        Dancer dancertoRemove = DancerBase.getDancerByChatID(chatID);

        nextSampoList.removeDancerFromList(dancertoRemove);
        if (dancertoRemove.getSex().equals(Dancer.FOLLOWER) && nextSampoWaitingList.hasDancersInWaitingList()) {
            Dancer dancerToAdd = nextSampoWaitingList.removeFirstFromWaitingList();
            nextSampoList.addFollowerToEmptySlot(dancerToAdd);
            res.add(replyToTelegramMsg(dancerToAdd.getChatID(), "Перенёс тебя из списка ожидания в основной список, " +
                    "в пару к танцору: " + nextSampoList.findPairByDancer(dancerToAdd), null));
        }
        updatedListSampo = nextSampoList.printToString();
        res.add(replyToTelegramMsg(chatID, "Удалил тебя из списка", Buttons.buttonsBeforeSignUP()));

        return res;
    }

    public List<SendMessage> cancelPair(long chatID) {

        var res = new ArrayList<SendMessage>();

        Dancer firstDancerToRemove = DancerBase.getDancerByChatID(chatID);

        Dancer secondDancerToRemove = nextSampoList.findPairByDancer(firstDancerToRemove);
        System.out.println("буду удалять пару: " + firstDancerToRemove.getLastName() + " - " + secondDancerToRemove.getLastName());

        if (secondDancerToRemove == null) {
            return List.of(replyToTelegramMsg(chatID, "Не вижу в списке вашу пару. Чтобы отменить запись, надо сначала записаться))", null));
        }

        nextSampoList.removePairFromList(firstDancerToRemove, secondDancerToRemove);

        updatedListSampo = nextSampoList.printToString();
        res.add(replyToTelegramMsg(chatID, "Удалил из списка вашу пару: " + firstDancerToRemove.getLastName() + " - " + secondDancerToRemove.getLastName(), null));
        res.add(sendMessageAfterCancelPair(chatID));
        if (firstDancerToRemove.getSex().equals(Dancer.LEADER)) {
            res.add(replyToTelegramMsg(secondDancerToRemove.getChatID(), firstDancerToRemove.getFirstName() +
                    " " + firstDancerToRemove.getLastName() + " попросил отменить вашу запись на сампо. Удалил вас из списка!", null));
        } else {
            res.add(replyToTelegramMsg(secondDancerToRemove.getChatID(), firstDancerToRemove.getFirstName() +
                    " " + firstDancerToRemove.getLastName() + " попросила отменить вашу запись на сампо. Удалил вас из списка!", null));
        }

        return res;
    }

    public SendMessage sendMessageAfterSignUp(long chatID) {
        return replyToTelegramMsg(chatID, "До встречи на сампо! Захвати с собой водички", Buttons.buttonsBeforeSignUP());
    }

    public SendMessage sendMessageAfterAddingToWaitingList(long chatID) {

        return replyToTelegramMsg(chatID, "Записал тебя в лист ожидания! Если кто-то из партнёрш основного списка отвалится, " +
                "я автоматически запишу тебя к освободившемуся партнёру." +
                " Ну лучше договорись с партнёром и запишитесь тогда вместе.", Buttons.buttonsAfterSignUPAlone());

    }

    public List<SendMessage> sendMessageAfterInit(long chatID) {

        return List.of(replyToTelegramMsg(chatID, "Готово. Добавил информацию о тебе в нашей маленькой базе будущих чемпионов.\n" +
                "Больше эту процедуру проходить не нужно.\n" +
                "Записывайся на следующее сампо", Buttons.buttonsBeforeSignUP()));

    }

    public SendMessage sendMessageAfterCancelAlone(long chatID) {
        return replyToTelegramMsg(chatID, "Удалил тебя из списка", Buttons.buttonsBeforeSignUP());
    }

    public SendMessage sendMessageAfterCancelPair(long chatID) {
        return replyToTelegramMsg(chatID, "Удалил вашу пару из списка", Buttons.buttonsBeforeSignUP());
    }

    public SendMessage askWhatSex(Message message) {
        String firstName = message.getFrom().getFirstName();

        return replyToTelegramMsg(message.getChatId(), "Привет, " + firstName + "! Бот сначала должен записать тебя в базу. " +
                "Эту процедуру нужно проделать один раз. Подскажи, кто ты?", Buttons.buttonsAfterStart());

    }

    public List<SendMessage> askWhatLastName(CallbackQuery callbackQuery) {
        String lastname = callbackQuery.getFrom().getLastName();
        var chatId = callbackQuery.getMessage().getChatId();
        var button = Buttons.buttonsAskUserLastname();
        if (lastname == null) {
            return List.of(replyToTelegramMsg(chatId,
                    "У тебя в телеграме не заполнена фамилия. Давай запишу правильную фамилию, с которой ты будешь записываться на сампо.",
                    button));
        } else {
            return List.of(replyToTelegramMsg(chatId, "Твоя фамилия в телеграме: " + lastname + "\n Исправить? (нажми одну из кнопок ниже)",
                    button));
        }
    }

    public List<SendMessage> readDancerLastName(Message message) {
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
        return sendMessageAfterInit(chatID);
    }


    public List<SendMessage> sendListSampo(long chatID) {

        InlineKeyboardMarkup button;

        SendMessage messageWithList = new SendMessage(); // Create a SendMessage object with mandatory fields
        messageWithList.setChatId(String.valueOf(chatID));
        messageWithList.enableMarkdown(true);
        messageWithList.setText("*Текущий список на сампо 1 марта:*\n\n"
                + nextSampoList.printToString()
                + "\n*Лист ожидания:*\n"
                + nextSampoWaitingList.printToString());
        if (nextSampoList.isAlreadySignedAlone(chatID) || nextSampoWaitingList.isAlredySigned(chatID)) {
            button = Buttons.buttonsAfterSignUPAlone();
        } else if (nextSampoList.isAlreadySignedInPair(chatID)) {
            button = Buttons.buttonsAfterSignUPPair();
        } else {
            button = Buttons.buttonsBeforeSignUP();
        }

        return List.of(replyToTelegramMsg(chatID, "*Текущий список на сампо 1 марта:*\n\n"
                + nextSampoList.printToString()
                + "\n*Лист ожидания:*\n"
                + nextSampoWaitingList.printToString(), button));
    }
}
