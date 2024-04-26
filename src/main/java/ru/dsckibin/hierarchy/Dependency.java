package ru.dsckibin.hierarchy;

import java.util.HashMap;

public class Dependency extends HashMap<TypeOfDependency, Integer> {
    private final static int INC_VALUE = 1;

    public void upWeight(TypeOfDependency typeOfDependency) {
        this.put(typeOfDependency, this.get(typeOfDependency) + INC_VALUE);
    }

    public void putNew(TypeOfDependency typeOfDependency) {
        this.put(typeOfDependency, INC_VALUE);
    }
}