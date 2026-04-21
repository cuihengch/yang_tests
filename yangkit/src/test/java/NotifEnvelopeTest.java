import com.fasterxml.jackson.databind.JsonNode;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotifEnvelopeTest {

    @Test
    void testValidNotifEnvelope() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/notif");
        JsonNode validData = YangkitUtils.loadJson("../data/valid_notification.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testInvalidNotifEnvelope() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/notif");
        JsonNode validData = YangkitUtils.loadJson("../data/invalid_notification.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }
}
