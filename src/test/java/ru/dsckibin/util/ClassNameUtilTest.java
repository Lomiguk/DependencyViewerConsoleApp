package ru.dsckibin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassNameUtilTest {
    private final ClassNameUtil names = new ClassNameUtil();

    @Test
    void preparesArchiveClassNamesWithoutDamagingInnerClasses() {
        assertEquals(
                "com.example.Outer$Inner",
                names.prepareClassNameToUse("com/example/Outer$Inner.class")
        );
    }

    @Test
    void preparesObjectAndArrayDescriptors() {
        assertEquals("java.lang.String", names.prepareAsmName("Ljava/lang/String;"));
        assertEquals("java.lang.String[][]", names.prepareAsmName("[[Ljava/lang/String;"));
        assertEquals("int[]", names.prepareAsmName("[I"));
        assertEquals("com.example.Outer$Inner", names.prepareAsmName("Lcom/example/Outer$Inner;"));
    }
}
