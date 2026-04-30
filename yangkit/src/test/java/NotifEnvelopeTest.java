import com.fasterxml.jackson.databind.JsonNode;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class NotifEnvelopeTest {

    @Test
    void testValidNotifEnvelope() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/notif",
                "../data/notif/valid-notification.json");
    }

    @Test
    void testInvalidNotifEnvelope() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/notif",
                "../data/notif/invalid-notification.json");
    }
}
