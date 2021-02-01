import java.io.IOException;
import java.util.HashSet;

public class DancerBase {
    public static HashSet<pojo.Dancer> dancerBase = new HashSet<>();
    public static String sourceDancerBase = "dancerBase.xml";

    public static pojo.Dancer emptyDancer = new pojo.Dancer("", "пусто-нет в базе", pojo.Dancer.LEADER, 0, null);


    public static void addNewDancer (pojo.Dancer dancer){
        try {
            dancerBase= util.Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dancerBase.add(dancer);
        try {
            util.Converter.saveDancerBaseToFile(dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static pojo.Dancer getDancerByChatID(long chatID) {
        pojo.Dancer dancerFound=emptyDancer;
        try {
            dancerBase= util.Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (var dancer : dancerBase) {
            if (dancer.getChatID()==chatID) {
                dancerFound=dancer;
            }
        }

        return dancerFound;
    }

    public static pojo.Dancer findDancerByLastName(String lastName) {
        pojo.Dancer dancerFound=emptyDancer;
        try {
            dancerBase= util.Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (var dancer : dancerBase) {
            if (dancer.getLastName().equalsIgnoreCase(lastName)) {
                dancerFound=dancer;
                System.out.println("Нашёл совпадение по фамилии: "+dancer.getFirstName()+" "+dancer.getLastName());
            }
        }return dancerFound;
    }

}
