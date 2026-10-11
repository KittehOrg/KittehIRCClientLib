package org.kitteh.irc.client.library;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Confirms every package in the library is exported by module-info.java.
 */
public class ModuleInfoTest {
    @Test
    public void testAllPackagesExported() throws IOException {
        Path classes = Path.of("target", "classes");
        ModuleDescriptor moduleDescriptor;
        try (InputStream inputStream = Files.newInputStream(classes.resolve("module-info.class"))) {
            moduleDescriptor = ModuleDescriptor.read(inputStream);
        }
        Set<String> exported = moduleDescriptor.exports().stream()
                .filter(export -> !export.isQualified())
                .map(ModuleDescriptor.Exports::source)
                .collect(Collectors.toSet());
        Set<String> missing;
        try (Stream<Path> files = Files.walk(classes)) {
            missing = files
                    .filter(file -> file.toString().endsWith(".class") && !file.getFileName().toString().equals("module-info.class"))
                    .map(file -> classes.relativize(file.getParent()).toString().replace(file.getFileSystem().getSeparator(), "."))
                    .filter(pack -> !exported.contains(pack))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
        Assertions.assertEquals(Set.of(), missing, "Packages missing from module-info.java exports");
    }
}
