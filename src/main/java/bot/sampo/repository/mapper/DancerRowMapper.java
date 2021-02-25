package bot.sampo.repository.mapper;

import bot.sampo.model.Dancer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DancerRowMapper implements RowMapper<Dancer> {
    @Override
    public Dancer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Dancer.builder()
                .telegramName(rs.getString("telegram_name"))
                .lastName(rs.getString("last_name"))
                .firstName(rs.getString("first_name"))
                .sex(Dancer.mapSexFromDb(rs.getObject("man", Boolean.class)))
                .chatID(rs.getLong("id"))
                .admin(rs.getBoolean("admin"))
                .build();
    }
}
