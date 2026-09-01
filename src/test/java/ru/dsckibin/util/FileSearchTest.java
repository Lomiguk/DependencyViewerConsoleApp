package ru.dsckibin.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSearchTest {
    @TempDir
    Path directory;

    @Test
    void returnsSortedMatchesAndDoesNotLeakPreviousResults() throws IOException {
        var nested = Files.createDirectory(directory.resolve("nested"));
        Files.createFile(directory.resolve("b.jar"));
        Files.createFile(nested.resolve("a.JAR"));
        Files.createFile(directory.resolve("notes.txt"));

        var search = new FileSearch();
        var first = search.searchFilesInDirectoryByExtensions(directory.toFile(), Set.of(".jar"));
        var second = search.searchFilesInDirectoryByExtensions(nested.toFile(), Set.of(".jar"));

        assertEquals(2, first.size());
        assertEquals(1, second.size());
        assertEquals(nested.resolve("a.JAR").toAbsolutePath().toString(), second.get(0));
    }
}
