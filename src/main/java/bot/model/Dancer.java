package bot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dancer {
    public static final String LEADER = "Партнер";//Названия полов
    public static final String FOLLOWER = "Партнерша";
    private long chatID;
    private String firstName;
    private String lastName;
    private String telegramName;
    private String sex;
    private String idASH;//на будущее, код танцора в базе АСХ

    public static String mapSexFromDb(Boolean inMan) {
        if (inMan == null)
            return null;
        else if (inMan)
            return LEADER;
        else
            return FOLLOWER;
    }
}


