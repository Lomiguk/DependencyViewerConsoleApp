package ru.dsckibin.util.ignoring;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IgnoreUtil {
    public List<String> getIgnoredNamesFrom(String ignoreFileName) {
        var path = Path.of(ignoreFileName);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("ignore file does not exist: " + path);
        }

        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to read ignore file: " + path, e);
        }
    }
}
