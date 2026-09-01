package ru.dsckibin.util;

import ru.dsckibin.exception.NotDirectoryException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Comparator;

public class FileSearch {
    private final List<String> result = new ArrayList<>();

    public List<String> searchFilesInDirectoryByExtensions(File file, Set<String> extensions) {
        result.clear();
        if (file.isDirectory()) {
            searchByExtensions(file, extensions);
        } else {
            throw new NotDirectoryException("Not a directory: " + file.getAbsolutePath());
        }
        result.sort(Comparator.naturalOrder());
        return List.copyOf(result);
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

    private boolean checkExtension(String fileName, Set<String> extensions) {
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
