package bot.sampo.repository;

import bot.sampo.model.Dancer;
import bot.sampo.model.Pair;
import bot.sampo.model.PairDb;
import bot.sampo.model.Tables;
import bot.sampo.repository.mapper.DancerRowMapper;
import bot.sampo.repository.mapper.LongRowMapper;
import bot.sampo.repository.mapper.PairRowMapper;
import bot.sampo.repository.mapper.PairShortRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static bot.sampo.repository.DbResultUtils.getOneOrNull;

@Service
@RequiredArgsConstructor
public class SampoListRepositoryImpl implements SampoListRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ConfigRepository configRepository;
    private final SimpleJdbcInsert waitDanceSimpleInsert;
    private final SimpleJdbcInsert pairDanceSimpleInsert;
    private final DancerRepository dancerRepository;

    private static final String QUERY_SELECT_PAIR = "select * from " + Tables.PAIR_LIST_TABLE_NAME + " where sampo_date=:sampoDate and (leader=:dancerId or follower=:dancerId )";
    private static final String QUERY_SELECT_WAIT = "select * from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate and dancer=:dancerId";


    @Override
    public boolean inPair(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        Map<String, Object> param = Map.of("sampoDate", sampoDate, "dancerId", dancerId);
        return jdbcTemplate.queryForObject("select exists(" + QUERY_SELECT_PAIR + ")", param, Boolean.class);
    }

    @Override
    public void writePair(Pair pair) {

        if (pair.getLeader().isLeader() == pair.getFollower().isLeader())
            throw new RuntimeException("members of pair must have different roles");

        var sampoDate = configRepository.getCurrentSampoDate();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("sampo_date", sampoDate);
        parameters.put("leader", pair.getLeader().getChatID());
        parameters.put("follower", pair.getFollower().getChatID());
        pairDanceSimpleInsert.execute(parameters);
    }

    @Override
    public Dancer getFirstWaiting() {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query = "select dancer from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate order by creation_time asc limit 1";
        Map<String, Object> param = Map.of("sampoDate", sampoDate);
        var dancerId = getOneOrNull(jdbcTemplate.query(query, param, new LongRowMapper("dancer")));

        if (dancerId == null)
            return null;

        return dancerRepository.getDancerById(dancerId);
    }

    @Override
    public void removeFromWaiting(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query = "delete from " + Tables.WAIT_LIST_TABLE_NAME + " where sampo_date=:sampoDate and dancer=:dancer";
        Map<String, Object> param = Map.of("dancer", dancerId, "sampoDate", sampoDate);
        jdbcTemplate.update(query, param);
    }

    @Override
    public Pair getPair(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        Map<String, Object> param = Map.of("sampoDate", sampoDate, "dancerId", dancerId);
        PairDb pairDb = getOneOrNull(jdbcTemplate.query(QUERY_SELECT_PAIR, param, new PairRowMapper()));

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
        return jdbcTemplate.queryForObject("select exists(" + QUERY_SELECT_WAIT + ")", param, Boolean.class);
    }

    @Override
    public void removeFromPairList(long dancerId) {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query = "delete from " + Tables.PAIR_LIST_TABLE_NAME + " where sampo_date=:sampoDate and (leader=:dancer or follower=:dancer)";
        Map<String, Object> param = Map.of("dancer", dancerId, "sampoDate", sampoDate);
        jdbcTemplate.update(query, param);
    }

    @Override
    public String getPairListAsText() {
        var sampoDate = configRepository.getCurrentSampoDate();
        var query =
                "select d1.last_name leader, d2.last_name follower " +
                        "from " + Tables.PAIR_LIST_TABLE_NAME + " as w " +
                        "left join " + Tables.DANCER_TABLE_NAME + " as d1 on w.leader=d1.id " +
                        "left join " + Tables.DANCER_TABLE_NAME + " as d2 on w.follower=d2.id " +
                        "where w.sampo_date=:sampoDate";
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
                        "from " + Tables.WAIT_LIST_TABLE_NAME + " as w " +
                        "left join " + Tables.DANCER_TABLE_NAME + " as d on w.dancer=d.id " +
                        "where w.sampo_date=:sampoDate";
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
