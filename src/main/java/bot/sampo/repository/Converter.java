package bot.sampo.repository;

import bot.sampo.model.Dancer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Converter {

    private final static String baseFile = "dancerBase.json";
    private static String filePathListSampo = "listSampo.json";
    private static String filePathWaitingList = "waitingList.json";

    public static void setFilePathListSampo(String path) {
        filePathListSampo = path;
    }

    public static void setFilePathWaitingList(String path) {
        filePathListSampo = path;
    }


    public static void saveDancerBaseToFile(Map<Long, Dancer> dancerBase) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (OutputStream output = Files.newOutputStream(Path.of(baseFile));
             PrintStream sender = new PrintStream(output)) {
            for (var dancer : dancerBase.entrySet())
                try {
                    sender.println(mapper.writeValueAsString(dancer.getValue()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    public static Map<Long, Dancer> readDancerBaseFromFile() throws IOException {
        var dancerBase = new HashMap<Long, Dancer>();
        ObjectMapper mapper = new ObjectMapper();

        Files.lines(Paths.get(baseFile), StandardCharsets.UTF_8).forEach(s -> {
            try {
                var dancer = mapper.readValue(s, Dancer.class);
                dancerBase.put(dancer.getChatID(), dancer);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return dancerBase;

    }

    public static Dancer stringToDancer(String string) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(string, Dancer.class);
    }

    public static String dancerToJSON(Dancer dancer) {
        ObjectMapper mapper = new ObjectMapper();
        String result = null;
        try {
            result = mapper.writeValueAsString(dancer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void saveListSampoToFile(ArrayList<ArrayList<Dancer>> listSampo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (OutputStream output = Files.newOutputStream(Path.of(filePathListSampo));
             PrintStream sender = new PrintStream(output)) {
            for (var pair : listSampo)
                try {
                    sender.println(mapper.writeValueAsString(pair));
                } catch (Exception e) {
                    System.out.println(e);
                }
        }
    }


    public static ArrayList<ArrayList<Dancer>> readListSampoFromFile() throws IOException {
        ArrayList<ArrayList<Dancer>> listSampo = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        Files.lines(Paths.get(filePathListSampo), StandardCharsets.UTF_8).forEach(s -> {
            try {
                ArrayList<Dancer> pair=new ArrayList<>();
                pair.add(mapper.readValue(s, Dancer.class));
                pair.add(mapper.readValue(s, Dancer.class));
                listSampo.add(pair);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return listSampo;
    }

    public static void saveWaitingListToFile(ArrayList<ArrayList<Dancer>> listSampo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (OutputStream output = Files.newOutputStream(Path.of(filePathWaitingList));
             PrintStream sender = new PrintStream(output)) {
            for (var pair : listSampo)
                try {
                    sender.println(mapper.writeValueAsString(pair));
                } catch (Exception e) {
                    System.out.println(e);
                }
        }
    }

}