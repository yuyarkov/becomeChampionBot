package bot.repository;

import bot.model.Dancer;
import bot.model.Pair;

public interface SampoListRepository {

    boolean isSigned(long dancer);

    void sign(Dancer dancer1, Dancer dancer2);

    Dancer getFirstWaiting();

    void removeFromWaiting(long freeDancer);

    Pair getPair(long chatID);

    boolean isWaiting(long chatID);

    void removeFromMainListAndAddToWait(long chatID);

    String getMainListAsText();

    String getWaitingListAsText();

    void addWaiting(long dancerId);
}
