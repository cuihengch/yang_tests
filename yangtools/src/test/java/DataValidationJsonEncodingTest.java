import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.MandatoryLeafEnforcer;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.impl.ReferenceDataTreeFactory;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataValidationJsonEncodingTest {

    @Test
    void testValidJsonValidation() throws Exception {
        List<String> schemaFile = List.of("../yang/schema-test.yang");
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
                            Files.newInputStream(Paths.get("../data/valid.json"))
                    ))) {
                parser.parse(reader);
            }
        });

    }

    @Test
    void testInvalidJsonValidation() throws Exception {
        List<String> schemaFile = List.of("../yang/schema-test.yang");
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
                            Files.newInputStream(Paths.get("../data/invalid.json"))
                    ))) {
                parser.parse(reader);
            }
        });

    }

    @Test
    void testMissingMandatory() throws Exception {
        List<String> schemaFile = List.of("../yang/mandatory-test.yang");
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
                            Files.newInputStream(Paths.get("../data/missing.json"))
                    ))) {
                parser.parse(reader);
            }
        });

        NormalizedNode data = resultHolder.getResult().data();
        assertNotNull(data);

        YangInstanceIdentifier path = YangInstanceIdentifier.of(data.name());

        var schemaNode = DataSchemaContextTree.from(schema)
                .childByPath(path)
                .dataSchemaNode();

        DataNodeContainer containerSchema = assertInstanceOf(
                DataNodeContainer.class,
                schemaNode
        );

        MandatoryLeafEnforcer enforcer = MandatoryLeafEnforcer.forContainer(
                containerSchema,
                false
        );

        assertNotNull(enforcer);

        assertThrows(
                IllegalArgumentException.class,
                () -> enforcer.enforceOnData(data)
        );
    }

    @Test
    void testInvalidMustValidation() throws Exception {
        EffectiveModelContext schema = YangToolsUtils.loadSchema(
                List.of("../yang/must-test.yang")
        );

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schema)
        );

        assertDoesNotThrow(() -> {
            try (JsonReader reader = new JsonReader(
                    new InputStreamReader(
                            Files.newInputStream(Paths.get("../data/invalid-must-bis.json"))
                    ))) {
                parser.parse(reader);
            }
        });

        var data = resultHolder.getResult().data();
        DataTree dataTree = YangToolsUtils.newOperationalTree(schema);

        YangInstanceIdentifier path = YangInstanceIdentifier.of(data.name());

        var modification = dataTree.takeSnapshot().newModification();
        modification.write(path, data);
        modification.ready();

        assertThrows(Exception.class, () -> dataTree.validate(modification));
    }

}