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


}

