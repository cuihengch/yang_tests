import org.dom4j.DocumentException;
import org.jaxen.JaxenException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class XPathExtractionTest {

    @Test
    void testLoadValidSchema() throws DocumentException, IOException, YangParserException, JaxenException {
        String yang = "../yang/schema-test.yang";
        YangSchemaContext schema = YangkitUtils.loadSchema(yang);
        assertNotNull(schema);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schema);
        assertTrue(schemaValidation.isOk());
        YangXPathImpl xpath = new YangXPathImpl("/system/hostname");
    }
}
