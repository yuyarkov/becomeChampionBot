package bot.sampo.service;

import bot.sampo.CommonContext;
import bot.sampo.model.Dancer;
import bot.sampo.repository.ConfigRepository;
import bot.sampo.repository.DancerRepository;
import bot.sampo.util.BotRequestHelper;
import bot.sampo.util.TelegramUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

class AdminRequestServiceTest extends CommonContext {


    @Autowired
    private DancerRepository dancerRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private BotRequestHelper helper;

    @Test
    void setSampoDate() {

        dancerRepository.save(Dancer.builder().leader(true).admin(true).chatID(1).firstName("admin").lastName("admin").build());

        var user = TelegramUser.builder().firstName("admin").lastName("admin").id(1).build();

        helper.sendMsg("#sc2021-02-28", user);

        Assertions.assertThat(configRepository.getCurrentSampoDate()).isEqualTo(LocalDate.of(2021, 2, 28));
    }

}