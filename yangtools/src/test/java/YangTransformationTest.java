import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.dagger.ReferenceDataTreeFactoryModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YangTransformationTest {

    private static DataTree newOperationalTree(EffectiveModelContext schemaContext) {
        DataTreeFactory factory = ReferenceDataTreeFactoryModule.provideDataTreeFactory();
        return factory.create(DataTreeConfiguration.DEFAULT_OPERATIONAL, schemaContext);
    }

    @Test
    void addDataTest() throws Exception {
        List<String> schemaFiles = List.of("../yang/yang-transformation.yang");
        EffectiveModelContext schemaContext = YangToolsUtils.loadSchema(schemaFiles);
        assertNotNull(schemaContext);

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schemaContext)
        );

        try (JsonReader reader = new JsonReader(
                new InputStreamReader(
                        Files.newInputStream(Paths.get("../data/yang_transformation_bis.json"))
                ))) {
            parser.parse(reader);
        }

        assertNotNull(resultHolder.getResult());

        var normalizationResult = resultHolder.getResult();
        var rootNode = normalizationResult.data();
        System.out.println("=== Parsed JSON ===");
        YangToolsUtils.printDataTree(rootNode);
        ContainerNode fooNode = assertInstanceOf(ContainerNode.class, rootNode);

        DataTree dataTree = newOperationalTree(schemaContext);

        QName fooQName = QName.create("urn:yang:transformation", "foo").intern();
        QName numQName = QName.create("urn:yang:transformation", "num").intern();

        YangInstanceIdentifier fooPath =
                YangInstanceIdentifier.of(new YangInstanceIdentifier.NodeIdentifier(fooQName));
        YangInstanceIdentifier numPath =
                fooPath.node(new YangInstanceIdentifier.NodeIdentifier(numQName));

        DataTreeModification initMod = dataTree.takeSnapshot().newModification();
        initMod.write(fooPath, fooNode);
        initMod.ready();
        dataTree.validate(initMod);
        dataTree.commit(dataTree.prepare(initMod));

        LeafNode<Integer> numLeaf = ImmutableNodes.leafNode(numQName, 3);

        DataTreeModification updateMod = dataTree.takeSnapshot().newModification();
        updateMod.write(numPath, numLeaf);
        updateMod.ready();
        dataTree.validate(updateMod);
        dataTree.commit(dataTree.prepare(updateMod));

        NormalizedNode readNode =
                dataTree.takeSnapshot().readNode(numPath).orElseThrow();

        LeafNode<?> leafNode = assertInstanceOf(LeafNode.class, readNode);
        assertEquals(3, leafNode.body());
        System.out.println("=== DataTree after update ===");
        NormalizedNode updatedFoo =
                dataTree.takeSnapshot().readNode(fooPath).orElseThrow();
        YangToolsUtils.printDataTree(updatedFoo);
    }

    @Test
    void updateDataTest() throws Exception {
        List<String> schemaFiles = List.of("../yang/yang-transformation.yang");
        EffectiveModelContext schemaContext = YangToolsUtils.loadSchema(schemaFiles);
        assertNotNull(schemaContext);

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schemaContext)
        );

        try (JsonReader reader = new JsonReader(
                new InputStreamReader(
                        Files.newInputStream(Paths.get("../data/yang_transformation_bis.json"))
                ))) {
            parser.parse(reader);
        }

        assertNotNull(resultHolder.getResult());

        var normalizationResult = resultHolder.getResult();
        var rootNode = normalizationResult.data();
        System.out.println("=== Parsed JSON ===");
        YangToolsUtils.printDataTree(rootNode);
        ContainerNode fooNode = assertInstanceOf(ContainerNode.class, rootNode);

        DataTree dataTree = newOperationalTree(schemaContext);

        QName fooQName = QName.create("urn:yang:transformation", "foo").intern();
        QName valToUpdateQName = QName.create("urn:yang:transformation", "value-to-update").intern();

        YangInstanceIdentifier fooPath =
                YangInstanceIdentifier.of(new YangInstanceIdentifier.NodeIdentifier(fooQName));
        YangInstanceIdentifier numPath =
                fooPath.node(new YangInstanceIdentifier.NodeIdentifier(valToUpdateQName));

        DataTreeModification initMod = dataTree.takeSnapshot().newModification();
        initMod.write(fooPath, fooNode);
        initMod.ready();
        dataTree.validate(initMod);
        dataTree.commit(dataTree.prepare(initMod));

        LeafNode<Integer> valToUpdateLeaf = ImmutableNodes.leafNode(valToUpdateQName, 3);

        DataTreeModification updateMod = dataTree.takeSnapshot().newModification();
        updateMod.write(numPath, valToUpdateLeaf);
        updateMod.ready();
        dataTree.validate(updateMod);
        dataTree.commit(dataTree.prepare(updateMod));

        NormalizedNode readNode =
                dataTree.takeSnapshot().readNode(numPath).orElseThrow();

        LeafNode<?> leafNode = assertInstanceOf(LeafNode.class, readNode);
        assertEquals(3, leafNode.body());
        System.out.println("=== DataTree after update ===");
        NormalizedNode updatedFoo =
                dataTree.takeSnapshot().readNode(fooPath).orElseThrow();
        YangToolsUtils.printDataTree(updatedFoo);
    }

}