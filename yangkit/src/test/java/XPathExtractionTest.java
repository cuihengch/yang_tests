import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

public class XPathExtractionTest {

    private JsonNode json(String rawJson) throws Exception {
        return new ObjectMapper().readTree(rawJson);
    }

    @Test
    void minimalXpathTest() throws Exception {
        YangDataDocument doc = YangkitUtils.loadValidYangDataDoc("../yang/xpath",
                "../data/xpath/xpath-test-valid.json");

        YangXPathImpl xpath = new YangXPathImpl("/xt:top-container/xt:child-container/xt:value1");
        xpath.addNamespace("xt", "urn:xpath:test");

        String value = xpath.stringValueOf(doc.getDataChild(YangkitUtils.getIdentifier("urn:xpath:test","top-container")));
        assertEquals("value", value);
    }

    @Test
    void anydataXpathTest() throws Exception {
        YangDataDocument doc = YangkitUtils.loadValidYangDataDoc("../yang/xpath",
                "../data/xpath/anydata-xpath-test-valid.json");

        YangXPathImpl xpath = new YangXPathImpl("/xt:anydata-container/xt:child-container/xt:payload/st:system/st:hostname");
        xpath.addNamespace("xt", "urn:xpath:test");
        xpath.addNamespace("st", "urn:schema:test");

        String value = xpath.stringValueOf(doc.getDataChild(YangkitUtils.getIdentifier("urn:xpath:test","anydata-container")));
        // TODO: Heng - "value" returns "" instead of "router1", here return false?
         assertEquals("router1", value);
    }

    @Test
    void structureXpathTest() throws Exception {
        YangDataDocument doc = YangkitUtils.loadValidYangDataDoc("../yang/xpath-structure",
                "../data/xpath-structure/scotthuang_valid_structure_message.json");

        YangXPathImpl xpath = new YangXPathImpl("/ts:payload/ts:content");
        xpath.addNamespace("ts", "urn:example:test-structure");

        String value = xpath.stringValueOf(doc.getDataChild(YangkitUtils.getIdentifier("urn:example:test-structure","payload")));
        //assertEquals("router-01.example.com", value);
        assertEquals("sample data", value);
    }

    @Test
    void nonExistentXpathTest() throws Exception {
        YangDataDocument doc = YangkitUtils.loadValidYangDataDoc("../yang/xpath-structure",
                "../data/xpath-structure/non-existent-xpath.json");

        // XPath pointing to a leaf that does not exist in the data tree
        YangXPathImpl xpath = new YangXPathImpl("/ts:message/ts:metadata/ts:non-existent-leaf");
        xpath.addNamespace("ts", "urn:example:test-structure");

        YangData<?> contextNode =
                doc.getDataChild(YangkitUtils.getIdentifier("urn:example:test-structure", "payload"));
        String value = xpath.stringValueOf(contextNode);
        assertEquals("", value, "Extracting a non-existent node should return an empty string");
    }

}
