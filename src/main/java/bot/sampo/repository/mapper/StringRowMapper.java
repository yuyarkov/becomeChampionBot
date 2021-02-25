package bot.sampo.repository.mapper;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class StringRowMapper implements RowMapper<String> {

    private final String columnName;

    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(columnName);
    }
}
