package bot.repository;

import java.time.LocalDate;

public interface ConfigRepository {
    LocalDate getNextSampoDate();
}
