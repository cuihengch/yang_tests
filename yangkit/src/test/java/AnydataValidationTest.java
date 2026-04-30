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
        // todo: payload is primitive, invalid - in strict validation of yangkit, why is it succ?
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/anydata",
                "../data/anydata/primitive-anydata.json");
    }

    @Test
    void testObjectWithSchemaAnydata() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/anydata",
                "../data/anydata/object-with-schema-anydata.json");
    }

    @Test
    void testObjectWithoutSchemaAnydata() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/anydata",
                "../data/anydata/object-without-schema-anydata.json");
    }

    @Test
    void testPrimitiveAnydataInAnydata()throws DocumentException, IOException, YangParserException {
        // todo: payload is primitive, invalid - in strict validation of yangkit, why is it succ?
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/anydata-anydata",
                "../data/anydata-in-anydata/primitive-anydata.json");
    }

    @Test
    void testObjectWithSchemaAnydataInAnydata() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/anydata-anydata",
                "../data/anydata-in-anydata/object-with-schema-anydata-anydata.json");
    }

    @Test
    void testObjectWithoutSchemaAnydataInAnydata() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/anydata-anydata",
                "../data/anydata-in-anydata/object-without-schema-anydata-anydata.json");
    }

    @Test
    void testNullAnydata() throws Exception {
        // TODO: Heng - is this actually valid?
        //  RFC 7951 Sec.5.5: "anydata instance is encoded in the same way as a container, i.e., as a name/object pair."
        //  Additionally, "The 'null' value is only allowed in the single-element array '[null]' corresponding to the encoding of the 'empty' type".
        YangkitUtils.loadValidYangDataDoc("../yang/anydata",
                "../data/anydata/null-anydata.json");
    }

    /**
     * Tests validation of an anydata node containing an empty object.
     * This is the correct as empty anydata node per RFC 7951.
     */
    @Test
    void testEmptyObjectAnydata() throws Exception {
        YangkitUtils.loadValidYangDataDoc("../yang/anydata",
                "../data/anydata/empty-object-anydata.json");
    }

    @Test
    void testEmptyArrayAnydata() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/anydata",
                "../data/anydata/empty-array-anydata.json");
    }

    @Test
    void testPrimitiveArrayAnydata() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/anydata",
                "../data/anydata/primitive-array-anydata.json");
    }

    @Test
    void testObjectArrayAnydata() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/anydata",
                "../data/anydata/object-array-anydata.json");
    }

    /**
     * Tests that identityrefs that cross module boundaries resolve correctly when embedded inside an anydata block.
     * In JSON encoding, identityrefs take the form "module-name:identity-name", so parsing must correctly look up "module-name".
     */
    @Test
    void testAnydataCrossModuleIdentityref() throws Exception {
        YangkitUtils.loadValidYangDataDoc("../yang/anydata-identity",
                "../data/anydata-identity/cross-module-identityref-anydata.json");
    }

    /**
     * Tests that validation fails when an identityref inside an anydata block
     * refers to a module that does not exist in the schema context.
     */
    @Test
    void testAnydataCrossModuleIdentityrefMissingModule() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/anydata-identity-missing",
                "../data/anydata-identity/cross-module-identityref-missing-module-anydata.json");
    }
}
