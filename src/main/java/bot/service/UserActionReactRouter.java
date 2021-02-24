package bot.service;

import bot.misc.Buttons;
import bot.model.Dancer;
import bot.model.Pair;
import bot.repository.ConfigRepository;
import bot.repository.DancerRepository;
import bot.repository.SampoListRepository;
import lombok.RequiredArgsConstructor;
import org.jvnet.hk2.annotations.Service;
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


@Service
@RequiredArgsConstructor
public class UserActionReactRouter {

    private final Map<Long, Boolean> waitingForPartnerName = new HashMap<>(); //статус, где записывается парой и указывае фамилию партнера/партнерши*/
    private final Map<Long, Boolean> waitingForLastName = new HashMap<>(); //статус, где юзеру нужно ввести свою фамилию
    //ключ - chatID, 0 - имя тг, 1 - фамилия тг, 2 - пол, 3 - ник в телеграме, 4 - жду фамилию самого юзера, 5 - жду фамилию партнера/партнерши
    private final Map<Long, String> dialogDancerSex = new HashMap<>(); //юзер при начале общения с ботом указывает свой пол
    private final DancerRepository dancerRepository;
    private final SampoListRepository sampoListRepository;
    private ConfigRepository configRepository;

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
                return signUpAlone(chatID);
            case "CancelAlone":
                return cancel(chatID, false);
            case "CancelPair":
                return cancel(chatID, true);
        }

        return List.of(replyToTelegramMsg(chatID, "Такому не обучен", null));
    }

    private List<SendMessage> reactToRequest(Message message) {
        long chatID = message.getChatId();

        var dancer = dancerRepository.getByChatId(chatID);

        if (waitingForLastName.get(chatID) == null) {
            waitingForLastName.put(chatID, false);
        }
        if (waitingForPartnerName.get(chatID) == null) {
            waitingForPartnerName.put(chatID, false);
        }

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
                .telegramName(telegramName)
                .sex(dialogDancerSex.get(chatID))
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
        if (sampoListRepository.isSigned(dancer1.getChatID())) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancer1.getFirstName() + " " + dancer1.getLastName() + " уже есть в списке.", null));
        }
        if (sampoListRepository.isSigned(dancer2.getChatID())) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancer2.getFirstName() + " " + dancer2.getLastName() + " уже есть в списке.", null));
        }

        var res = new ArrayList<SendMessage>();

        if (dancer1.getSex().equals(Dancer.LEADER)) {
            sampoListRepository.sign(dancer1, dancer2);
            res.add(replyToTelegramMsg(dancer2.getChatID(), "Ты записана на следующее сампо в паре с партнёром: "
                    + dancer1.getFirstName() + " " + dancer1.getLastName(), null));
        } else {
            sampoListRepository.sign(dancer2, dancer1);
            res.add(replyToTelegramMsg(dancer2.getChatID(), "Ты записан на следующее сампо в паре с партнёршей: "
                    + dancer1.getFirstName() + " " + dancer1.getLastName(), null));
        }

        res.add(sendMessageAfterSignUp(chatID));

        return res;
    }


    public List<SendMessage> parseDancerFromRequest(Message message) {
        String messageLastName = message.getText();
        long chatID = message.getChatId();
        Dancer firstDancer = dancerRepository.getByChatId(chatID);
        Dancer foundDancer = dancerRepository.getByLastName(messageLastName);
        if (foundDancer != null) {
            waitingForPartnerName.put(chatID, false);
            return signUpWithPartner(chatID, firstDancer, foundDancer);
        } else {
            return List.of(replyToTelegramMsg(chatID, messageLastName + " — не нашёл такой фамилии в базе танцоров. Либо опечатка (тогда нажми ещё раз" +
                    " «Записаться в паре» и введи фамилию." +
                    " Или пусть самостоятельно напишет боту и заполнит информацию о себе.", null));
        }

    }

    public List<SendMessage> signUpAlone(long chatID) {

        Dancer dancerToAdd = dancerRepository.getByChatId(chatID);
        if (sampoListRepository.isSigned(dancerToAdd.getChatID()) || sampoListRepository.isWaiting(chatID)) {
            return List.of(replyToTelegramMsg(chatID, "Ошибка: " + dancerToAdd.getFirstName() + " " + dancerToAdd.getLastName() +
                    " уже есть в списке", null));
        }

        var res = new ArrayList<SendMessage>();

        var freeDancer = sampoListRepository.getFirstWaiting();
        if (freeDancer != null && !freeDancer.getSex().equals(dancerToAdd.getSex())) {//если очередь не пуста и первый противоположенного пола
            sampoListRepository.sign(freeDancer, dancerToAdd);
            sampoListRepository.removeFromWaiting(freeDancer.getChatID());//удалить из листа ожидания
            res.add(replyToTelegramMsg(freeDancer.getChatID(), "Записал к тебе в пару танцора: " +
                    dancerToAdd.getFirstName() + " " + dancerToAdd.getLastName(), null));
            res.add(replyToTelegramMsg(chatID, "Записал тебя вместе с партнёршей: " +
                    freeDancer.getFirstName() + " " + freeDancer.getLastName(), null));
        } else {
            sampoListRepository.addWaiting(dancerToAdd.getChatID());
            sendMessageAfterAddingToWaitingList(chatID);
        }

        res.add(sendMessageAfterSignUp(chatID));

        return res;
    }

    public List<SendMessage> cancel(long chatID, boolean cancelPartner) {

        var res = new ArrayList<SendMessage>();

        Dancer dancerToRemove = dancerRepository.getByChatId(chatID);

        Pair pair = sampoListRepository.getPair(chatID);
        boolean isWaiting = sampoListRepository.isWaiting(chatID);

        if (pair == null && !isWaiting) {
            res.add(replyToTelegramMsg(chatID, "Тебя не было в списке", Buttons.buttonsBeforeSignUP()));
            return res;
        }

        if (isWaiting) {
            sampoListRepository.removeFromWaiting(dancerToRemove.getChatID());
        } else {
            sampoListRepository.removeFromMainListAndAddToWait(chatID);
        }
        res.add(replyToTelegramMsg(chatID, "Удалил тебя из списка" + (cancelPartner ? ", по просьбе партнера" : ""), Buttons.buttonsBeforeSignUP()));

        if (cancelPartner) {
            cancel(chatID, false);
        } else {
            res.addAll(signUpAlone(pair.getFollower().getChatID() == chatID ? pair.getLeader().getChatID() : pair.getFollower().getChatID()));
        }
        return res;
    }

    public SendMessage sendMessageAfterSignUp(long chatID) {
        return replyToTelegramMsg(chatID, "До встречи на сампо! Захвати с собой водички", Buttons.buttonsBeforeSignUP());
    }

    public SendMessage sendMessageAfterAddingToWaitingList(long chatID) {

        return replyToTelegramMsg(chatID, "Записал тебя в лист ожидания! Если кто-то из основного списка отвалится, " +
                "я автоматически запишу тебя к освободившемуся." +
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
                .telegramName(telegramName)
                .sex(dialogDancerSex.get(chatID))
                .build();
    }


    public List<SendMessage> sendListSampo(long chatID) {

        InlineKeyboardMarkup button;

        if (sampoListRepository.isWaiting(chatID)) {
            button = Buttons.buttonsAfterSignUPAlone();
        } else if (sampoListRepository.isSigned(chatID)) {
            button = Buttons.buttonsAfterSignUPPair();
        } else {
            button = Buttons.buttonsBeforeSignUP();
        }

        return List.of(replyToTelegramMsg(chatID, "*Текущий список на сампо " + configRepository.getNextSampoDate() + ":*\n\n"
                + sampoListRepository.getMainListAsText()
                + "\n*Лист ожидания:*\n"
                + sampoListRepository.getWaitingListAsText(), button));
    }
}
