import pojo.Dancer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

public class WaitingListSampo {

    private ArrayList<ArrayList<pojo.Dancer>> waitingList;
    private LocalDate sampoDate;
    private String filePathWaitingList;

    private pojo.Dancer emptyFollower = new pojo.Dancer("", "в поиске", pojo.Dancer.FOLLOWER, becomeChampionBot.CHAT_ID_DETOCHKIN, "emptyFollower");
    private pojo.Dancer emptyLeader = new pojo.Dancer("", "в поиске", pojo.Dancer.LEADER, becomeChampionBot.CHAT_ID_DETOCHKIN, "emptyLeader");


    public WaitingListSampo(LocalDate sampoDate) {//конструктор с одной датой на входе
        this.sampoDate = sampoDate;
        waitingList = new ArrayList<ArrayList<pojo.Dancer>>();
        filePathWaitingList = "waitingList" + sampoDate.toString();
    }


    public boolean addToWaitingList(pojo.Dancer dancer) {
        boolean alreadySigned = false;
        for (int i = 0; i < waitingList.size(); i++) {
            if (waitingList.get(i).contains(dancer)) {
                alreadySigned = true;
            }
        }
        if (!alreadySigned) {
            ArrayList<pojo.Dancer> pair = new ArrayList<>();
            pair.add(emptyLeader);
            pair.add(dancer);
            waitingList.add(pair);
            try {
                util.Converter.saveWaitingListToFile(waitingList);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;//если уже была записан ранее
    }

    public void removeFromWaitingList(Dancer dancer){
        for (var pair:waitingList){
            if (pair.contains(dancer)) {waitingList.remove(pair);}
        }
        try {
            util.Converter.saveWaitingListToFile(waitingList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasDancersInWaitingList() {
        if (waitingList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    public Dancer removeFirstFromWaitingList() {
        pojo.Dancer followerToRemove = waitingList.get(0).get(1);
        waitingList.remove(0);
        try {
            util.Converter.saveWaitingListToFile(waitingList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return followerToRemove;
    }


    public void printWaitingList() {
        for (int i = 0; i < waitingList.size(); i++) {
            System.out.println((i + 1) + ": " + waitingList.get(i).get(1).getLastName());
        }
    }


    public String printToString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < waitingList.size(); i++) {
            result.append((i + 1) + ". " + waitingList.get(i).get(1).getLastName() + "\n");
        }
        return result.toString();
    }


    public boolean isAlredySigned(long chatID) {
        boolean result = false;
        for (int i = 0; i < waitingList.size(); i++) {
            if (waitingList.get(i).get(0).getChatID()==chatID || waitingList.get(i).get(1).getChatID()==chatID) {
                result = true;
            }
        }
        return result;
    }
}
