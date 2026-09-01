package ru.dsckibin.util;

import org.objectweb.asm.Type;

import java.util.ArrayList;

public class ClassNameUtil {
    private static final String CLASS_EXTENSION = ".class";
    private static final String CLASS_PATH_SPLITTER = "/";
    private static final String DOT_CLASS_PATH_SPLITTER = ".";
    private static final int OPTIMAL_LENGTH = 15;

    public String prepareClassNameToUse(String className) {
        return changeNameSplitter(removeClassExtension(className));
    }

    private String removeClassExtension(String name) {
        if (name.endsWith(CLASS_EXTENSION)) {
            return name.substring(0, name.length()-6);
        }
        return name;
    }

    public String prepareAsmName(String name) {
        return Type.getType(name).getClassName();
    }

    public String changeNameSplitter(String name) {
        return name.replace(CLASS_PATH_SPLITTER, DOT_CLASS_PATH_SPLITTER);
    }

    public String simplifyName(String name) {
        var workedName = name.replace(DOT_CLASS_PATH_SPLITTER, CLASS_PATH_SPLITTER);
        var currentLength = workedName.length();
        if (currentLength < OPTIMAL_LENGTH) {
            return workedName.replace(CLASS_PATH_SPLITTER, DOT_CLASS_PATH_SPLITTER);
        }
        var parts = workedName.split(CLASS_PATH_SPLITTER);
        var newParts = new ArrayList<String>();
        for (var i = 0; i < parts.length; i++) {
            if (currentLength > OPTIMAL_LENGTH && i < parts.length - 1) {
                newParts.add(String.valueOf(parts[i].charAt(0)));
                currentLength -= parts[i].length() - 1;
                continue;
            }
            newParts.add(parts[i]);
        }

        return String.join(DOT_CLASS_PATH_SPLITTER, newParts);
    }
}
