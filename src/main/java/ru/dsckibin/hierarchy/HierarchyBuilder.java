package ru.dsckibin.hierarchy;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
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

    public HierarchyBuilder(JarMaster jarMaster, ClassNameUtil classNameUtil) {
        this.jarMaster = jarMaster;
        this.classNameUtil = classNameUtil;
    }

    public Set<Node> build(String jarPath, List<String> diff) {
        var result = new HashSet<Node>();
        jarMaster.getClassesAsByteArray(jarPath).forEach((name, bytes) -> {
            var byteNode = bytesToAsmClassNode(bytes);
            var jarNode = new Node(
                    classNameUtil.prepareClassNameToUse(name),
                    checkDiffsForContains(diff, byteNode.sourceFile)
            );
            jarNode.addDependencies(
                    getJarClassFieldsAsDependencyNodes(byteNode)
            );
            jarNode.addDependencies(
                    getJarClassMethodDependenciesAsDependencyNodes(byteNode)
            );

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
        parentNode.fields.forEach( fieldNode -> {
            var node = new DependencyNode(
                    classNameUtil.prepareClassNameToUse(fieldNode.desc),
                    TypeOfDependency.FIELD
            );
            if (result.containsKey(node)) {
                result.put(node, result.get(node) + 1);
            } else  {
                result.put(node, BASE_COUNT_VALUE);
            }
        });
        return result;
    }

    private Map<DependencyNode, Integer> getJarClassMethodDependenciesAsDependencyNodes(ClassNode parentNode) {
        var result = new HashMap<DependencyNode, Integer>();
        parentNode.methods.forEach( methodNode -> {
            var params = Type.getArgumentTypes(methodNode.desc);
            for (var param : params) {
                var node = new DependencyNode(
                        classNameUtil.prepareClassNameToUse(param.getClassName()),
                        TypeOfDependency.METHOD_PARAM
                );
                if (result.containsKey(node)) {
                    result.put(node, result.get(node) + 1);
                } else  {
                    result.put(node, BASE_COUNT_VALUE);
                }
            }
        });
        return result;
    }
}
