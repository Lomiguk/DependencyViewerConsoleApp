package ru.dsckibin.hierarchy;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import ru.dsckibin.util.FileNameUtil;
import ru.dsckibin.util.asm.ClassNameUtil;
import ru.dsckibin.util.jar.JarMaster;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HierarchyBuilder {
    private final static int BASE_COUNT_VALUE = 1;

    private final JarMaster jarMaster;
    private final ClassNameUtil classNameUtil;
    private final FileNameUtil fileNameUtil;

    public HierarchyBuilder(
            JarMaster jarMaster,
            ClassNameUtil classNameUtil,
            FileNameUtil fileNameUtil) {
        this.jarMaster = jarMaster;
        this.classNameUtil = classNameUtil;
        this.fileNameUtil = fileNameUtil;
    }

    public Set<Node> build(String jarPath, List<String> diff) {
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

    private Boolean checkDiffsForContains(Collection<String> diffs, String sourceFile) {
        for (var elem : diffs) {
            if (elem.endsWith(sourceFile)) return false;
        }
        return true;
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

    private Map<DependencyNode, Integer> getJarClassFieldsAsDependencyNodes(ClassNode parentNode) {
        var result = new HashMap<DependencyNode, Integer>();
        parentNode.fields.forEach(fieldNode ->
                addDependencyNode(result, fieldNode.desc, TypeOfDependency.FIELD)
        );
        return result;
    }

    private Map<DependencyNode, Integer> getJarClassMethodDependenciesAsDependencyNodes(ClassNode parentNode) {
        var result = new HashMap<DependencyNode, Integer>();
        parentNode.methods.forEach(methodNode -> {
            var params = Type.getArgumentTypes(methodNode.desc);
            for (var param : params) {
                addDependencyNode(result, param.getClassName(), TypeOfDependency.METHOD_PARAM);
            }

            methodNode.instructions.forEach(instruction -> {
                        switch (instruction.getOpcode()) {
                            case Opcodes.NEW -> addDependencyNode(
                                    result,
                                    ((TypeInsnNode) instruction).desc,
                                    TypeOfDependency.NEW
                            );
                            case Opcodes.INVOKEINTERFACE, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEVIRTUAL ->
                                    addDependencyNode(
                                            result,
                                            ((MethodInsnNode) instruction).owner,
                                            TypeOfDependency.INVOKE
                                    );
                            case Opcodes.INVOKEDYNAMIC -> addDependencyNode(
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

    private void addDependencyNode(
            Map<DependencyNode, Integer> dependencies,
            String name,
            TypeOfDependency typeOfDependency
    ) {
        var node = new DependencyNode(
                classNameUtil.prepareClassNameToUse(fileNameUtil.clear(name)),
                typeOfDependency
        );
        if (dependencies.containsKey(node)) {
            dependencies.put(node, dependencies.get(node) + 1);
        } else {
            dependencies.put(node, BASE_COUNT_VALUE);
        }
    }
}
