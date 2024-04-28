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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HierarchyBuilder {
    private final static Logger LOGGER = Logger.getLogger(HierarchyBuilder.class.getName());
    private final static GitView DEFAULT_CHANGED_STATUS = GitView.CHANGED;

    private final JarMaster jarMaster;
    private final ClassNameUtil classNameUtil;

    public HierarchyBuilder(
            JarMaster jarMaster,
            ClassNameUtil classNameUtil) {
        this.jarMaster = jarMaster;
        this.classNameUtil = classNameUtil;
    }

    public Hierarchy buildWithDiff(
            String jarPath,
            List<String> diff
    ) {
        return build(jarPath, diff);
    }

    public Hierarchy buildWithoutDiff(
            String jarPath
    ) {
        return build(jarPath, null);
    }

    private Hierarchy build(
            String jarPath,
            List<String> diff
    ) {
        var result = new Hierarchy();
        jarMaster.getClassesAsByteArray(jarPath).forEach((name, bytes) -> {
            var byteNode = bytesToAsmClassNode(bytes);
            var jarNode = new Node(
                    classNameUtil.prepareClassNameToUse(name),
                    diff != null ? checkDiffsForContains(diff, byteNode.sourceFile) : DEFAULT_CHANGED_STATUS
            );
            jarNode.addDependencies(result.registeringDependencyNodes(getJarClassFieldsAsDependencyNodes(byteNode)))
                   .addDependencies(result.registeringDependencyNodes(getJarClassMethodDependenciesAsDependencyNodes(byteNode)));

            result.addNodeAsJar(jarNode);
        });

        return transformIntervalNodes(result);
    }

    private Hierarchy transformIntervalNodes(Hierarchy rootNodes) {
        for (var rootNode : rootNodes.values()) {
            if (!rootNode.getGitView().equals(GitView.CHANGED)) continue;
            setStatusOfIntermediateNodesAsAnInterval(new HashSet<>(), rootNode);
        }
        return rootNodes;
    }

    private void setStatusOfIntermediateNodesAsAnInterval(Set<Node> prePath, Node currentNode) {
        var dependencyNodes = currentNode.getDependencies().keySet();
        if (prePath.contains(currentNode)) { return;}
        var path = new HashSet<>(prePath);
        path.add(currentNode);
        for (var node : dependencyNodes) {
            if (node.getGitView().equals(GitView.CHANGED)) {
                transformIntervalPathNodes(path);
                continue;
            }
            setStatusOfIntermediateNodesAsAnInterval(path, node);
        }
    }

    private void transformIntervalPathNodes(Set<Node> path) {
        path.forEach(node -> {
            if (node.getGitView().equals(GitView.NOT_CHANGED)) {
                node.setIntervalToGitView();
            }
        });
    }

    private GitView checkDiffsForContains(Collection<String> diffs, String sourceFile) {
        for (var elem : diffs) {
            if (elem.endsWith(sourceFile)) return GitView.CHANGED;
        }
        return GitView.NOT_CHANGED;
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

    private Dependencies getJarClassFieldsAsDependencyNodes(ClassNode parentNode) {
        var result = new Dependencies();
        parentNode.fields.forEach(fieldNode ->
                addDependency(
                        result,
                        classNameUtil.prepareAsmName(fieldNode.desc),
                        TypeOfDependency.FIELD
                )
        );
        return result;
    }

    private Dependencies getJarClassMethodDependenciesAsDependencyNodes(ClassNode parentNode) {
        var result = new Dependencies();
        parentNode.methods.forEach(methodNode -> {
            var params = Type.getArgumentTypes(methodNode.desc);
            for (var param : params) {
                addDependency(
                        result,
                        classNameUtil.changeNameSplitter(param.getClassName()),
                        TypeOfDependency.METHOD_PARAM
                );
            }

            methodNode.instructions.forEach(instruction -> {
                        switch (instruction.getOpcode()) {
                            case Opcodes.NEW -> addDependency(
                                    result,
                                    classNameUtil.prepareAsmName(((TypeInsnNode) instruction).desc),
                                    TypeOfDependency.NEW
                            );
                            case Opcodes.INVOKEINTERFACE, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEVIRTUAL ->
                                    addDependency(
                                            result,
                                            classNameUtil.prepareAsmName(((MethodInsnNode) instruction).owner),
                                            TypeOfDependency.INVOKE
                                    );
                            case Opcodes.INVOKEDYNAMIC -> addDependency(
                                    result,
                                    classNameUtil.prepareAsmName(((InvokeDynamicInsnNode) instruction).bsm.getOwner()),
                                    TypeOfDependency.INVOKE
                            );
                        }
                    }
            );
        });
        return result;
    }

    private void addDependency(
            Map<Node, Dependency> dependencies,
            String name,
            TypeOfDependency typeOfDependency
    ) {
        if (dependencies.containsKey(new Node(name))) {
            Node node = null;
            for (var depNode : dependencies.keySet()) {
                if (depNode.getName().equals(name)) {
                    node = depNode;
                }
            }
            if (node == null) {
                LOGGER.log(Level.WARNING, String.format("%s class node was skipped", name));
                return;
            }
            var dependency = dependencies.get(node);
            if (dependency.containsKey(typeOfDependency)) {
                dependency.upWeight(typeOfDependency);
            } else {
                dependency.put(typeOfDependency, 1);
            }
        } else {
            var node = new Node(name);
            var dependency = new Dependency();
            dependency.putNew(typeOfDependency);
            dependencies.put(node, dependency);
        }
    }
}
