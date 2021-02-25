package bot.repository.mapper;

import bot.model.PairShortDb;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PairShortRowMapper implements RowMapper<PairShortDb> {
    @Override
    public PairShortDb mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PairShortDb.builder()
                .leaderLastName(rs.getString("leader"))
                .followerLastName(rs.getString("follower"))
                .build();
    }
}
