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
        var query = "select * from " + Tables.DANCER_TABLE_NAME + " where last_name = :lastName";
        var param = Map.of("lastName", lastName);
        return getOneOrNull(jdbcTemplate.query(query, param, new DancerRowMapper()));
    }

    @Override
    public void save(Dancer d) {
        var query = "insert into " + Tables.DANCER_TABLE_NAME + "(id, first_name, last_name, leader, admin) " +
                "values (:id, :firstName, :lastName, :leader, :admin) " +
                "ON CONFLICT (id) DO UPDATE set " +
                "first_name=:firstName, last_name=:lastName, leader=:leader, admin=:admin";

        var param = Map.of(
                "id", d.getChatID(),
                "firstName", d.getFirstName(),
                "lastName", d.getLastName(),
                "leader", d.isLeader(),
                "admin", d.isAdmin());

        jdbcTemplate.update(query, param);
    }


}
