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
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/xpath");
        JsonNode validData = YangkitUtils.loadJson("../data/xpath-test-valid.json");
        assertTrue(schemaContext.validate().isOk());

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument doc =
                new YangDataDocumentJsonParser(schemaContext)
                        .parse(validData, validatorResultBuilder);

        YangXPathImpl xpath = new YangXPathImpl("/xt:top-container/xt:child-container/xt:value1");
        xpath.addNamespace("xt", "urn:xpath:test");

        String value = xpath.stringValueOf(doc.getDataChild(YangkitUtils.getIdentifier("urn:xpath:test","top-container")));
        assertEquals("value", value);
    }

    @Test
    void anydataXpathTest() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/xpath");
        JsonNode validData = YangkitUtils.loadJson("../data/anydata-xpath-test-valid.json");
        schemaContext.validate();

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument doc =
                new YangDataDocumentJsonParser(schemaContext)
                        .parse(validData, validatorResultBuilder);

        assertTrue(validatorResultBuilder.build().isOk());
        doc.validate();

        YangXPathImpl xpath = new YangXPathImpl("/xt:anydata-container/xt:child-container/xt:payload/st:system/st:hostname");
        xpath.addNamespace("xt", "urn:xpath:test");
        xpath.addNamespace("st", "urn:schema:test");

        String value = xpath.stringValueOf(doc.getDataChild(YangkitUtils.getIdentifier("urn:xpath:test","anydata-container")));
        // TODO: Heng - "value" returns "" instead of "router1", here return false?
         assertEquals("router1", value);
    }

    @Test
    void structureXpathTest() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        JsonNode validData = YangkitUtils.loadJson("../data/scotthuang_valid_structure_message.json");
        schemaContext.validate();

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument doc =
                new YangDataDocumentJsonParser(schemaContext)
                        .parse(validData, validatorResultBuilder);

        assertTrue(validatorResultBuilder.build().isOk());
        doc.validate();

        YangXPathImpl xpath = new YangXPathImpl("/ts:payload/ts:content");
        xpath.addNamespace("ts", "urn:example:test-structure");

        String value = xpath.stringValueOf(doc.getDataChild(YangkitUtils.getIdentifier("urn:example:test-structure","payload")));
        //assertEquals("router-01.example.com", value);
        assertEquals("sample data", value);
    }

    @Test
    void nonExistentXpathTest() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        schemaContext.validate();
        JsonNode validData = json("""
            {
              "test-structure:message": {
                "metadata": {
                  "timestamp": "2025-03-05T10:30:00.000Z",
                  "source": "router-01.example.com"
                },
                "payload": {}
              }
            }
            """);

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument doc = new YangDataDocumentJsonParser(schemaContext).parse(validData, validatorResultBuilder);

        // XPath pointing to a leaf that does not exist in the data tree
        YangXPathImpl xpath = new YangXPathImpl("/ts:message/ts:metadata/ts:non-existent-leaf");
        xpath.addNamespace("ts", "urn:example:test-structure");

        YangData<?> contextNode =
                doc.getDataChild(YangkitUtils.getIdentifier("urn:example:test-structure", "payload"));
        String value = xpath.stringValueOf(contextNode);
        assertEquals("", value, "Extracting a non-existent node should return an empty string");
    }

}
