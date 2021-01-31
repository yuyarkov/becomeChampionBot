import java.time.LocalDate;
import java.util.ArrayList;
import pojo.Dancer;

public class ListSampo {
    private ArrayList<ArrayList<Dancer>> mainListSampo;
    private LocalDate sampoDate;
    private Dancer emptyFollower = new Dancer("", "в поиске", Dancer.FOLLOWER, becomeChampionBot.CHAT_ID_DETOCHKIN,"emptyFollower");
    private Dancer emptyLeader = new Dancer("", "в поиске", Dancer.LEADER, becomeChampionBot.CHAT_ID_DETOCHKIN,"emptyLeader");

    public ListSampo(LocalDate sampoDate) {//конструктор с одной датой на входе
        this.sampoDate = sampoDate;
        mainListSampo = new ArrayList<>();
    }

    public ArrayList<ArrayList<Dancer>> getMainListSampo() {
        return mainListSampo;
    }

    public LocalDate getSampoDate() {
        return sampoDate;
    }

    public void addPairToList(Dancer dancer1, Dancer dancer2) {
        ArrayList<Dancer> pair = new ArrayList<>();
        pair.add(dancer1);
        pair.add(dancer2);
        mainListSampo.add(pair);

    }

    public boolean addDancerToList(Dancer dancer) {
        boolean alreadySigned = false;
        for (int i = 0; i < mainListSampo.size(); i++) {
            if (mainListSampo.get(i).contains(dancer)) {
                alreadySigned = true;
            }
        }
        if (!alreadySigned) {
            ArrayList<Dancer> pair = new ArrayList<>();
            if (dancer.getSex().equals(Dancer.FOLLOWER)) {
                pair.add(emptyLeader);
                pair.add(dancer);
                mainListSampo.add(pair);
                return true;
            } else {
                pair.add(dancer);
                pair.add(emptyFollower);
                mainListSampo.add(pair);
                return true;
            }
        }
        return false;//если уже был записан ранее
    }


    public void removePairFromList(Dancer dancer) {
        for (int i = 0; i < mainListSampo.size(); i++) {
            var pair = mainListSampo.get(i);
            if (pair.contains(dancer)) {
                mainListSampo.remove(pair);
            }
        }
    }

    public void removeDancerFromList(Dancer dancer) {
        for (var i = 0; i < mainListSampo.size(); i++) {
            var pair = mainListSampo.get(i);
            if (pair.contains(dancer)) {
                if (dancer.getSex().equals(Dancer.LEADER)) {
                    pair.set(0, emptyLeader);
                } else {
                    pair.set(1, emptyFollower);
                }
            }
            if (pair.contains(emptyLeader) && pair.contains(emptyFollower)) {
                mainListSampo.remove(i);
            }
        }
    }

    public void printListSampo() {
        for (int i = 0; i < mainListSampo.size(); i++) {
            System.out.println((i + 1) + ": " + mainListSampo.get(i).get(0).getLastName() + " - " + mainListSampo.get(i).get(1).getLastName());
        }
    }

    public String printToString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mainListSampo.size(); i++) {
            result.append((i + 1) + " " + mainListSampo.get(i).get(0).getLastName() + " - " + mainListSampo.get(i).get(1).getLastName() + "\n");
        }
        return result.toString();
    }


}
