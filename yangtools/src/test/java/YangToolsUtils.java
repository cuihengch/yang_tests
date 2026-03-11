import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

import java.nio.file.Paths;
import java.util.List;
import java.util.ServiceLoader;

public final class YangToolsUtils {
    public static EffectiveModelContext loadSchema(List<String> files) throws Exception {
        YangParserFactory factory = ServiceLoader.load(YangParserFactory.class)
                .findFirst()
                .orElseThrow();

        var parser = factory.createParser();

        for (String file : files) {
            var f = Paths.get(file).toAbsolutePath();
            parser.addSource(new FileYangTextSource(f));
        }

        return parser.buildEffectiveModel();
    }

    public static StructureEffectiveStatement findStructure(
            EffectiveModelContext schemaContext,
            String moduleName,
            String structureName
    ) {
        var module = schemaContext.getModuleStatements().values().stream()
                .filter(m -> m.argument().getLocalName().equals(moduleName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Module not found: " + moduleName));

        return module.findFirstEffectiveSubstatement(StructureEffectiveStatement.class)
                .filter(stmt -> stmt.argument().getLocalName().equals(structureName))
                .orElseThrow(() -> new IllegalStateException(
                        "Structure not found: " + moduleName + ":" + structureName));
    }

    public static EffectiveStatementInference toInference(
            EffectiveModelContext schemaContext,
            StructureEffectiveStatement structure
    ) {
        var stack = SchemaInferenceStack.of(schemaContext);
        stack.enterSchemaTree(structure.argument());
        return stack.toInference();
    }

}

