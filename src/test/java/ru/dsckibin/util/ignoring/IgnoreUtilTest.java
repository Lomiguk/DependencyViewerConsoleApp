package ru.dsckibin.util.ignoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IgnoreUtilTest {
    @TempDir
    Path directory;

    @Test
    void readsNamesAndSkipsBlankLinesAndComments() throws IOException {
        var file = directory.resolve("classes.ignore");
        Files.writeString(file, "# platform classes\njava.*\n\n  *.Generated  \n");

        assertEquals(
                java.util.List.of("java.*", "*.Generated"),
                new IgnoreUtil().getIgnoredNamesFrom(file.toString())
        );
    }

    @Test
    void reportsMissingFile() {
        assertThrows(IllegalArgumentException.class,
                () -> new IgnoreUtil().getIgnoredNamesFrom(directory.resolve("missing").toString()));
    }
}
