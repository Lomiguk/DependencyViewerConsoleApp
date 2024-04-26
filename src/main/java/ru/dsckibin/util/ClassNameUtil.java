package ru.dsckibin.util;

import ru.dsckibin.util.asm.Primitive;

import java.util.ArrayList;

public class ClassNameUtil {
    private static final String OBJECT_MARKER_PREFIX = "L";
    private static final String OBJECT_MARKER_POSTFIX = ";";
    private static final String CLASS_PATH_SPLITTER = "/";
    private static final String DOT_CLASS_PATH_SPLITTER = ".";
    private static final String ARRAY_START_MARKER = "[";
    private static final char ARRAY_START_CHAR_MARKER = '[';
    private static final String ARRAY_END_MARKER = ";";
    private static final String NEW_ARRAY_MARKER = "[]";
    private static final int OBJECT_NAME_START_POSITION = 1;
    private static final int OPTIMAL_LENGTH = 15;

    public String prepareClassNameToUse(String className) {
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length()-6);
        }

        if (className.startsWith(OBJECT_MARKER_PREFIX) && className.endsWith(OBJECT_MARKER_POSTFIX)) {
            return simplifyName(className.substring(OBJECT_NAME_START_POSITION, className.length()-1));
        }

        if (Primitive.enumSet().contains(className)) {
            return simplifyName(Primitive.valueOf(className).TYPE_NAME);
        }

        if (className.startsWith(OBJECT_MARKER_PREFIX) && className.endsWith(OBJECT_MARKER_POSTFIX)) {
            return simplifyName(prepareObject(className));
        }

        if (className.startsWith(ARRAY_START_MARKER)) {
            return simplifyName(prepareArray(className));
        }

        return simplifyName(className);
    }

    private String prepareObject(String className) {
        return className.substring(1, className.length()-1);
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
