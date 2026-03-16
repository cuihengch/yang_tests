import com.google.gson.stream.JsonReader;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YangStructureTest {

    @Test
    void testYangStructure() throws Exception {
        List<String> schemaFile = List.of("../yang/ietf-yang-structure-ext.yang", "../yang/structure.yang");
        EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        assertNotNull(schema);
    }

    @Test
    void testYangStructureMinimal() throws Exception {
        List<String> schemaFile = List.of("../yang/struct-test.yang");
        EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        assertNotNull(schema);
    }

    @Test
    void testYangStructureScottHuangValidMinimal() throws Exception {
        List<String> schemaFile = List.of("../yang/scotthuang-structure.yang", "../yang/ietf-yang-types.yang", "../yang/ietf-yang-structure-ext.yang");
        EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        assertNotNull(schema);

        var module = schema.getModuleStatements().values().stream()
                .filter(m -> m.argument().getLocalName().equals("test-structure"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Module not found: test-structure"));

        System.out.println("=== MODULE === " + module.argument());

        System.out.println("=== DECLARED SUBSTATEMENTS ===");
        module.getDeclared().declaredSubstatements().forEach(stmt -> System.out.println(
                stmt.getClass().getName()
                        + " | "
                        + stmt.statementDefinition().getStatementName()
                        + " | arg=" + stmt.rawArgument()
        ));

        System.out.println("=== EFFECTIVE SUBSTATEMENTS ===");
        module.effectiveSubstatements().forEach(stmt -> System.out.println(
                stmt.getClass().getName()
                        + " | "
                        + stmt.statementDefinition().getStatementName()
                        + " | arg=" + stmt.argument()
        ));

        var structure = YangToolsUtils.findStructure(schema, "test-structure", "message");

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schema)
        );

        assertDoesNotThrow(() -> {
            try (JsonReader reader = new JsonReader(
                    new InputStreamReader(
                            Files.newInputStream(Paths.get("../data/scotthuang_valid_structure_minimal.json"))
                    ))) {
                parser.parse(reader);
            }
        });
    }

    @Test
    void testYangStructureScottHuangValid() throws Exception {
        List<String> schemaFile = List.of("../yang/scotthuang-structure.yang", "../yang/ietf-yang-types.yang", "../yang/ietf-yang-structure-ext.yang");
        EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        assertNotNull(schema);

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schema)
        );

        assertDoesNotThrow(() -> {
            try (JsonReader reader = new JsonReader(
                    new InputStreamReader(
                            Files.newInputStream(Paths.get("../data/scotthuang_valid_structure_message.json"))
                    ))) {
                parser.parse(reader);
            }
        });
    }

    @Test
    void testYangStructureScottHuangInvalid() throws Exception {
        List<String> schemaFile = List.of("../yang/scotthuang-structure.yang", "../yang/ietf-yang-types.yang", "../yang/ietf-yang-structure-ext.yang");
        EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        assertNotNull(schema);

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schema)
        );

        assertThrows(Exception.class, () -> {
            try (JsonReader reader = new JsonReader(
                    new InputStreamReader(
                            Files.newInputStream(Paths.get("../data/scotthuang_invalid_structure_type.json"))
                    ))) {
                parser.parse(reader);
            }
        });
    }

    @Test
    void testYangStructureScottHuangInvalidMissingMandatory() throws Exception {
        List<String> schemaFile = List.of("../yang/scotthuang-structure.yang", "../yang/ietf-yang-types.yang", "../yang/ietf-yang-structure-ext.yang");
        EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        assertNotNull(schema);

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schema)
        );

        assertThrows(Exception.class, () -> {
            try (JsonReader reader = new JsonReader(
                    new InputStreamReader(
                            Files.newInputStream(Paths.get("../data/scotthuang_invalid_structure_missing_mandatory.json"))
                    ))) {
                parser.parse(reader);
            }
        });
    }
}