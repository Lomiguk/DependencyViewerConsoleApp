package ru.dsckibin.ui;

import ru.dsckibin.exception.ConsoleReadingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ConsoleUiManager {
    private final static String INPUT_MARKER = ">";
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public <T> T select(List<T> objs) {
        System.out.println("Select:");
        return selecting(objs);
    }

    public <T> T select(List<T> objs, String topic) {
        System.out.printf("Select %s: \n", topic);
        return selecting(objs);
    }

    private <T> T selecting(List<T> objs) {
        printNumericalList(objs);
        System.out.print(INPUT_MARKER);
        int input;
        try {
            input = Integer.parseInt(reader.readLine().trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return objs.get(input);
    }

    private <T> void printNumericalList(List<T> objs) {
        var i = 0;
        for (var obj : objs) {
            System.out.printf("%d) %s%n", i, obj);
            i++;
        }
    }

    public String getGitRepo() {
        try {
            System.out.println("Git repo not found");
            System.out.println("Write absolute path to git repository:");
            System.out.print(INPUT_MARKER);
            return reader.readLine();
        } catch (IOException e) {
            throw new ConsoleReadingException("Failed to read path to git repository");
        }

    }
}
