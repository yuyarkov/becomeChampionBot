package bot.sampo.service;

import bot.sampo.CommonContext;
import bot.sampo.repository.ConfigRepository;
import bot.sampo.repository.DancerRepository;
import bot.sampo.repository.SampoListRepository;
import bot.sampo.util.BotRequestHelper;
import bot.sampo.util.TelegramUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class BotScenarioTest extends CommonContext {

    @Autowired
    private BotRequestHelper helper;

    @Autowired
    private DancerRepository dancerRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private SampoListRepository sampoListRepository;

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

        configRepository.setCurrentSampoDate(LocalDate.now());

        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();

        var resp = helper.sendMsg("/start", user).getBotResponse(user);

        assertThat(resp.getButtons()).containsExactlyInAnyOrder("Leader", "Follower");

        resp = helper.pushButton(resp, "Leader", user).getBotResponse(user);

        assertThat(resp.getButtons()).containsExactlyInAnyOrder("readmylastname", "lastnameok");

        resp = helper.pushButton(resp, "readmylastname", user).getBotResponse(user);

        resp = helper.sendMsg("user one", user).getBotResponse(user);

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

        configRepository.setCurrentSampoDate(LocalDate.now());

        var user = TelegramUser.builder().firstName("user1").lastName(null).id(1L).build();
        var resp = helper.sendMsg("/start", user).getBotResponse(user);
        assertThat(resp.getButtons()).containsExactlyInAnyOrder("Leader", "Follower");
        resp = helper.pushButton(resp, "Leader", user).getBotResponse(user);
        assertThat(resp.getButtons()).containsExactlyInAnyOrder("readmylastname", "lastnameok");
        resp = helper.pushButton(resp, "readmylastname", user).getBotResponse(user);
        resp = helper.sendMsg("user one", user).getBotResponse(user);


        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();
        resp = helper.sendMsg("/start", user2).getBotResponse(user2);
        assertThat(resp.getButtons()).containsExactlyInAnyOrder("Leader", "Follower");
        resp = helper.pushButton(resp, "Follower", user2).getBotResponse(user2);
        assertThat(resp.getButtons()).containsExactlyInAnyOrder("readmylastname", "lastnameok");
        resp = helper.pushButton(resp, "readmylastname", user2).getBotResponse(user2);
        resp = helper.sendMsg("user two", user2).getBotResponse(user2);

        assertThat(resp.getButtons()).containsExactlyInAnyOrder("SignUpPair", "SignUpAlone", "ShowList");

        resp = helper.pushButton(resp, "SignUpPair", user2).getBotResponse(user2);
        assertThat(resp.getMessage()).contains("Скажи, с кем");
        var pResp = helper.sendMsg("user one", user2);

        var user1Resp = pResp.getBotResponse(user);
        var user2Resp = pResp.getBotResponse(user2);

        assertThat(user1Resp.getMessage()).contains("Ты записан(а)");
        assertThat(user2Resp.getMessage()).contains("Ты записан(а)");


        var pair = sampoListRepository.getPair(user1Resp.getClientId());

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

        configRepository.setCurrentSampoDate(LocalDate.now());

        var user2 = TelegramUser.builder().firstName("user2").lastName(null).id(2L).build();
        var resp = helper.sendMsg("/start", user2).getBotResponse(user2);
        assertThat(resp.getButtons()).containsExactlyInAnyOrder("Leader", "Follower");
        resp = helper.pushButton(resp, "Follower", user2).getBotResponse(user2);
        assertThat(resp.getButtons()).containsExactlyInAnyOrder("readmylastname", "lastnameok");
        resp = helper.pushButton(resp, "readmylastname", user2).getBotResponse(user2);
        resp = helper.sendMsg("user two", user2).getBotResponse(user2);

        assertThat(resp.getButtons()).containsExactlyInAnyOrder("SignUpPair", "SignUpAlone", "ShowList");

        resp = helper.pushButton(resp, "SignUpPair", user2).getBotResponse(user2);
        assertThat(resp.getMessage()).contains("Скажи, с кем");
        var pResp = helper.sendMsg("not exists", user2);

        var user2Resp = pResp.getBotResponse(user2);

        assertThat(user2Resp.getMessage()).contains("not exists — не нашёл такой");

    }


    /*
    Отмена сампо парой

    Итог:
    - Пара отменена записей в базе не осталось
    - оба участника получили инфо об этмене
     */

    /*
    Отмена сампо одним участником при пустом листе ожидания

    Итог:
    -Пара отменена
    -другой участник в листе ожидания
    -другой получает уведомление об отмене
     */

    /*
    Отмена сампо одним участником при наличии в листе ожидания противоположенного пола
    -Пара отменена
    -другой участник записан с другим партнером
    -другой получает уведомление о смене партнера

     */



    /*
    Запись в лист ожидания

    Итог
    - записан в лист ожидания
     */

        /*

    Запись в ожидание при наличии противоположенного пола в листе ожидания

    Итог:
    -Произведена запись парой

     */

        /*
    Удаление из листа ожидания

    Итог:
    -участник удален из листа ожидания
     */


    /*
    Запрос на удаление парой при отсутствии пары

    Итог:
    Сообщение об ошибке
     */

    /*

    Запрос на удаления из листа ожидания при отсутствии в листе ожидания

    Итог:
    Сообщение об ошибке

     */


}
