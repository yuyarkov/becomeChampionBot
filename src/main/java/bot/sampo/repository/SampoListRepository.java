package bot.sampo.repository;

import bot.sampo.model.Dancer;
import bot.sampo.model.Pair;

public interface SampoListRepository {

    boolean inPair(long dancer);

    void writePair(Pair pair);

    Dancer getFirstWaiting();

    String getPairListAsText();

    Pair getPair(long chatID);

    void removeFromWaiting(long freeDancer);

    boolean isWaiting(long chatID);

    void removeFromPairListAndAddToWait(long chatID);

    String getWaitingListAsText();

    void addWaiting(long dancerId);
}
