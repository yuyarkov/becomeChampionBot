import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.io.IOException;
import java.util.HashSet;

public class DancerBase {
    public static HashSet<pojo.Dancer> dancerBase = new HashSet<>();
    public static String sourceDancerBase = "dancerBase.xml";

    public static pojo.Dancer emptyDancer = new pojo.Dancer("", "ПустойТанцор", pojo.Dancer.LEADER, 0, null);


    public static void addNewDancer(pojo.Dancer dancer) {
        try {
            dancerBase = util.Converter.readDancerBaseFromFile();
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
        pojo.Dancer dancerFound = emptyDancer;
        try {
            dancerBase = util.Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (var dancer : dancerBase) {
            if (dancer.getChatID() == chatID) {
                dancerFound = dancer;
            }
        }

        return dancerFound;
    }

    public static pojo.Dancer findDancerByLastName(String lastName) {
        pojo.Dancer dancerFound = emptyDancer;
        try {
            dancerBase = util.Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (var dancer : dancerBase) {
            if (dancer.getLastName().equalsIgnoreCase(lastName)) {
                dancerFound = dancer;
            }
        }
        return dancerFound;
    }

    public static boolean hasDancerWithThisLastName(String lastName) {
        for (var dancer : dancerBase) {
            if (dancer.getLastName().equalsIgnoreCase(lastName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDancerWithThisChatID(long chatID) {
        for (var dancer : dancerBase) {
            if (dancer.getChatID() == chatID) {
                return true;
            }
        }
        return false;
    }

    public static void createDancer(CallbackQuery callbackQuery) {
        String firstName = callbackQuery.getFrom().getFirstName();
        String lastName = callbackQuery.getFrom().getLastName();
        long chatID = callbackQuery.getMessage().getChatId();
        String telegramName = callbackQuery.getFrom().getUserName();
        if (isDancerWithThisChatID(chatID)) {
            pojo.Dancer findDancer = getDancerByChatID(chatID);
            findDancer.setFirstName(firstName);
            findDancer.setLastName(lastName);
            findDancer.setTelegramName(telegramName);
        } else if (hasDancerWithThisLastName(lastName)) {
            pojo.Dancer findDancer = findDancerByLastName(lastName);
            findDancer.setChatID(chatID);
            findDancer.setFirstName(firstName);
            findDancer.setTelegramName(telegramName);
        } else {
            pojo.Dancer newDancer = new pojo.Dancer(firstName, lastName, becomeChampionBot.dialogDancerSex, chatID, telegramName);
            dancerBase.add(newDancer);
        }
        try {
            util.Converter.saveDancerBaseToFile(dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
        becomeChampionBot.dialogDancerSex = null;
    }


}
