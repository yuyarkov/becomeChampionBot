package bot.sampo.repository;

import bot.sampo.model.Dancer;

public interface DancerRepository {
    Dancer getDancerById(long id);

    Dancer getByLastName(String lastName);

    void save(Dancer dancer);
}
