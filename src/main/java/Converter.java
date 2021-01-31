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


    public static void saveDancerBaseToFile(HashSet<Dancer> dancerBase) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (OutputStream output = Files.newOutputStream(Path.of(baseFile));
             PrintStream sender = new PrintStream(output)) {
            for (var dancer : dancerBase)
                try {
                    sender.println(mapper.writeValueAsString(dancer));
                } catch (Exception e) {
                    System.out.println(e);
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

}