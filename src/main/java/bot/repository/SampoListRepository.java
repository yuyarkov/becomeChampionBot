package bot.repository;

import bot.model.Dancer;
import bot.model.Pair;

public interface SampoListRepository {

    boolean inPair(long dancer);

    void writePair(long dancerId1, long dancerId2);

    Dancer getFirstWaiting();

    String getPairListAsText();

    Pair getPair(long chatID);

    void removeFromWaiting(long freeDancer);

    boolean isWaiting(long chatID);

    void removeFromPairListAndAddToWait(long chatID);

    String getWaitingListAsText();

    void addWaiting(long dancerId);
}
