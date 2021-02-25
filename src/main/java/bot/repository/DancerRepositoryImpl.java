package bot.repository;

import bot.model.Dancer;
import bot.model.Tables;
import bot.repository.mapper.DancerRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class DancerRepositoryImpl implements DancerRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Dancer getDancerById(long dancerId) {
        var query = "select * from " + Tables.DANCER_TABLE_NAME + " where  id = :id";
        var param = Map.of("id", dancerId);
        return jdbcTemplate.queryForObject(query, param, new DancerRowMapper());
    }

    @Override
    public Dancer getByLastName(String lastName) {
        var query = "select * from " + Tables.DANCER_TABLE_NAME + " last_name = :lastName";
        var param = Map.of("lastName", lastName);
        return jdbcTemplate.queryForObject(query, param, new DancerRowMapper());
    }

    @Override
    public void save(Dancer d) {
        var query = "insert into " + Tables.DANCER_TABLE_NAME + "(id, first_name, last_name, telegram_nickname, sex) " +
                "values (:id, :firstName, :lastName, :telegramNickname, :sex) " +
                "ON CONFLICT (id) DO UPDATE set " +
                "first_name=:firstName, last_name=:lastName, telegram_nickname=:telegramNickname, sex=:sex";

        var param = Map.of(
                "id", d.getChatID(),
                "firstName", d.getFirstName(),
                "lastName", d.getLastName(),
                "telegramNickname", d.getTelegramName(),
                "sex", d.getSex());

        jdbcTemplate.update(query, param);
    }

}
