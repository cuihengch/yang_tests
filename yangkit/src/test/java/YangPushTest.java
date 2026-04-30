import com.fasterxml.jackson.databind.JsonNode;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class YangPushTest {

    @Test
    void test1ValidPushUpdate() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/yangpush",
                "../data/yangpush/1-push-update.json");
    }

    @Test
    void test1ValidSubscriptionStarted() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/yangpush",
                "../data/yangpush/1-subscription-started.json");
    }

    @Test
    void test2ValidPushUpdate() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/yangpush",
                "../data/yangpush/2-push-update.json");
    }

    @Test
    void test2ValidSubscriptionStarted() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/yangpush",
                "../data/yangpush/2-subscription-started.json");
    }


}
