package ru.dsckibin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainCliOptionsTest {
    @Test
    void parsesOptionsAndPaths() {
        var options = Main.CliOptions.parse(new String[]{
                "--git-diff", "--ignore", "classes.ignore", "--simplify-names", "repo", "app.jar"
        });

        assertEquals("repo", options.repository());
        assertEquals("app.jar", options.archive());
        assertEquals("classes.ignore", options.ignoreFile());
        assertTrue(options.useGitDiff());
        assertTrue(options.simplifyNames());
        assertFalse(options.help());
    }

    @Test
    void supportsInteractiveDefaults() {
        var options = Main.CliOptions.parse(new String[0]);

        assertNull(options.repository());
        assertNull(options.archive());
        assertNull(options.ignoreFile());
        assertFalse(options.useGitDiff());
    }

    @Test
    void rejectsUnknownOptionsAndMissingValues() {
        assertThrows(IllegalArgumentException.class,
                () -> Main.CliOptions.parse(new String[]{"--unknown"}));
        assertThrows(IllegalArgumentException.class,
                () -> Main.CliOptions.parse(new String[]{"--ignore"}));
    }
}
