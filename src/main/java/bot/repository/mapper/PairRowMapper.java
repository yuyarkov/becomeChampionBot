package bot.repository.mapper;

import bot.model.PairDb;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PairRowMapper implements RowMapper<PairDb> {
    @Override
    public PairDb mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PairDb.builder()
                .leader(rs.getLong("leader"))
                .follower(rs.getLong("follower"))
                .build();
    }
}
