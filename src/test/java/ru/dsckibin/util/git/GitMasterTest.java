package ru.dsckibin.util.git;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitMasterTest {
    @TempDir
    Path directory;

    @Test
    void readsHistoryAndDiffWithoutPlatformSpecificPaths() throws Exception {
        String firstHash;
        String secondHash;
        try (var git = Git.init().setDirectory(directory.toFile()).call()) {
            var source = directory.resolve("Example.java");
            Files.writeString(source, "class Example {}\n");
            git.add().addFilepattern("Example.java").call();
            firstHash = git.commit()
                    .setAuthor("Test", "test@example.com")
                    .setMessage("first")
                    .call().getName();

            Files.writeString(source, "class Example { int value; }\n");
            git.add().addFilepattern("Example.java").call();
            secondHash = git.commit()
                    .setAuthor("Test", "test@example.com")
                    .setMessage("second")
                    .call().getName();
        }

        var master = new GitMaster(directory.toString());
        var branches = master.getBranches();
        var branch = branches.stream().filter(name -> name.startsWith("refs/heads/")).findFirst().orElseThrow();

        assertFalse(master.getCommits(branch).isEmpty());
        assertEquals(java.util.List.of("Example.java"), master.getDiff(branch, firstHash, secondHash));
        assertTrue(branch.contains("master") || branch.contains("main"));
    }
}
