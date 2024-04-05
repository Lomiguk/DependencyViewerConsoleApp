package ru.dsckibin.util.asm;

import java.util.ArrayList;

public class ClassNameUtil {
    private static final String OBJECT_MARKER_PREFIX = "L";
    private static final String OBJECT_MARKER_POSTFIX = ";";
    private static final String CLASS_PATH_SPLITTER = "/";
    private static final String WRONG_CLASS_PATH_SPLITTER = ".";
    public String prepareClassNameToUse(String className) {
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length()-6);
        }

        if (className.startsWith(OBJECT_MARKER_PREFIX) && className.endsWith(OBJECT_MARKER_POSTFIX)) {
            return className.substring(1, className.length()-1);
        }

        // TODO(сделать нормальное преобразование)

        if (className.startsWith("[[") && className.endsWith(";")) {
            return className.substring(2, className.length()-1) + "[][]";
        }

        if (className.startsWith("[[")) {
            return className.substring(2) + "[][]";
        }

        if (className.startsWith("[") && className.endsWith(";")) {
            return className.substring(1, className.length()-1) + "[]";
        }

        if (className.startsWith("[")) {
            return className.substring(1) + "[]";
        }

        //TODO

        if (Primitive.enumSet().contains(className)) {
            return Primitive.valueOf(className).TYPE_NAME;
        }

        return className.replace(WRONG_CLASS_PATH_SPLITTER, CLASS_PATH_SPLITTER);
    }

    public String simplifyName(String name, Integer optimalLength) {
        var currentLength = name.length();
        if (currentLength < optimalLength) {
            return name;
        }
        var parts = name.split(CLASS_PATH_SPLITTER);
        var newParts = new ArrayList<String>();
        for (var part : parts) {
            if (currentLength > optimalLength) {
                newParts.add(String.valueOf(part.charAt(1)));
                currentLength -= part.length() - 1;
            }
            newParts.add(part);
        }

        return String.join(CLASS_PATH_SPLITTER, newParts);
    }
}
