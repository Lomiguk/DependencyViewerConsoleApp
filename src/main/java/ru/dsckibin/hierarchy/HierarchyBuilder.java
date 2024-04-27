package ru.dsckibin.hierarchy;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import ru.dsckibin.util.ClassNameUtil;
import ru.dsckibin.util.jar.JarMaster;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HierarchyBuilder {
    private final static boolean DEFAULT_CHANGED_STATUS = true;

    private final JarMaster jarMaster;
    private final ClassNameUtil classNameUtil;

    public HierarchyBuilder(
            JarMaster jarMaster,
            ClassNameUtil classNameUtil) {
        this.jarMaster = jarMaster;
        this.classNameUtil = classNameUtil;
    }

    public Set<Node> build(
            String jarPath,
            List<String> diff
    ) {
        var result = new HashSet<Node>();
        jarMaster.getClassesAsByteArray(jarPath).forEach((name, bytes) -> {
            var byteNode = bytesToAsmClassNode(bytes);
            var jarNode = new Node(
                    classNameUtil.prepareClassNameToUse(name),
                    checkDiffsForContains(diff, byteNode.sourceFile)
            );
            jarNode
                    .addDependencies(getJarClassFieldsAsDependencyNodes(byteNode))
                    .addDependencies(getJarClassMethodDependenciesAsDependencyNodes(byteNode));

            result.add(jarNode);
        });
        return result;
    }

    public Set<Node> build(
            String jarPath
    ) {
        var result = new HashSet<Node>();
        jarMaster.getClassesAsByteArray(jarPath).forEach((name, bytes) -> {
            var byteNode = bytesToAsmClassNode(bytes);
            var jarNode = new Node(
                    classNameUtil.prepareClassNameToUse(name),
                    DEFAULT_CHANGED_STATUS
            );
            jarNode
                    .addDependencies(getJarClassFieldsAsDependencyNodes(byteNode))
                    .addDependencies(getJarClassMethodDependenciesAsDependencyNodes(byteNode));

            result.add(jarNode);
        });
        return result;
    }

    private Boolean checkDiffsForContains(Collection<String> diffs, String sourceFile) {
        for (var elem : diffs) {
            if (elem.endsWith(sourceFile)) return true;
        }
        return false;
    }

    private ClassNode bytesToAsmClassNode(byte[] bytes) {
        var classReader = new ClassReader(bytes);
        var classNode = new ClassNode();
        try {
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            classReader.accept(classNode, ClassReader.SKIP_FRAMES);
        }
        return classNode;
    }

    private Map<String, Dependency> getJarClassFieldsAsDependencyNodes(ClassNode parentNode) {
        var result = new HashMap<String, Dependency>();
        parentNode.fields.forEach(fieldNode ->
                addDependency(result, fieldNode.desc, TypeOfDependency.FIELD)
        );
        return result;
    }

    private Map<String, Dependency> getJarClassMethodDependenciesAsDependencyNodes(ClassNode parentNode) {
        var result = new HashMap<String, Dependency>();
        parentNode.methods.forEach(methodNode -> {
            var params = Type.getArgumentTypes(methodNode.desc);
            for (var param : params) {
                addDependency(result, param.getClassName(), TypeOfDependency.METHOD_PARAM);
            }

            methodNode.instructions.forEach(instruction -> {
                        switch (instruction.getOpcode()) {
                            case Opcodes.NEW -> addDependency(
                                    result,
                                    ((TypeInsnNode) instruction).desc,
                                    TypeOfDependency.NEW
                            );
                            case Opcodes.INVOKEINTERFACE, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEVIRTUAL ->
                                    addDependency(
                                            result,
                                            ((MethodInsnNode) instruction).owner,
                                            TypeOfDependency.INVOKE
                                    );
                            case Opcodes.INVOKEDYNAMIC -> addDependency(
                                    result,
                                    ((InvokeDynamicInsnNode) instruction).bsm.getOwner(),
                                    TypeOfDependency.INVOKE
                            );
                        }
                    }
            );
        });
        return result;
    }

    private void addDependency(
            Map<String, Dependency> dependencies,
            String name,
            TypeOfDependency typeOfDependency
    ) {
        var preparedName = classNameUtil.prepareClassNameToUse(name);
        if (dependencies.containsKey(preparedName)) {
            var dependency = dependencies.get(preparedName);
            if (dependency.containsKey(typeOfDependency)) {
                dependency.upWeight(typeOfDependency);
            } else {
                dependency.put(typeOfDependency, 1);
            }
        } else {
            var dependency = new Dependency();
            dependency.putNew(typeOfDependency);
            dependencies.put(preparedName, dependency);
        }
    }
}
