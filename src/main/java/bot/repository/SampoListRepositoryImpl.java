package bot.repository;

import bot.model.Dancer;
import bot.model.Pair;
import bot.model.PairDb;
import bot.model.Tables;
import bot.repository.mapper.DancerRowMapper;
import bot.repository.mapper.PairRowMapper;
import bot.repository.mapper.PairShortRowMapper;
import lombok.RequiredArgsConstructor;
import org.jvnet.hk2.annotations.Service;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class SampoListRepositoryImpl implements SampoListRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ConfigRepository configRepository;
    private final SimpleJdbcInsert waitDanceSimpleInsert;
    private final SimpleJdbcInsert pairDanceSimpleInsert;
    private final DancerRepository dancerRepository;

    private static final String QUERY_SELECT_PAIR = "select * from " + Tables.PAIR_LIST_TABLE_NAME + " where sampo_date=:sampoDate and (dancer1=:dancerId or dancer2=:dancerId )";
    private static final String QUERY_SELECT_WAIT = "select * from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate and dancer=:dancerId";


    @Override
    public boolean inPair(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        Map<String, Object> param = Map.of("sampoDate", sampoDate, "dancerId", dancerId);
        return jdbcTemplate.queryForObject(QUERY_SELECT_PAIR, param, Object.class) != null;
    }

    @Override
    public void writePair(long dancerId1, long dancerId2) {
        var sampoDate = configRepository.getCurrentSampoDate();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("sampo_date", sampoDate);
        parameters.put("dancer1", dancerId1);
        parameters.put("dancer2", dancerId2);
        pairDanceSimpleInsert.execute(parameters);
    }

    @Override
    public Dancer getFirstWaiting() {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query = "select dancer from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate order by creation_time asc limit 1";
        Map<String, Object> param = Map.of("sampoDate", sampoDate);
        var dancerId = jdbcTemplate.queryForObject(query, param, Long.class);

        if (dancerId == null)
            return null;

        return dancerRepository.getDancerById(dancerId);
    }

    @Override
    public void removeFromWaiting(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query = "delete from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate and dancer=:dancer limit 1";
        Map<String, Object> param = Map.of("dancer", dancerId, "sampoDate", sampoDate);
        jdbcTemplate.update(query, param);
    }

    @Override
    public Pair getPair(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        Map<String, Object> param = Map.of("sampoDate", sampoDate);
        PairDb pairDb = jdbcTemplate.queryForObject(QUERY_SELECT_PAIR, param, new PairRowMapper());

        if (pairDb == null)
            return null;

        return Pair.builder()
                .leader(dancerRepository.getDancerById(pairDb.getLeader()))
                .follower(dancerRepository.getDancerById(pairDb.getFollower()))
                .build();
    }

    @Override
    public boolean isWaiting(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        Map<String, Object> param = Map.of("sampoDate", sampoDate, "dancerId", dancerId);
        return jdbcTemplate.queryForObject(QUERY_SELECT_WAIT, param, Object.class) != null;
    }

    @Override
    public void removeFromPairListAndAddToWait(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query = "delete from " + Tables.DANCER_TABLE_NAME + " where sampo_date=:sampoDate and (dancer1=:dancer or dancer2=:dancer) limit 1";
        Map<String, Object> param = Map.of("dancer", dancerId, "sampoDate", sampoDate);
        jdbcTemplate.update(query, param);

        addWaiting(dancerId);
    }

    @Override
    public String getPairListAsText() {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query =
                "select d1.last_name leader, d2.last_name follower " +
                        "from " + Tables.PAIR_LIST_TABLE_NAME + " as w " +
                        "left join " + Tables.DANCER_TABLE_NAME + " as d1 on w.dancer1=d.id " +
                        "left join " + Tables.DANCER_TABLE_NAME + " as d2 on w.dancer2=d.id " +
                        "where w.sampo_date:=sampoDate";
        Map<String, Object> param = Map.of("sampoDate", sampoDate);
        var waitedDancers = jdbcTemplate.query(query, param, new PairShortRowMapper());

        StringBuilder result = new StringBuilder();
        var index = new AtomicInteger();
        waitedDancers.forEach(v -> result.append(index.incrementAndGet() + ". " + v.getLeaderLastName() + " - " + v.getFollowerLastName() + "\n"));
        return result.toString();
    }

    @Override
    public String getWaitingListAsText() {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query =
                "select d.* " +
                        "from " + Tables.WAIT_LIST_TABLE_NAME + " as w" +
                        "left join " + Tables.DANCER_TABLE_NAME + " as d on w.dancer=d.id " +
                        "where w.sampo_date:=sampoDate";
        Map<String, Object> param = Map.of("sampoDate", sampoDate);
        var waitedDancers = jdbcTemplate.query(query, param, new DancerRowMapper());

        StringBuilder result = new StringBuilder();
        var index = new AtomicInteger();

        waitedDancers.forEach(v -> result.append(index.incrementAndGet() + ". " + v.getLastName() + "\n"));
        return result.toString();
    }

    @Override
    public void addWaiting(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        var param = Map.of("sampo_date", sampoDate, "dancer", dancerId, "creation_time", System.currentTimeMillis());
        waitDanceSimpleInsert.execute(param);
    }


}
