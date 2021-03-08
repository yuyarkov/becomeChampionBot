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

        var dateStr = getOneOrNull(jdbcTemplate.query(query, Map.of(), new StringRowMapper("value")));

        if (dateStr == null)
            throw new RuntimeException("sampo date not configured");

        return LocalDate.parse(dateStr);
    }

    @Override
    public void setCurrentSampoDate(LocalDate localDate) {
        var query = "insert into " + Tables.CONFIG_TABLE_NAME + "(id, value) " +
                "values (:id, :value) " +
                "ON CONFLICT (id) DO UPDATE set " +
                "value=:value";
        var param = Map.of(
                "id", "nextSampo",
                "value", localDate.toString());
        jdbcTemplate.update(query, param);
    }
}
