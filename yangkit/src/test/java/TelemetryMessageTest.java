import com.fasterxml.jackson.databind.JsonNode;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TelemetryMessageTest {

    @Test
    void testValidTelemetryMsgNetGauze() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/telemetry",
                "../data/telemetry/valid-telemetry-msg-netgauze.json");
    }

    @Test
    void testValidTelemetryMsgPMACCT() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/telemetry",
                "../data/telemetry/valid-telemetry-msg-pmacct.json");
    }

    @Test
    void testInvalidTelemetryMsg() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/telemetry",
                "../data/telemetry/invalid-telemetry-msg.json");
    }

}
