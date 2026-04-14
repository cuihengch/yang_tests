import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnydataValidationTest {

    private JsonNode json(String rawJson) throws Exception {
        return new ObjectMapper().readTree(rawJson);
    }

    @Test
    void testPrimitiveTypeAnydata() throws DocumentException, IOException, YangParserException {
        /* TODO: Heng - if payload is primitive, isn't it not satisfying the RFC? and should be asserted as false?
            ref: https://www.rfc-editor.org/rfc/rfc7951.html#section-5.5: "It is valid I-JSON"
         */
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata");
        JsonNode validData = YangkitUtils.loadJson("../data/primitive-anydata.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testObjectWithSchemaAnydata() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata");
        JsonNode validData = YangkitUtils.loadJson("../data/object-with-schema-anydata.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testObjectWithoutSchemaAnydata() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata");
        JsonNode validData = YangkitUtils.loadJson("../data/object-without-schema-anydata.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testPrimitiveAnydataInAnydata()throws DocumentException, IOException, YangParserException {
        // todo: payload is primitive, invalid - in strict validation of yangkit, why is it succ?
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-anydata");
        JsonNode validData = YangkitUtils.loadJson("../data/primitive-anydata-anydata.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testObjectWithSchemaAnydataInAnydata() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-anydata");
        JsonNode validData = YangkitUtils.loadJson("../data/object-with-schema-anydata-anydata.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testObjectWithoutSchemaAnydataInAnydata() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-anydata");
        JsonNode validData = YangkitUtils.loadJson("../data/object-without-schema-anydata-anydata.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testNullAnydata() throws Exception {
        // TODO: Heng - is this actually valid?
        //  RFC 7951 Sec.5.5: "anydata instance is encoded in the same way as a container, i.e., as a name/object pair."
        //  Additionally, "The 'null' value is only allowed in the single-element array '[null]' corresponding to the encoding of the 'empty' type".
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata");
        JsonNode validData = json("""
            {
              "data": {
                "anydata-example:super-container": {
                  "anydata-example:payload": null
                }
              }
            }
            """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    /**
     * Tests validation of an anydata node containing an empty object.
     * This is the correct as empty anydata node per RFC 7951.
     */
    @Test
    void testEmptyObjectAnydata() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata");
        JsonNode validData = json("""
            {
              "data": {
                "anydata-example:super-container": {
                  "anydata-example:payload": {}
                }
              }
            }
            """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testEmptyArrayAnydata() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata");
        JsonNode validData = json("""
            {
              "data": {
                "anydata-example:super-container": {
                  "anydata-example:payload": []
                }
              }
            }
            """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testPrimitiveArrayAnydata() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata");
        JsonNode validData = json("""
            {
              "data": {
                "anydata-example:super-container": {
                  "anydata-example:payload": [1, 2, 3]
                }
              }
            }
            """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testObjectArrayAnydata() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata");
        JsonNode validData = json("""
            {
              "data": {
                "anydata-example:super-container": {
                  "anydata-example:payload": [{"a": 1}, {"b": 2}]
                }
              }
            }
            """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    /**
     * Tests that identityrefs that cross module boundaries resolve correctly when embedded inside an anydata block.
     * In JSON encoding, identityrefs take the form "module-name:identity-name", so parsing must correctly look up "module-name".
     */
    @Test
    void testAnydataCrossModuleIdentityref() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-identity");
        JsonNode validData = json("""
            {
              "anydata-test:root": {
                "payload": {
                  "target-module:type-value": "ext-ident:specific-ident"
                }
              }
            }
            """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    /**
     * Tests that validation fails when an identityref inside an anydata block
     * refers to a module that does not exist in the schema context.
     */
    @Test
    void testAnydataCrossModuleIdentityrefMissingModule() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-identity-missing");
        JsonNode invalidData = json("""
            {
              "anydata-test:root": {
                "payload": {
                  "target-module:type-value": "non-existed-ext-ident:specific-ident"
                }
              }
            }
            """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, invalidData);
        assertTrue(firstDataValidation.isOk(), "Data parsing should succeed since structural types match");

        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, invalidData);
        // TODO: Heng - why it's true? shouldn't it be false?
        //  The validation fail because the identityref refers to a module that is not existed.
        assertFalse(secondDataValidation.isOk(), "Validation should fail because non-existing-ext-ident module is missing");
    }
}
