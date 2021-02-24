package bot.repository;

import bot.model.Dancer;

public interface DancerRepository {
    Dancer getByChatId(long id);

    Dancer getByLastName(String lastName);

    void save(Dancer dancer);
}
