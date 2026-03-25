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
        assertThrows(Exception.class, () -> {
            String yang = "../yang/bad-example.yang";
            YangSchemaContext schema = YangkitUtils.loadSchema(yang);
        });
    }
}
