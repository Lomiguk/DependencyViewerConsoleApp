package ru.dsckibin.util.asm;

import java.util.HashSet;
import java.util.Set;

public enum Primitive {
    Z("boolean"),
    B("byte"),
    S("short"),
    I("integer"),
    J("long"),
    F("float"),
    D("double"),
    C("char"),
    V("void");

    public final String TYPE_NAME;

    Primitive(String name) {
        this.TYPE_NAME = name;
    }

    public static Set<String> enumSet() {
        Set<String> result = new HashSet<>();
        for (var primitive: Primitive.values()) {
            result.add(primitive.name());
        }
        return result;
    }
}
