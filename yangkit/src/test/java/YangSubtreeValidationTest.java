import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.jaxen.NamespaceContext;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.NamespaceContextDom4j;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.JsonCodecUtil;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class YangSubtreeValidationTest {

    @Test
    void minimalXpathTest() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/xpath");
        JsonNode validData = YangkitUtils.loadJson("../data/xpath-test-valid.json");
        assertTrue(schemaContext.validate().isOk());

        System.out.println(validData);
        // Can't do that with XPath atm because the target is a 'container'
        var childContainer = validData.get("data").get("xpath-test:top-container");
        System.out.println(childContainer);

        var dataWrappedChildContainer = new ObjectMapper().createObjectNode();
        dataWrappedChildContainer.putIfAbsent("data", childContainer);

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

        assertTrue(build.isOk());
    }

}
