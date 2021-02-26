package bot.sampo.util;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class BotPluralResponse {
    private Map<Long, BotResponse> responseMap;

    public Set<String> getButtons(TelegramUser u) {
        return getBotResponse(u).getButtons();
    }

    public String getMessage(TelegramUser u) {
        return getBotResponse(u).getMessage();
    }

    public BotResponse getBotResponse(TelegramUser u) {
        var v = responseMap.get(u.getId());
        if (v == null)
            throw new RuntimeException("No response for this user " + u);

        return v;
    }
}
