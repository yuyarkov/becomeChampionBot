package bot.sampo.repository;

import bot.sampo.model.Tables;
import bot.sampo.repository.mapper.StringRowMapper;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

import static bot.sampo.repository.DbResultUtils.getOneOrNull;

@Service
@AllArgsConstructor
public class ConfigRepositoryImpl implements ConfigRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public LocalDate getCurrentSampoDate() {
        var query = "select value from " + Tables.CONFIG_TABLE_NAME + " where  id = 'nextSampo'";

        var epochDays = getOneOrNull(jdbcTemplate.query(query, Map.of(), new StringRowMapper("value")));

        if (epochDays == null)
            throw new RuntimeException("sampo date not configured");

        return LocalDate.ofEpochDay(Long.parseLong(epochDays));
    }

    @Override
    public void setCurrentSampoDate(LocalDate localDate) {
        var query = "insert into " + Tables.CONFIG_TABLE_NAME + "(id, value) " +
                "values (:id, :value) " +
                "ON CONFLICT (id) DO UPDATE set " +
                "value=:value";
        var param = Map.of(
                "id", "nextSampo",
                "value", localDate.toEpochDay());
        jdbcTemplate.update(query, param);
    }
}
