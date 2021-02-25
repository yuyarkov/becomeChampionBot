package bot.sampo.repository;

import bot.sampo.model.Dancer;
import bot.sampo.model.Tables;
import bot.sampo.repository.mapper.DancerRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import static bot.sampo.repository.DbResultUtils.getOneOrNull;


@Service
@RequiredArgsConstructor
public class DancerRepositoryImpl implements DancerRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Dancer getDancerById(long dancerId) {
        var query = "select * from " + Tables.DANCER_TABLE_NAME + " where  id = :id";
        var param = Map.of("id", dancerId);
        return getOneOrNull(jdbcTemplate.query(query, param, new DancerRowMapper()));
    }

    @Override
    public Dancer getByLastName(String lastName) {
        var query = "select * from " + Tables.DANCER_TABLE_NAME + " last_name = :lastName";
        var param = Map.of("lastName", lastName);
        return getOneOrNull(jdbcTemplate.query(query, param, new DancerRowMapper()));
    }

    @Override
    public void save(Dancer d) {
        var query = "insert into " + Tables.DANCER_TABLE_NAME + "(id, first_name, last_name, telegram_name, man) " +
                "values (:id, :firstName, :lastName, :telegramName, :man) " +
                "ON CONFLICT (id) DO UPDATE set " +
                "first_name=:firstName, last_name=:lastName, telegram_name=:telegramName, man=:man";

        var param = Map.of(
                "id", d.getChatID(),
                "firstName", d.getFirstName(),
                "lastName", d.getLastName(),
                "telegramName", d.getTelegramName(),
                "man", d.mapSexToDb());

        jdbcTemplate.update(query, param);
    }


}
