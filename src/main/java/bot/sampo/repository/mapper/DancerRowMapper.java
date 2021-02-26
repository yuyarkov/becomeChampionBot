package bot.sampo.repository.mapper;

import bot.sampo.model.Dancer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DancerRowMapper implements RowMapper<Dancer> {
    @Override
    public Dancer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Dancer.builder()
                .lastName(rs.getString("last_name"))
                .firstName(rs.getString("first_name"))
                .leader(rs.getBoolean("leader"))
                .chatID(rs.getLong("id"))
                .admin(rs.getBoolean("admin"))
                .build();
    }
}
