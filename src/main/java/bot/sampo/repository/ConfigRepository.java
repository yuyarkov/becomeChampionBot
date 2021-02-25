package bot.sampo.repository;

import java.time.LocalDate;

public interface ConfigRepository {
    LocalDate getCurrentSampoDate();

    void setCurrentSampoDate(LocalDate localDate);
}
