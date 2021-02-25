package bot.sampo.repository.mapper;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class LongRowMapper implements RowMapper<Long> {

    private final String columnName;

    @Override
    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong(columnName);
    }
}
