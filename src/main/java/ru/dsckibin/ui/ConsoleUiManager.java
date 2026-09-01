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
        if (objs.isEmpty()) {
            throw new IllegalArgumentException("nothing to select");
        }
        printNumericalList(objs);
        while (true) {
            System.out.print(INPUT_MARKER);
            try {
                var line = reader.readLine();
                if (line == null) {
                    throw new ConsoleReadingException("Console input was closed");
                }
                int input = Integer.parseInt(line.trim());
                if (input >= 0 && input < objs.size()) {
                    return objs.get(input);
                }
            } catch (NumberFormatException ignored) {
                // The message below is enough for interactive use.
            } catch (IOException e) {
                throw new ConsoleReadingException("Failed to read selection");
            }
            System.out.printf("Enter a number from 0 to %d.%n", objs.size() - 1);
        }
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
            var path = reader.readLine();
            if (path == null) {
                throw new ConsoleReadingException("Console input was closed");
            }
            return path;
        } catch (IOException e) {
            throw new ConsoleReadingException("Failed to read path to git repository");
        }

    }
}
