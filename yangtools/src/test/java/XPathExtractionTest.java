import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class XPathExtractionTest {

    @Test
    void minimalXpathTest() throws Exception {
        List<String> schemaFile = List.of("../yang/xpath/xpath-test.yang");
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
                            Files.newInputStream(Paths.get("../data/yangtools-xpath-test-valid.json"))
                    ))) {
                parser.parse(reader);
            }
        });

        NormalizedNode root = resultHolder.getResult().data();

        String ns = "urn:xpath:test";

        YangInstanceIdentifier path = YangInstanceIdentifier.of(
                YangInstanceIdentifier.NodeIdentifier.create(QName.create(ns, "child-container")),
                YangInstanceIdentifier.NodeIdentifier.create(QName.create(ns, "value1"))
        );

        Optional<NormalizedNode> result =
                NormalizedNodes.findNode(root, path);

        assertTrue(result.isPresent(), "Node not found");
        Object value = ((LeafNode<?>) result.get()).body();
        assertEquals("value", value);

    }

    @Test
    void minimalContainerXpathTest() throws Exception {
        List<String> schemaFile = List.of("../yang/xpath/xpath-test.yang");
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
                            Files.newInputStream(Paths.get("../data/yangtools-xpath-test-valid.json"))
                    ))) {
                parser.parse(reader);
            }
        });

        NormalizedNode root = resultHolder.getResult().data();

        String ns = "urn:xpath:test";

        YangInstanceIdentifier path = YangInstanceIdentifier.of(
                YangInstanceIdentifier.NodeIdentifier.create(QName.create(ns, "child-container"))
        );

        Optional<NormalizedNode> result =
                NormalizedNodes.findNode(root, path);

        assertTrue(result.isPresent(), "Node not found");

        assertInstanceOf(ContainerNode.class, result.get());


    }
}