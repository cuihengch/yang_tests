import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.MandatoryLeafEnforcer;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.dagger.ReferenceDataTreeFactoryModule;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YangSubtreeValidationTest {

    private static DataTree newOperationalTree(EffectiveModelContext schemaContext) {
        DataTreeFactory factory = ReferenceDataTreeFactoryModule.provideDataTreeFactory();
        return factory.create(DataTreeConfiguration.DEFAULT_OPERATIONAL, schemaContext);
    }

    @Test
    void subtreeValidationTest() throws Exception {
        List<String> schemaFiles = List.of("../yang/subtree.yang");
        EffectiveModelContext schemaContext = YangToolsUtils.loadSchema(schemaFiles);
        assertNotNull(schemaContext);

        QName rootQName = QName.create("urn:example", "root").intern();
        QName contentQName = QName.create("urn:example", "content").intern();
        QName subscriptionQName = QName.create("urn:example", "subscription").intern();

        var contentInference = SchemaInferenceStack
                .ofDataTreePath(schemaContext, rootQName, contentQName)
                .toInference();

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schemaContext),
                contentInference
        );

        try (JsonReader reader = new JsonReader(
                new InputStreamReader(
                        Files.newInputStream(Paths.get("../data/subtree.json"))
                ))) {
            parser.parse(reader);
        }

        assertNotNull(resultHolder.getResult());

        ContainerNode subscriptionNode = assertInstanceOf(
                ContainerNode.class,
                resultHolder.getResult().data()
        );

        YangInstanceIdentifier subscriptionPath =
                YangInstanceIdentifier.of(new YangInstanceIdentifier.NodeIdentifier(rootQName))
                        .node(new YangInstanceIdentifier.NodeIdentifier(contentQName))
                        .node(new YangInstanceIdentifier.NodeIdentifier(subscriptionQName));

        DataTree dataTree = newOperationalTree(schemaContext);

        DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(subscriptionPath, subscriptionNode);
        mod.ready();

        dataTree.validate(mod);
        dataTree.commit(dataTree.prepare(mod));

        NormalizedNode readNode =
                dataTree.takeSnapshot().readNode(subscriptionPath).orElseThrow();

        ContainerNode validatedSubscription = assertInstanceOf(ContainerNode.class, readNode);

        var subscriptionSchemaContext = DataSchemaContextTree.from(schemaContext)
                .findChild(subscriptionPath)
                .orElseThrow();

        DataNodeContainer subscriptionSchema = assertInstanceOf(
                DataNodeContainer.class,
                subscriptionSchemaContext.dataSchemaNode()
        );

        MandatoryLeafEnforcer enforcer = MandatoryLeafEnforcer.forContainer(
                subscriptionSchema,
                true
        );

        assertNull(enforcer);

        System.out.println("=== Validated subtree ===");
        YangToolsUtils.printDataTree(validatedSubscription);
    }
}