import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaValidationTest {

    @Test
    void testLoadValidSchema() throws DocumentException, IOException, YangParserException {
        String yang = "../yang/example.yang";
        YangSchemaContext schema = YangkitUtils.loadSchema(yang);
        assertNotNull(schema);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schema);
        assertTrue(schemaValidation.isOk());
    }

    @Test
    void testLoadInvalidSchema() {
        assertThrows(YangParserException.class, () -> {
            String yang = "../yang/bad-example.yang";
            YangSchemaContext schema = YangkitUtils.loadSchema(yang);
        });
    }

    @Test
    void testLoadInvalidSchemaSemantics() throws DocumentException, IOException, YangParserException {
        // Tests a YANG file that is syntactically correct but has semantic errors.
        YangSchemaContext schema = YangkitUtils.loadSchema("../yang/semantic-error-example.yang");
        assertNotNull(schema);

        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schema);
        assertFalse(schemaValidation.isOk(), "Validation should fail because of semantic modeling errors");
    }
}
