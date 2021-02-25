package bot.sampo.service;

import bot.sampo.CommonContext;
import bot.sampo.repository.ConfigRepository;
import bot.sampo.repository.DancerRepository;
import bot.sampo.util.BotRequestHelper;
import bot.sampo.util.TelegramUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class BotScenarioTest extends CommonContext {

    @Autowired
    private BotRequestHelper helper;

    @Autowired
    private DancerRepository dancerRepository;

    @Autowired
    private ConfigRepository configRepository;

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

        var resp = helper.sendMsg("/start", user);

        Assertions.assertThat(resp.getButtons()).containsExactlyInAnyOrder("Leader", "Follower");

        resp = helper.pushButton(resp, "Leader", user);

        Assertions.assertThat(resp.getButtons()).containsExactlyInAnyOrder("readmylastname", "lastnameok");

        resp = helper.pushButton(resp, "readmylastname", user);

        resp = helper.sendMsg("user one", user);

        Assertions.assertThat(resp.getButtons()).containsExactlyInAnyOrder("ShowList", "SignUpPair", "SignUpAlone");

        resp = helper.pushButton(resp, "ShowList", user);

        var newDancer = dancerRepository.getDancerById(1L);

        Assertions.assertThat(newDancer.getFirstName()).isEqualTo("user1");
        Assertions.assertThat(newDancer.getLastName()).isEqualTo("user one");
    }

}
