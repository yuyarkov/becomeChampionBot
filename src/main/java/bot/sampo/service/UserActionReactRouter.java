package bot.sampo.service;

import bot.sampo.misc.Buttons;
import bot.sampo.model.Dancer;
import bot.sampo.model.Pair;
import bot.sampo.repository.ConfigRepository;
import bot.sampo.repository.DancerRepository;
import bot.sampo.repository.SampoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bot.sampo.misc.Buttons.buttonsBeforeSignUP;


@Service
@RequiredArgsConstructor
public class UserActionReactRouter {

    private final Map<Long, Boolean> waitingForPartnerName = new HashMap<>(); //статус, где записывается парой и указывае фамилию партнера/партнерши*/
    private final Map<Long, Boolean> waitingForLastName = new HashMap<>(); //статус, где юзеру нужно ввести свою фамилию
    //ключ - chatID, 0 - имя тг, 1 - фамилия тг, 2 - пол, 3 - ник в телеграме, 4 - жду фамилию самого юзера, 5 - жду фамилию партнера/партнерши
    private final Map<Long, Boolean> dialogDancerSex = new HashMap<>(); //юзер при начале общения с ботом указывает свой пол. true - лидер
    private final DancerRepository dancerRepository;
    private final SampoListRepository sampoListRepository;
    private final ConfigRepository configRepository;
    private final AdminRequestService adminRequestService;

    public List<SendMessage> getReaction(Update request) {

        if (adminRequestService.isAdminCommand(request) && adminRequestService.isAdmin(request.getMessage().getChatId())) {
            return adminRequestService.handle(request);
        }

        Message receivedText = request.getMessage();
        if (request.hasMessage() && receivedText.hasText()) {
            return reactToRequest(receivedText);
        } else if (request.hasCallbackQuery()) {
            return reactToCallBack(request.getCallbackQuery());
        }

        throw new RuntimeException("unexpected format");
    }

    private List<SendMessage> reactToCallBack(CallbackQuery callbackQuery) {
        long chatID = callbackQuery.getMessage().getChatId();
        switch (callbackQuery.getData()) {
            case "Leader":
                dialogDancerSex.put(chatID, true);
                return askWhatLastName(callbackQuery);
            case "Follower":
                dialogDancerSex.put(chatID, false);
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
                return signUpAlone(chatID);
            case "CancelAlone":
                return cancelAlone(chatID);
            case "CancelPair":
                return cancelPair(chatID);
        }

        return List.of(replyToTelegramMsg(chatID, "Такому не обучен", null));
    }

    private List<SendMessage> reactToRequest(Message message) {
        long chatID = message.getChatId();

        var dancer = dancerRepository.getDancerById(chatID);

        waitingForLastName.putIfAbsent(chatID, false);
        waitingForPartnerName.putIfAbsent(chatID, false);

        if (message.getText().equals("/start")) {

            if (dancer != null) {
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

    private Dancer parseCallBackToDancer(CallbackQuery callbackQuery) {
        String firstName = callbackQuery.getFrom().getFirstName();
        String lastName = callbackQuery.getFrom().getLastName();
        long chatID = callbackQuery.getMessage().getChatId();
        String telegramName = callbackQuery.getFrom().getUserName();

        return Dancer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .chatID(chatID)
                .leader(dialogDancerSex.get(chatID))
                .build();
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
        if (sampoListRepository.inPair(dancer1.getChatID())) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancer1.getFirstName() + " " + dancer1.getLastName() + " уже есть в списке.", null));
        }
        if (sampoListRepository.inPair(dancer2.getChatID())) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancer2.getFirstName() + " " + dancer2.getLastName() + " уже есть в списке.", null));
        }

        var res = new ArrayList<SendMessage>();

        sampoListRepository.writePair(getPair(dancer1, dancer2));
        res.add(replyToTelegramMsg(dancer2.getChatID(), "Ты записан(а) на следующее сампо в паре с : "
                + dancer1.getFirstName() + " " + dancer1.getLastName(), null));
        res.add(replyToTelegramMsg(dancer1.getChatID(), "Ты записан(а) на следующее сампо в паре с : "
                + dancer2.getFirstName() + " " + dancer2.getLastName(), null));
        res.add(sendMessageAfterSignUp(chatID));

        return res;
    }


    public List<SendMessage> parseDancerFromRequest(Message message) {
        String messageLastName = message.getText();
        long chatID = message.getChatId();
        Dancer firstDancer = dancerRepository.getDancerById(chatID);
        Dancer foundDancer = dancerRepository.getByLastName(messageLastName);
        waitingForPartnerName.put(chatID, false);
        if (foundDancer != null) {
            return signUpWithPartner(chatID, firstDancer, foundDancer);
        } else {
            return List.of(replyToTelegramMsg(chatID, messageLastName + " — не нашёл такой фамилии в базе танцоров. Либо опечатка (тогда нажми ещё раз" +
                    " «Записаться в паре» и введи фамилию." +
                    " Или пусть самостоятельно напишет боту и заполнит информацию о себе.", buttonsBeforeSignUP()));
        }

    }

    public List<SendMessage> signUpAlone(long chatID) {

        Dancer dancerToAdd = dancerRepository.getDancerById(chatID);
        if (sampoListRepository.inPair(dancerToAdd.getChatID()) || sampoListRepository.isWaiting(chatID)) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancerToAdd.getFirstName() + " " + dancerToAdd.getLastName() +
                    " уже есть в списке", null));
        }

        var res = new ArrayList<SendMessage>();

        var freeDancer = sampoListRepository.getFirstWaiting();
        if (freeDancer != null && !freeDancer.isLeader() == dancerToAdd.isLeader()) {//если очередь не пуста и первый противоположенного пола
            sampoListRepository.writePair(getPair(freeDancer, dancerToAdd));
            sampoListRepository.removeFromWaiting(freeDancer.getChatID());//удалить из листа ожидания
            res.add(replyToTelegramMsg(freeDancer.getChatID(), "Записал к тебе в пару танцора: " +
                    dancerToAdd.getFirstName() + " " + dancerToAdd.getLastName(), null));
            res.add(replyToTelegramMsg(chatID, "Записал к тебе в пару танцора: " +
                    freeDancer.getFirstName() + " " + freeDancer.getLastName(), null));
        } else {
            sampoListRepository.addWaiting(dancerToAdd.getChatID());
            res.add(sendMessageAfterAddingToWaitingList(chatID));
        }

        res.add(sendMessageAfterSignUp(chatID));

        return res;
    }

    public List<SendMessage> cancelPair(long chatID) {

        var res = new ArrayList<SendMessage>();

        Pair pair = sampoListRepository.getPair(chatID);
        boolean isWaiting = sampoListRepository.isWaiting(chatID);

        if (pair == null && !isWaiting) {
            res.add(replyToTelegramMsg(chatID, "Тебя не было в списке", buttonsBeforeSignUP()));
            return res;
        }

        if (isWaiting) {
            sampoListRepository.removeFromWaiting(chatID);
            res.add(sendMessageAfterCancelAlone(chatID));
        } else {
            var partnerId = pair.getFollower().getChatID() == chatID ? pair.getLeader().getChatID() : pair.getFollower().getChatID();
            sampoListRepository.removeFromPairList(chatID);
            res.add(sendMessageAfterCancelPair(chatID));
            res.add(sendMessageAfterCancelPair(partnerId));
        }
        return res;
    }

    public List<SendMessage> cancelAlone(long chatID) {

        var res = new ArrayList<SendMessage>();

        Pair pair = sampoListRepository.getPair(chatID);
        boolean isWaiting = sampoListRepository.isWaiting(chatID);

        if (pair == null && !isWaiting) {
            res.add(replyToTelegramMsg(chatID, "Тебя не было в списке", buttonsBeforeSignUP()));
            return res;
        }

        if (isWaiting) {
            sampoListRepository.removeFromWaiting(chatID);
            res.add(sendMessageAfterCancelAlone(chatID));
        } else {
            sampoListRepository.removeFromPairList(chatID);
            res.add(sendMessageAfterCancelAlone(chatID));

            var dancer = dancerRepository.getDancerById(chatID);

            var partnerId = pair.getFollower().getChatID() == chatID ? pair.getLeader().getChatID() : pair.getFollower().getChatID();
            res.add(replyToTelegramMsg(partnerId, dancer.getLastName() + " не придет.", null));

            res.addAll(signUpAlone(partnerId));
        }
        return res;
    }

    public SendMessage sendMessageAfterSignUp(long chatID) {
        return replyToTelegramMsg(chatID, "До встречи на сампо! Захвати с собой водички", buttonsBeforeSignUP());
    }

    public SendMessage sendMessageAfterAddingToWaitingList(long chatID) {

        return replyToTelegramMsg(chatID, "Записал тебя в лист ожидания! Если кто-то из основного списка отвалится, " +
                "я автоматически запишу тебя к освободившемуся." +
                " Ну лучше договорись с партнёром и запишитесь тогда вместе.", Buttons.buttonsAfterSignUPAlone());

    }

    public List<SendMessage> sendMessageAfterInit(long chatID) {

        return List.of(replyToTelegramMsg(chatID, """
                Готово. Добавил информацию о тебе в нашей маленькой базе будущих чемпионов.
                Больше эту процедуру проходить не нужно.
                Записывайся на следующее сампо""", buttonsBeforeSignUP()));

    }

    public SendMessage sendMessageAfterCancelAlone(long chatID) {
        return replyToTelegramMsg(chatID, "Удалил тебя из списка", buttonsBeforeSignUP());
    }

    public SendMessage sendMessageAfterCancelPair(long chatID) {
        return replyToTelegramMsg(chatID, "Удалил вашу пару из списка", buttonsBeforeSignUP());
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
        var dancer = parseMessageToDancer(message);
        dancerRepository.save(dancer);
        waitingForLastName.put(chatID, false);
        return sendMessageAfterInit(chatID);
    }

    private Dancer parseMessageToDancer(Message message) {
        long chatID = message.getChatId();
        String lastName = message.getText();
        String firstName = message.getFrom().getFirstName();
        String telegramName = message.getFrom().getUserName();

        return Dancer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .chatID(chatID)
                .leader(dialogDancerSex.get(chatID))
                .build();
    }


    public List<SendMessage> sendListSampo(long chatID) {

        InlineKeyboardMarkup button;

        if (sampoListRepository.isWaiting(chatID)) {
            button = Buttons.buttonsAfterSignUPAlone();
        } else if (sampoListRepository.inPair(chatID)) {
            button = Buttons.buttonsAfterSignUPPair();
        } else {
            button = buttonsBeforeSignUP();
        }

        return List.of(replyToTelegramMsg(chatID, "*Текущий список на сампо " + configRepository.getCurrentSampoDate() + ":*\n\n"
                + sampoListRepository.getPairListAsText()
                + "\n*Лист ожидания:*\n"
                + sampoListRepository.getWaitingListAsText(), button));
    }

    private Pair getPair(Dancer dancer1, Dancer dancer2) {
        return Pair
                .builder()
                .follower(dancer1.isLeader() ? dancer2 : dancer1)
                .leader(dancer1.isLeader() ? dancer1 : dancer2)
                .build();
    }

}
