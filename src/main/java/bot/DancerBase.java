package bot;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.io.IOException;
import java.util.HashSet;

public class DancerBase {
    public static HashSet<Dancer> dancerBase = new HashSet<>();
    public static Dancer emptyDancer = new Dancer("", "ПустойТанцор", Dancer.LEADER, 0, null);


    public static Dancer getDancerByChatID(long chatID) {
        Dancer dancerFound = emptyDancer;
        try {
            dancerBase = Converter.readDancerBaseFromFile();
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

    public static Dancer findDancerByLastName(String lastName) {
        Dancer dancerFound = emptyDancer;
        try {
            dancerBase = Converter.readDancerBaseFromFile();
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
        try {
            dancerBase = Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (var dancer : dancerBase) {
            if (dancer.getLastName().equalsIgnoreCase(lastName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDancerWithThisChatID(long chatID) {
        try {
            dancerBase = Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        try {
            dancerBase = Converter.readDancerBaseFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isDancerWithThisChatID(chatID)) {
            Dancer findDancer = getDancerByChatID(chatID);
            findDancer.setFirstName(firstName);
            findDancer.setLastName(lastName);
            findDancer.setTelegramName(telegramName);
        } else if (hasDancerWithThisLastName(lastName)) {
            Dancer findDancer = findDancerByLastName(lastName);
            findDancer.setChatID(chatID);
            findDancer.setFirstName(firstName);
            findDancer.setTelegramName(telegramName);
        } else {
            Dancer newDancer = new Dancer(firstName, lastName, Application.dialogDancerSex.get(chatID), chatID, telegramName);
            dancerBase.add(newDancer);
        }
        try {
            Converter.saveDancerBaseToFile(dancerBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
