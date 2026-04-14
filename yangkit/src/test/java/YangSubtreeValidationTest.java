import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
// TODO: Heng - would be good to clean up the unused imports
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.NamespaceContextDom4j;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

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

}
