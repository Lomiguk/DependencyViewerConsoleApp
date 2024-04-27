package ru.dsckibin.util;

import ru.dsckibin.util.asm.Primitive;

import java.util.ArrayList;

public class ClassNameUtil {
    private static final String CLASS_EXTENSION = ".class";
    private static final String OBJECT_MARKER_PREFIX = "L";
    private static final String OBJECT_MARKER_POSTFIX = ";";
    private static final String CLASS_PATH_SPLITTER = "/";
    private static final String DOT_CLASS_PATH_SPLITTER = ".";
    private static final String ARRAY_START_MARKER = "[";
    private static final char ARRAY_START_CHAR_MARKER = '[';
    private static final String ARRAY_END_MARKER = ";";
    private static final String SUBCLASS_MARKER = "$";
    private static final String NEW_ARRAY_MARKER = "[]";
    private static final int OPTIMAL_LENGTH = 15;

    public String prepareClassNameToUse(String className) {
        var preparedName = className;

        if (preparedName.endsWith(CLASS_EXTENSION)) {
            preparedName = preparedName.substring(0, preparedName.length()-6);
        }
        if (preparedName.startsWith(ARRAY_START_MARKER)) {
            preparedName = prepareArray(preparedName);
        }
        if (preparedName.startsWith(OBJECT_MARKER_PREFIX)) {
            preparedName = prepareObject(preparedName);
        } else if (Primitive.enumSet().contains(preparedName)) {
            preparedName = Primitive.valueOf(preparedName).TYPE_NAME;
        }
        if (preparedName.contains(SUBCLASS_MARKER)) {
            preparedName = preparedName.substring(0, preparedName.indexOf(SUBCLASS_MARKER)-1);
        }

        return preparedName.replace(CLASS_PATH_SPLITTER, DOT_CLASS_PATH_SPLITTER);
    }

    private String prepareObject(String className) {
        if (className.endsWith(OBJECT_MARKER_POSTFIX)) {
            return className.substring(1, className.length()-1);
        } else {
            return className.substring(1);
        }
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
