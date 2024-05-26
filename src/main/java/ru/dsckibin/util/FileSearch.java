package ru.dsckibin.util;

import ru.dsckibin.exception.NotDirectoryException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class FileSearch {
    private static final Logger LOGGER = Logger.getLogger(FileSearch.class.getName());
    private final List<String> result = new ArrayList<>();
    public List<String> getResult() {
        return result;
    }

    public void searchFilesInDirectoryByExtensions(File file, Set<String> extensions) {
        if (file.isDirectory()) {
            searchByExtensions(file, extensions);
        } else {
            LOGGER.info(String.format("Try search file in file : %s", file.getName()));
            throw new NotDirectoryException(file.getName());
        }
    }

    private void searchByExtensions(File file, Set<String> extensions) {
        if (file.isDirectory() && file.canRead()) {
            var childFiles = file.listFiles();
            if (childFiles == null) return;
            for (File f : childFiles) {
                if (f.isDirectory()) {
                    searchByExtensions(f, extensions);
                } else {
                    if (checkExtension(f.getName().toLowerCase(), extensions)) {
                        result.add(f.getAbsoluteFile().toString());
                    }
                }
            }
        }
    }

    private Boolean checkExtension(String fileName, Set<String> extensions) {
        var isContain = false;
        for (var extension : extensions) {
            if (fileName.endsWith(extension)) {
                isContain = true;
                break;
            }
        }
        return isContain;
    }
}
