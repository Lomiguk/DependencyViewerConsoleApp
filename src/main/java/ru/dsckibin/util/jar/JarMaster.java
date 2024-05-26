package ru.dsckibin.util.jar;

import ru.dsckibin.util.FileSearcher;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarMaster {
    private final static Set<String> JAVA_ARCHIVE_EXTENSION = Set.of(".jar", ".war", ".zip");

    private final FileSearcher fileSearcher = new FileSearcher();

    public List<String> searchJar(String parentDirectory) {
        fileSearcher.searchFilesInDirectoryByExtensions(
                new File(parentDirectory),
                JAVA_ARCHIVE_EXTENSION
        );
        return fileSearcher.getResult();
    }

    public Map<String, byte[]> getClassesAsByteArray(String jarPath) {
        try (var jarFile = new JarFile(jarPath)){
            return getClassesAsBytes(jarFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, byte[]> getClassesAsBytes(JarFile jarFile) {
        List<JarEntry> entries = Collections.list(jarFile.entries());
        Map<String, byte[]> result = new HashMap<>();
        for (var entry : entries) {
            if (entry.getName().endsWith(".class")) {
                try (var inputStream = jarFile.getInputStream(entry)) {
                    result.put(entry.getName(), inputStream.readAllBytes());
                } catch (IOException ioException) {
                    System.out.println("Could not obtain class entry for " + entry.getName());
                    throw new RuntimeException(ioException);
                }
            }
        }
        return result;
    }
}
