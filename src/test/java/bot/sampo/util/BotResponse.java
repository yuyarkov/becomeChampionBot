package bot.sampo.util;


import lombok.Data;

import java.util.Set;

@Data
public class BotResponse {
    private Set<String> buttons;
    private String message;
    private long clientId;

    public BotResponse(long clientId) {
        this.clientId = clientId;
    }
}
