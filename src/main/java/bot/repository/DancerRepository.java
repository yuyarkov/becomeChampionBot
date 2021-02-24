package bot.repository;

import bot.Dancer;

public interface DancerRepository {
    Dancer getByChatId(long id);

    void save(Dancer dancer);
}
