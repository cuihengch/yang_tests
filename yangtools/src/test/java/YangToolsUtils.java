import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;

public final class YangToolsUtils {
    public static EffectiveModelContext loadSchema(List<String> files) throws Exception {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one YANG file must be provided");
        }

        var factory = ServiceLoader.load(YangParserFactory.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No YangParserFactory found — check yang-parser-rfc7950 is on the classpath"));

        var parser = factory.createParser();

        for (String file : files) {
            var path = Path.of(file).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("YANG file not found: " + path);
            }
            parser.addSource(new FileYangTextSource(path));
        }

        return parser.buildEffectiveModel();
    }

    public static EffectiveModelContext loadSchema(String folder) throws Exception {
        if (folder == null || folder.isBlank()) {
            throw new IllegalArgumentException("A folder path must be provided");
        }

        Path dir = Path.of(folder).toAbsolutePath().normalize();

        if (!Files.exists(dir)) {
            throw new IllegalArgumentException("Folder not found: " + dir);
        }
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Path is not a folder: " + dir);
        }

        List<Path> yangFiles;
        try (var stream = Files.walk(dir)) {
            yangFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yang"))
                    .sorted()
                    .toList();
        }

        if (yangFiles.isEmpty()) {
            throw new IllegalArgumentException("No .yang files found in folder: " + dir);
        }

        var factory = ServiceLoader.load(YangParserFactory.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No YangParserFactory found — check yang-parser-rfc7950 is on the classpath"));

        var parser = factory.createParser();

        for (Path path : yangFiles) {
            parser.addSource(new FileYangTextSource(path));
        }

        return parser.buildEffectiveModel();
    }

    public static void printDataTree(NormalizedNode node) {
        if (node == null) {
            System.out.println("<null>");
            return;
        }
        System.out.println(node.prettyTree().get());
    }


}

