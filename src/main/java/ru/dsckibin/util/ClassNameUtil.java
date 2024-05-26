package ru.dsckibin.util;

import ru.dsckibin.util.asm.Primitive;

import java.util.ArrayList;

public class ClassNameUtil {
    private static final String CLASS_EXTENSION = ".class";
    private static final String OBJECT_MARKER_PREFIX = "L";
    private static final String CLASS_PATH_SPLITTER = "/";
    private static final String DOT_CLASS_PATH_SPLITTER = ".";
    private static final String ARRAY_START_MARKER = "[";
    private static final char ARRAY_START_CHAR_MARKER = '[';
    private static final String ARRAY_END_MARKER = ";";
    private static final String SUBCLASS_MARKER = "$";
    private static final String NEW_ARRAY_MARKER = "[]";
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
        var result = name;

        if (result.startsWith(ARRAY_START_MARKER)) {
            result = prepareArray(result);
        }
        if (result.startsWith(OBJECT_MARKER_PREFIX)) {
            result = prepareObject(result);
        } else if (Primitive.enumSet().contains(result)) {
            result = Primitive.valueOf(result).TYPE_NAME;
        }
        if (result.contains(SUBCLASS_MARKER)) {
            result = result.substring(0, result.indexOf(SUBCLASS_MARKER)-1);
        }

        return changeNameSplitter(result);
    }

    public String changeNameSplitter(String name) {
        return name.replace(CLASS_PATH_SPLITTER, DOT_CLASS_PATH_SPLITTER);
    }

    private String prepareObject(String className) {
        var result = className.replace(";", "");
        return result.substring(1);
    }

    private String prepareArray(String className) {
        var bracketCount = 0;
        var arrays = new StringBuilder();
        while (bracketCount < className.length() && className.charAt(bracketCount) == ARRAY_START_CHAR_MARKER) {
            bracketCount++;
            arrays.append(NEW_ARRAY_MARKER);
        }
        if (className.endsWith(ARRAY_END_MARKER)) {
            return className.substring(bracketCount, className.length()-1) + arrays;
        } else {
            return className.substring(bracketCount) + arrays;
        }
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
