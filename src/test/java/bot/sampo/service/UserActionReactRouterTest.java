package bot.sampo.service;

import bot.sampo.CommonContext;
import bot.sampo.repository.ConfigRepository;
import bot.sampo.repository.DancerRepository;
import bot.sampo.repository.SampoListRepository;
import bot.sampo.util.BotRequestHelper;
import bot.sampo.util.BotResponse;
import bot.sampo.util.TelegramUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class UserActionReactRouterTest extends CommonContext {

    @Autowired
    private BotRequestHelper helper;

    @Autowired
    private DancerRepository dancerRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private SampoListRepository sampoListRepository;

    @BeforeEach
    void init() {
        configRepository.setCurrentSampoDate(LocalDate.now());
    }

    /**
     * Инициализация:
     * - дата сампо
     * <p>
     * Сценарий:
     * - новый обученный пользователь регистрируется в системе
     * - получает список сампо
     * <p>
     * Итог:
     * - пользователь создан в базе
     * - получен пустой лист сампо
     */
    @Test
    void startTest() {

        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();

        var resp = doRegistration(user, "user one", "Leader");

        assertThat(resp.getButtons()).containsExactlyInAnyOrder("ShowList", "SignUpPair", "SignUpAlone");

        resp = helper.pushButton(resp, "ShowList", user).getBotResponse(user);

        var newDancer = dancerRepository.getDancerById(1L);

        assertThat(newDancer.getFirstName()).isEqualTo("user1");
        assertThat(newDancer.getLastName()).isEqualTo("user one");
    }

    /*
    Запись на сампо в паре партнер и партнерша уже зарегистрированы

    Итог:
        -Записаны в основной список
        -оба получили уведомление
     */
    @Test
    void pairAllRight() {

        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();

        var resp = doRegistration(user, "user one", "Leader");
        resp = doRegistration(user2, "user two", "Follower");

        assertThat(resp.getButtons()).containsExactlyInAnyOrder("SignUpPair", "SignUpAlone", "ShowList");

        resp = helper.pushButton(resp, "SignUpPair", user2).getBotResponse(user2);
        assertThat(resp.getMessage()).contains("Скажи, с кем");
        var pResp = helper.sendMsg("user one", user2);

        var user1Resp = pResp.getBotResponse(user);
        var user2Resp = pResp.getBotResponse(user2);

        assertThat(user1Resp.getMessage()).contains("Ты записан(а)");
        assertThat(user2Resp.getMessage()).contains("Ты записан(а)");


        var pair = sampoListRepository.getPair(user1Resp.getClientId());

        assertThat(sampoListRepository.inPair(1L)).isTrue();
        assertThat(sampoListRepository.inPair(2L)).isTrue();

        assertThat(pair.getLeader().getChatID()).isEqualTo(1L);
        assertThat(pair.getFollower().getChatID()).isEqualTo(2L);

        assertThat(pair.getLeader().isLeader()).isTrue();
        assertThat(pair.getFollower().isLeader()).isFalse();
    }



    /*
    Запись на сампо в паре но партнер не зарегистрирован

    Итог:
        -Выдача сообщения об ошибке

     */

    @Test
    void pairPartnerNotExist() {

        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();

        var resp = doRegistration(user2, "user two", "Follower");

        assertThat(resp.getButtons()).containsExactlyInAnyOrder("SignUpPair", "SignUpAlone", "ShowList");

        resp = helper.pushButton(resp, "SignUpPair", user2).getBotResponse(user2);
        assertThat(resp.getMessage()).contains("Скажи, с кем");
        var pResp = helper.sendMsg("not exists", user2);

        var user2Resp = pResp.getBotResponse(user2);

        assertThat(user2Resp.getMessage()).contains("not exists — не нашёл такой");

    }

     /*
    Запись на сампо при опечатке в фамилии
    -оба партнера зарегистрированы
    -партнер пробует записать и доускает ошибку в фамилии
    -получает сообщение об ошибке
    -нажимает кнопку записи повторно


    Итог:
        - успешно записаны

     */

    @Test
    void pairPartnerTypo() {

        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();

        var resp = doRegistration(user, "user one", "Leader");
        resp = doRegistration(user2, "user two", "Follower");

        resp = helper.pushButton(resp, "SignUpPair", user2).getBotResponse(user2);
        assertThat(resp.getMessage()).contains("Скажи, с кем");
        var pResp = helper.sendMsg("not exists", user2);

        var user2Resp = pResp.getBotResponse(user2);

        assertThat(user2Resp.getMessage()).contains("not exists — не нашёл такой");

        resp = helper.pushButton(user2Resp, "SignUpPair", user2).getBotResponse(user2);
        assertThat(resp.getMessage()).contains("Скажи, с кем");
        pResp = helper.sendMsg("user one", user2);

        var user1Resp = pResp.getBotResponse(user);
        user2Resp = pResp.getBotResponse(user2);

        assertThat(user1Resp.getMessage()).contains("Ты записан(а)");
        assertThat(user2Resp.getMessage()).contains("Ты записан(а)");

        assertThat(sampoListRepository.inPair(1L)).isTrue();
        assertThat(sampoListRepository.inPair(2L)).isTrue();

        var pair = sampoListRepository.getPair(user1Resp.getClientId());

        assertThat(pair.getLeader().getChatID()).isEqualTo(1L);
        assertThat(pair.getFollower().getChatID()).isEqualTo(2L);

        assertThat(pair.getLeader().isLeader()).isTrue();
        assertThat(pair.getFollower().isLeader()).isFalse();

    }


    /*
    Отмена сампо парой

    Итог:
    - Пара отменена записей в базе не осталось
    - оба участника получили инфо об отмене
     */
    @Test
    void cancelPair() {
        pairAllRight();

        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();

        var resp = helper.sendMsg("/start", user).getBotResponse(user);

        var pRest = helper.pushButton(resp, "CancelPair", user);

        var user1Resp = pRest.getBotResponse(user);
        var user2Resp = pRest.getBotResponse(user2);

        assertThat(user2Resp.getMessage()).contains("Удалил вашу пару");
        assertThat(user1Resp.getMessage()).contains("Удалил вашу пару");

        assertThat(sampoListRepository.inPair(1L)).isFalse();
        assertThat(sampoListRepository.inPair(2L)).isFalse();
    }


    /*
    Отмена сампо одним участником при пустом листе ожидания

    Итог:
    -Пара отменена
    -другой участник в листе ожидания
    -другой получает уведомление об отмене
     */
    @Test
    void cancelPairAlone() {

        pairAllRight();

        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();

        var resp = helper.sendMsg("/start", user).getBotResponse(user);

        var pRest = helper.pushButton(resp, "CancelAlone", user);

        var user1Resp = pRest.getBotResponse(user);
        var user2Resp = pRest.getBotResponse(user2);

        assertThat(user1Resp.getMessage()).contains("Удалил тебя из списка");
        assertThat(user2Resp.getMessage()).contains("Записал тебя в лист ожидания");

        assertThat(sampoListRepository.inPair(1L)).isFalse();
        assertThat(sampoListRepository.inPair(2L)).isFalse();
        assertThat(sampoListRepository.isWaiting(2L)).isTrue();

    }


    /*
    Отмена сампо одним участником при наличии в листе ожидания противоположенного пола
    -Пара отменена
    -другой участник записан с другим партнером
    -другой получает уведомление о смене партнера

     */
    @Test
    void cancelPairAloneWithFreeDancerInWaitList() {

        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();
        var user3 = TelegramUser.builder().firstName("user3").lastName(null).id(3L).build();

        pairAllRight();
        var resp = doRegistration(user3, "user three", "Leader");
        helper.pushButton(resp, "SignUpAlone", user3);

        resp = helper.sendMsg("/start", user).getBotResponse(user);

        var pRest = helper.pushButton(resp, "CancelAlone", user);

        var user1Resp = pRest.getBotResponse(user);
        var user2Resp = pRest.getBotResponse(user2);
        var user3Resp = pRest.getBotResponse(user3);

        assertThat(user1Resp.getMessage()).contains("Удалил тебя из списка");
        assertThat(user2Resp.getMessage()).contains("Записал к тебе в пару танцора: user3");
        assertThat(user3Resp.getMessage()).contains("Записал к тебе в пару танцора: user2");

        assertThat(sampoListRepository.inPair(1L)).isFalse();
        assertThat(sampoListRepository.inPair(2L)).isTrue();
        assertThat(sampoListRepository.inPair(3L)).isTrue();
        assertThat(sampoListRepository.isWaiting(2L)).isFalse();
        assertThat(sampoListRepository.isWaiting(3L)).isFalse();
    }


    /*
    Запись в лист ожидания

    Итог
    - записан в лист ожидания
     */
    @Test
    void signUpAlone() {
        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var resp = doRegistration(user, "user one", "Leader");

        var pResp = helper.pushButton(resp, "SignUpAlone", user);

        var user1Resp = pResp.getBotResponse(user);

        assertThat(user1Resp.getMessage()).contains("Записал тебя в лист ожидания");

        assertThat(sampoListRepository.isWaiting(user.getId())).isTrue();
    }


    /*

    Запись в ожидание при наличии противоположенного пола в листе ожидания

    Итог:
    -Произведена запись парой
    -оба получили уведомление

     */
    @Test
    void signUpAloneWithFreeDancer() {
        signUpAlone();

        var user1 = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();
        var resp = doRegistration(user2, "user two", "Follower");

        var pResp = helper.pushButton(resp, "SignUpAlone", user2);

        var user1Resp = pResp.getBotResponse(user1);
        var user2Resp = pResp.getBotResponse(user2);

        assertThat(user1Resp.getMessage()).contains("Записал к тебе в пару танцора: user2");
        assertThat(user2Resp.getMessage()).contains("Записал к тебе в пару танцора: user1");

        assertThat(sampoListRepository.inPair(user1.getId())).isTrue();
        assertThat(sampoListRepository.inPair(user2.getId())).isTrue();
        assertThat(sampoListRepository.isWaiting(user1.getId())).isFalse();
        assertThat(sampoListRepository.isWaiting(user2.getId())).isFalse();
    }


    /*
    Удаление из листа ожидания

    Итог:
    -участник удален из листа ожидания
     */
    @Test
    void cancelSignUpAlone() {
        signUpAlone();
        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var resp = helper.sendMsg("/start", user).getBotResponse(user);

        resp = helper.pushButton(resp, "CancelAlone", user).getBotResponse(user);

        assertThat(resp.getMessage()).contains("Удалил тебя из списка");

        assertThat(sampoListRepository.isWaiting(user.getId())).isFalse();

    }

    /*
    Запрос на удаление парой при отсутствии пары

    Итог:
    Сообщение об ошибке
     */
    @Test
    void cancelPairWithNoPair() {

        //не нашел как получить кнопку отмены парой если пары нет

    }


    /*

    Запрос на удаления из листа ожидания при отсутствии в листе ожидания

    Итог:
    Сообщение об ошибке

     */
    @Test
    void cancelWaitWithNoPair() {

        //не нашел как получить кнопку отмены если нет в листе

    }

    private BotResponse doRegistration(TelegramUser user, String lastName, String sex) {
        var resp = helper.sendMsg("/start", user).getBotResponse(user);
        assertThat(resp.getButtons()).containsExactlyInAnyOrder("Leader", "Follower");
        resp = helper.pushButton(resp, sex, user).getBotResponse(user);
        assertThat(resp.getButtons()).containsExactlyInAnyOrder("readmylastname", "lastnameok");
        resp = helper.pushButton(resp, "readmylastname", user).getBotResponse(user);
        return helper.sendMsg(lastName, user).getBotResponse(user);
    }

    ;

}
