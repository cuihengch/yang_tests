import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YangStructureTest {

    @Test
    void testYangStructure() throws Exception {
        List<String> schemaFile = List.of("../yang/minimal-structure/ietf-yang-structure-ext.yang", "../yang/minimal-structure/structure.yang");
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
        List<String> schemaFile = List.of("../yang/scotthuang-structure/scotthuang-structure.yang", "../yang/scotthuang-structure/ietf-yang-types.yang", "../yang/scotthuang-structure/ietf-yang-structure-ext.yang");
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
                            Files.newInputStream(Paths.get("../data/scotthuang_valid_structure_minimal.json"))
                    ))) {
                parser.parse(reader);
            }
        });
    }

    @Test
    void testYangStructureScottHuangValid() throws Exception {
        List<String> schemaFile = List.of("../yang/scotthuang-structure/scotthuang-structure.yang", "../yang/scotthuang-structure/ietf-yang-types.yang", "../yang/scotthuang-structure/ietf-yang-structure-ext.yang");
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
        List<String> schemaFile = List.of("../yang/scotthuang-structure/scotthuang-structure.yang", "../yang/scotthuang-structure/ietf-yang-types.yang", "../yang/scotthuang-structure/ietf-yang-structure-ext.yang");
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
        List<String> schemaFile = List.of("../yang/scotthuang-structure/scotthuang-structure.yang", "../yang/scotthuang-structure/ietf-yang-types.yang", "../yang/scotthuang-structure/ietf-yang-structure-ext.yang");
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