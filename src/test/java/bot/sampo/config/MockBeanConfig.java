package bot.sampo.config;

import bot.sampo.service.BecomeChampionBotService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class MockBeanConfig {

    @MockBean
    private BecomeChampionBotService becomeChampionBotService;
}
