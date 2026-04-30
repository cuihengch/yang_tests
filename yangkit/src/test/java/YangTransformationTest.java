import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.NamespaceContextDom4j;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.XPathStep;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.data.impl.model.YangDataValueStringImpl;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.impl.stmt.LeafImpl;
import org.yangcentral.yangkit.model.impl.stmt.TypeImpl;
import org.yangcentral.yangkit.model.impl.stmt.TypedDataNodeImpl;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class YangTransformationTest {

    @Test
    void addValueTest() throws Exception {
        YangSchemaContext context = YangkitUtils.loadValidSchema("../yang/yang-transformation");
        YangDataDocument doc = YangkitUtils.loadValidYangDataDoc(context,
                "../data/yang-transformation/yang_transformation.json");

        AbsolutePath absolutePath = new AbsolutePath();
        absolutePath.addStep(new XPathStep(new QName("urn:yang:transformation","foo")));
        absolutePath.addStep(new XPathStep(new QName("urn:yang:transformation","num")));
        var leaf = doc.getSchemaContext().getSchemaNode(absolutePath);

        var schemaNode = new LeafImpl("num");
        schemaNode.setContext(context.getModules().getFirst().getContext());

        var type = ((TypedDataNodeImpl)leaf).getType();
        schemaNode.setType(type);

        var leafNode = new LeafDataImpl<Integer>(schemaNode);

        var data = new YangDataValueStringImpl<Integer>(schemaNode, "3");
        leafNode.setValue(data);

        System.out.println("=== Parsed JSON ===");
        YangkitUtils.getTree(doc.getDataChildren().getFirst()," ");

        var container = (ContainerDataImpl) doc.getDataChild(YangkitUtils.getIdentifier("urn:yang:transformation", "foo"));

        container.addChild(leafNode);

        YangXPathImpl xpath = new YangXPathImpl("/yt:foo/yt:num");
        xpath.addNamespace("yt", "urn:yang:transformation");

        String value = xpath.stringValueOf(doc.getDataChild(YangkitUtils.getIdentifier("urn:yang:transformation", "foo")));
        assertEquals("3", value);

        System.out.println("=== DataTree after update ===");
        YangkitUtils.getTree(doc.getDataChildren().getFirst()," ");
    }

    @Test
    void updateValueTest() throws Exception {
        YangSchemaContext context = YangkitUtils.loadValidSchema("../yang/yang-transformation");
        YangDataDocument doc = YangkitUtils.loadValidYangDataDoc(context,
                "../data/yang-transformation/yang_transformation.json");

        AbsolutePath absolutePath = new AbsolutePath();
        absolutePath.addStep(new XPathStep(new QName("urn:yang:transformation","foo")));
        absolutePath.addStep(new XPathStep(new QName("urn:yang:transformation","value-to-update")));
        var leaf = doc.getSchemaContext().getSchemaNode(absolutePath);

        var schemaNode = new LeafImpl("value-to-update");
        schemaNode.setContext(context.getModules().getFirst().getContext());

        var type = ((TypedDataNodeImpl)leaf).getType();
        schemaNode.setType(type);

        var leafNode = new LeafDataImpl<Integer>(schemaNode);

        var data = new YangDataValueStringImpl<Integer>(schemaNode, "3");
        leafNode.setValue(data);

        System.out.println("=== Parsed JSON ===");
        YangkitUtils.getTree(doc.getDataChildren().getFirst()," ");

        var container = (ContainerDataImpl) doc.getDataChild(YangkitUtils.getIdentifier("urn:yang:transformation", "foo"));

        container.addChild(leafNode);

        YangXPathImpl xpath = new YangXPathImpl("/yt:foo/yt:value-to-update");
        xpath.addNamespace("yt", "urn:yang:transformation");

        String value = xpath.stringValueOf(doc.getDataChild(YangkitUtils.getIdentifier("urn:yang:transformation", "foo")));
        assertEquals("3", value);

        System.out.println("=== DataTree after update ===");
        YangkitUtils.getTree(doc.getDataChildren().getFirst()," ");
    }


}
