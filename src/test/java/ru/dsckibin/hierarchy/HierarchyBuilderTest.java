package ru.dsckibin.hierarchy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.dsckibin.util.ClassNameUtil;
import ru.dsckibin.util.jar.JarMaster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HierarchyBuilderTest {
    @TempDir
    Path directory;

    @Test
    void readsFieldAndMethodDependenciesFromAnArchive() throws IOException {
        var archive = directory.resolve("sample.jar");
        var resource = "/" + Sample.class.getName().replace('.', '/') + ".class";
        try (var input = getClass().getResourceAsStream(resource);
             var output = new JarOutputStream(Files.newOutputStream(archive))) {
            output.putNextEntry(new JarEntry(resource.substring(1)));
            output.write(input.readAllBytes());
            output.closeEntry();
        }

        var nodes = new HierarchyBuilder(new JarMaster(), new ClassNameUtil()).build(archive.toString());
        var sample = nodes.iterator().next();

        assertTrue(sample.getDependencies().containsKey("java.lang.String"));
        assertTrue(sample.getDependencies().containsKey("java.util.List"));
    }

    @Test
    void matchesDiffsByPackagePathAndHandlesInnerClasses() {
        var builder = new HierarchyBuilder(new JarMaster(), new ClassNameUtil());

        assertFalse(builder.build(createArchive(), List.of("src/main/java/other/Sample.java"))
                .iterator().next().getChangedStatus());
        assertTrue(builder.build(
                        createArchive(),
                        List.of("src/test/java/ru/dsckibin/hierarchy/HierarchyBuilderTest.java")
                ).iterator().next().getChangedStatus());
    }

    private String createArchive() {
        var archive = directory.resolve("inner.jar");
        var resource = "/" + Sample.class.getName().replace('.', '/') + ".class";
        try (var input = getClass().getResourceAsStream(resource);
             var output = new JarOutputStream(Files.newOutputStream(archive))) {
            output.putNextEntry(new JarEntry(resource.substring(1)));
            output.write(input.readAllBytes());
            output.closeEntry();
            return archive.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static class Sample {
        String name;

        void accept(List<String> values) {
            values.size();
        }
    }
}
