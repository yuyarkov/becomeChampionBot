package bot.repository;

import bot.model.Dancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Service
public class DancerRepositoryImpl implements DancerRepository {

    private Map<Long, Dancer> dancerBase = new HashMap<>();
    private Dancer emptyDancer = new Dancer("", "ПустойТанцор", Dancer.LEADER, 0, null);
    private static final Logger log = LoggerFactory.getLogger(DancerRepositoryImpl.class);

    @Override
    public Dancer getByChatId(long id) {
        return null;
    }

    @Override
    public Dancer getByLastName(String lastName) {
        return null;
    }

    @Override
    public void save(Dancer dancer) {

        try {
            dancerBase = Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            log.error("read db", e);
        }

        dancerBase.put(dancer.getChatID(), dancer);

        try {
            Converter.saveDancerBaseToFile(dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
