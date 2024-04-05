package ru.dsckibin.hierarchy;

import java.util.Objects;

public class DependencyNode {
    private final String name;
    private final TypeOfDependency typeOfDependency;

    public DependencyNode(String name, TypeOfDependency typeOfDependency) {
        this.name = name;
        this.typeOfDependency = typeOfDependency;
    }

    public String getName() {
        return name;
    }

    public TypeOfDependency getTypeOfDependency() {
        return typeOfDependency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyNode that = (DependencyNode) o;
        return name.equals(that.name) && typeOfDependency.equals(that.typeOfDependency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, typeOfDependency);
    }
}
