package bot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dancer {
    public static final String LEADER = "Партнер";//Названия полов
    public static final String FOLLOWER = "Партнерша";

    @JsonProperty("chat_id")
    private long chatID;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("telegram_nickname")
    private String telegramName;

    @JsonProperty("sex")
    private String sex;

    @JsonIgnore
    private String idASH;//на будущее, код танцора в базе АСХ

}


