package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pojo.Dancer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

public class Converter {

    private final static String baseFile = "dancerBase.json";
    private static String filePathListSampo="listSampo.json";
    private static String filePathWaitingList="waitingList.json";

    public static void setFilePathListSampo(String path){
        filePathListSampo=path;
    }
    public static void setFilePathWaitingList(String path){
        filePathListSampo=path;
    }


    public static void saveDancerBaseToFile(HashSet<Dancer> dancerBase) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (OutputStream output = Files.newOutputStream(Path.of(baseFile));
             PrintStream sender = new PrintStream(output)) {
            for (var dancer : dancerBase)
                try {
                    sender.println(mapper.writeValueAsString(dancer));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    public static HashSet<Dancer> readDancerBaseFromFile() throws IOException {
        HashSet<Dancer> dancerBase = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();

        Files.lines(Paths.get(baseFile), StandardCharsets.UTF_8).forEach(s -> {
            try {
                dancerBase.add(mapper.readValue(s, Dancer.class));
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

/*
    public static ArrayList<ArrayList<Dancer>> readListSampoFromFile() throws IOException {
        ArrayList<ArrayList<Dancer>> listSampo = new ArrayList<ArrayList<Dancer>>;
        ObjectMapper mapper = new ObjectMapper();

        Files.lines(Paths.get(filePathListSampo), StandardCharsets.UTF_8).forEach(s -> {
            try {
                listSampo.add(mapper.readValue(s, Dancer.class));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return listSampo;
*/

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