import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import pojo.Dancer;

public class ListSampo {
    private ArrayList<ArrayList<Dancer>> mainListSampo;


    private LocalDate sampoDate;
    public String filePathListSampo;
    private Dancer emptyFollower = new Dancer("", "в поиске", Dancer.FOLLOWER, 0, "emptyFollower");
    private Dancer emptyLeader = new Dancer("", "в поиске", Dancer.LEADER, becomeChampionBot.CHAT_ID_DETOCHKIN, "emptyLeader");

    public ListSampo(LocalDate sampoDate) {//конструктор с одной датой на входе
        this.sampoDate = sampoDate;
        mainListSampo = new ArrayList<ArrayList<Dancer>>();
        filePathListSampo = "mainList" + sampoDate.toString();
    }

    public String getFilePathListSampo() {
        return filePathListSampo;
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
        try {
            util.Converter.saveListSampoToFile(mainListSampo);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            } else {
                pair.add(dancer);
                pair.add(emptyFollower);
                mainListSampo.add(pair);
            }
            try {
                util.Converter.saveListSampoToFile(mainListSampo);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;//если уже был записан ранее
    }


    public void removePairFromList(Dancer firstDancerToRemove, Dancer secondDancerToRemove) {
        for (int i = 0; i < mainListSampo.size(); i++) {
            var pair = mainListSampo.get(i);
  if (pair.contains(firstDancerToRemove)){
                mainListSampo.remove(pair);
            }
            try {
                util.Converter.saveListSampoToFile(mainListSampo);
            } catch (IOException e) {
                e.printStackTrace();
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
            if (pair.contains(emptyLeader) & pair.contains(emptyFollower)) {
                mainListSampo.remove(i);
                i--;//уменьшаю счётчик, чтобы не забыть пройти лист до конца
            }
        }
        try {
            util.Converter.saveListSampoToFile(mainListSampo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Dancer findPairByDancer(Dancer firstDancer) {

        for (ArrayList<Dancer> pair : mainListSampo) {
            if (pair.get(0).equals(firstDancer)) {
                return pair.get(1);
            }
            if (pair.get(1).equals(firstDancer)){
                return pair.get(0);
            }
        }
        return DancerBase.emptyDancer;
    }

    public void printListSampo() {
        for (int i = 0; i < mainListSampo.size(); i++) {
            System.out.println((i + 1) + ": " + mainListSampo.get(i).get(0).getLastName() + " - " + mainListSampo.get(i).get(1).getLastName());
        }
    }

    public String printToString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mainListSampo.size(); i++) {
            result.append((i + 1) + ". " + mainListSampo.get(i).get(0).getLastName() + " — " + mainListSampo.get(i).get(1).getLastName() + "\n");
        }
        return result.toString();
    }


}
