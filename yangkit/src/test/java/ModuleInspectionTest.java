import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleInspectionTest {

    @Test
    void testLoadValidSchema() throws DocumentException, IOException, YangParserException {
        String yang = "../yang/schema-test.yang";
        YangSchemaContext schema = YangkitUtils.loadSchema(yang);
        assertNotNull(schema);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schema);
        assertTrue(schemaValidation.isOk());
        String namespace = schema.getModule("schema-test").getFirst().getMainModule().getNamespace().getArgStr();
        assertEquals("urn:schema:test", namespace);
    }
}
