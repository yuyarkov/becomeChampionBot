package bot.sampo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dancer {
    private long chatID;
    private String firstName;
    private String lastName;
    private boolean leader;
    private boolean admin;
    private String idASH;//на будущее, код танцора в базе АСХ

}


