package bot.sampo.repository;

import java.util.List;

public class DbResultUtils {

    public static <T> T getOneOrNull(List<T> list) {
        if (list == null || list.isEmpty())
            return null;
        else if (list.size() > 1) {
            throw new RuntimeException("unexpected result, only one object allowed " + list);
        } else {
            return list.get(0);
        }
    }
}
