import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
// TODO: Heng - would be good to clean up the unused imports
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.NamespaceContextDom4j;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.XPathStep;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.ContainerDataJsonCodec;
import org.yangcentral.yangkit.data.codec.json.JsonCodecUtil;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class YangSubtreeValidationTest {

    @Test
    void minimalSubtreeXpathTest() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/xpath");
        JsonNode validData = YangkitUtils.loadJson("../data/xpath-test-valid.json");
        assertTrue(schemaContext.validate().isOk());

        System.out.println(validData);
        // Can't do that with XPath atm because the target is a 'container'
        var childContainer = validData.get("data").get("xpath-test:top-container");
        System.out.println(childContainer);

        var dataWrappedChildContainer = new ObjectMapper().createObjectNode();
        dataWrappedChildContainer.putIfAbsent("data", childContainer);
        /* TODO: Heng - dataWrappedChildContainer is like
            {
            "data": {
                "xpath-test:child-container": {
                       "xpath-test:value1":"value",
                       "xpath-test:value2":42
                }
             }
            }
            and last assert will be failed.
        */

        System.out.println(dataWrappedChildContainer);

        var namespaces = new NamespaceContextDom4j();
        namespaces.addPrefixNSPair("xt", "urn:xpath:test");
        var schemaNode = schemaContext.getSchemaNode(AbsolutePath.parse("/xt:top-container/xt:child-container", namespaces, URI.create("urn:xpath:test")));
        System.out.println(schemaNode);

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();

        YangDataDocument doc =
                new YangDataDocumentJsonParser(schemaContext)
                        .parse(dataWrappedChildContainer, validatorResultBuilder);

        var build = validatorResultBuilder.build();
        System.out.println(build);

        YangXPathImpl xpath = new YangXPathImpl("/xt:top-container/xt:child-container");
        xpath.addNamespace("xt", "urn:xpath:test");

        // TODO: Heng - assert is false, it seems it still needs top-container as root to be true.
        assertTrue(build.isOk());
    }

    @Test
    void subtreeValidationTest() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/subtree.yang");
        assertTrue(schemaContext.validate().isOk());
        AbsolutePath absolutePath = new AbsolutePath();
        absolutePath.addStep(new XPathStep(new QName("urn:example", "root")));
        absolutePath.addStep(new XPathStep(new QName("urn:example", "content")));
        var subscription = schemaContext.getSchemaNode(absolutePath);
        ContainerDataJsonCodec codec = new ContainerDataJsonCodec((Container) subscription);
        JsonNode validData = YangkitUtils.loadJson("../data/subtree.json");
        ValidatorResultBuilder resultBuilder = new ValidatorResultBuilder();
        var containerData = codec.deserialize(validData, resultBuilder);
        containerData.setPath(new AbsolutePath());
        resultBuilder.merge(JsonCodecUtil.buildChildrenData(containerData, validData));
        resultBuilder.merge(containerData.validate());
        var result = resultBuilder.build();
        assertTrue(result.isOk());
    }

}
