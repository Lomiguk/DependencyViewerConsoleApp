package ru.dsckibin.util;

import ru.dsckibin.util.asm.Primitive;

public class FileNameUtil {

    private static final String OBJECT_MARKER_PREFIX = "L";
    private static final String OBJECT_MARKER_POSTFIX = ";";
    private static final String ARRAY_START_MARKER = "[";
    private static final String ARRAY_END_MARKER = ";";
    private static final String NEW_ARRAY_MARKER = "[]";
    private static final int OBJECT_NAME_START_POSITION = 1;
    private static final int ARRAY_NAME_START_POSITION = 2;
    private static final char ASM_NAME_SPLITTER = '.';
    private static final char PROGRAM_NAME_SPLITTER = '/';

    public String clear(String className) {
        if (className.startsWith(OBJECT_MARKER_PREFIX) && className.endsWith(OBJECT_MARKER_POSTFIX)) {
            return className.substring(OBJECT_NAME_START_POSITION, className.length()-1);
        }

        if (className.startsWith(ARRAY_START_MARKER) && className.endsWith(ARRAY_END_MARKER)) {
            return className.substring(ARRAY_NAME_START_POSITION, className.length()-1) + NEW_ARRAY_MARKER;
        }

        if (Primitive.enumSet().contains(className)) {
            return Primitive.valueOf(className).TYPE_NAME;
        }

        return className.replace(ASM_NAME_SPLITTER, PROGRAM_NAME_SPLITTER);
    }
}
