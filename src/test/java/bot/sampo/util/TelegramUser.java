package bot.sampo.util;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class TelegramUser {
    private final long id;
    private final String firstName;
    private final String lastName;
}
