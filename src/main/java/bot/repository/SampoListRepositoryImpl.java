package bot.repository;

import bot.model.Dancer;
import bot.model.Pair;
import bot.model.PairDb;
import bot.model.Tables;
import lombok.RequiredArgsConstructor;
import org.jvnet.hk2.annotations.Service;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SampoListRepositoryImpl implements SampoListRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ConfigRepository configRepository;
    private final SimpleJdbcInsert waitDanceSimpleInsert;
    private final SimpleJdbcInsert signedDanceSimpleInsert;

    private static final String QUERY_SELECT_PAIR = "select * from " + Tables.SIGNED_LIST_TABLE_NAME + " where sampo_date=:sampoDate and (dancer1=:dancerId or dancer2=:dancerId )";
    private static final String QUERY_SELECT_WAIT = "select * from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate and dancer=:dancerId";


    @Override
    public boolean isSigned(long dancerId) {
        var sampoDate = configRepository.getNextSampoDate();
        Map<String, Object> param = Map.of("sampoDate", sampoDate, "dancerId", dancerId);
        return jdbcTemplate.queryForObject(QUERY_SELECT_PAIR, param, Object.class) != null;
    }

    @Override
    public void sign(Dancer dancer1, Dancer dancer2) {
        var sampoDate = configRepository.getNextSampoDate();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("sampo_date", sampoDate);
        parameters.put("dancer1", dancer1.getChatID());
        parameters.put("dancer2", dancer1.getChatID());
        signedDanceSimpleInsert.execute(parameters);
    }

    @Override
    public Dancer getFirstWaiting() {
        var sampoDate = configRepository.getNextSampoDate();
        var query = "select dancer from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate order by creation_time asc limit 1";
        Map<String, Object> param = Map.of("sampoDate", sampoDate);
        var dancerId = jdbcTemplate.queryForObject(query, param, Long.class);

        if (dancerId == null)
            return null;

        return getDancerById(dancerId);
    }

    @Override
    public void removeFromWaiting(long freeDancer) {
        var sampoDate = configRepository.getNextSampoDate();
        var query = "delete from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate and dancer=:dancer limit 1";
        Map<String, Object> param = Map.of("dancer", freeDancer, "sampoDate", sampoDate);
        jdbcTemplate.update(query, param);
    }

    @Override
    public Pair getPair(long chatID) {
        var sampoDate = configRepository.getNextSampoDate();
        Map<String, Object> param = Map.of("sampoDate", sampoDate);
        PairDb pairDb = jdbcTemplate.queryForObject(QUERY_SELECT_PAIR, param, new PairRowMapper());

        if (pairDb == null)
            return null;

        return Pair.builder()
                .leader(getDancerById(pairDb.getLeader()))
                .follower(getDancerById(pairDb.getFollower()))
                .build();
    }

    @Override
    public boolean isWaiting(long dancerId) {
        var sampoDate = configRepository.getNextSampoDate();
        Map<String, Object> param = Map.of("sampoDate", sampoDate, "dancerId", dancerId);
        return jdbcTemplate.queryForObject(QUERY_SELECT_WAIT, param, Object.class) != null;
    }

    @Override
    public void removeFromMainListAndAddToWait(long dancerId) {
        var sampoDate = configRepository.getNextSampoDate();
        var query = "delete from " + Tables.DANCER_TABLE_NAME + " where sampo_date=:sampoDate and (dancer1=:dancer or dancer2=:dancer) limit 1";
        Map<String, Object> param = Map.of("dancer", dancerId, "sampoDate", sampoDate);
        jdbcTemplate.update(query, param);

        addWaiting(dancerId);
    }

    @Override
    public String getMainListAsText() {
        return null;
    }

    @Override
    public String getWaitingListAsText() {
        return null;
    }

    @Override
    public void addWaiting(long dancerId) {
        var sampoDate = configRepository.getNextSampoDate();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("sampo_date", sampoDate);
        parameters.put("dancer", dancerId);
        waitDanceSimpleInsert.execute(parameters);
    }

    private Dancer getDancerById(long dancerId) {
        var query = "select * from " + Tables.DANCER_TABLE_NAME + " where  id = :id";
        var param = Map.of("id", dancerId);
        return jdbcTemplate.queryForObject(query, param, new DancerRowMapper());
    }
}
