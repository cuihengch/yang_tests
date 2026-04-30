import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
// TODO: Heng - would be good to clean up the unused imports
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.NamespaceContextDom4j;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.XPathStep;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.ContainerDataJsonCodec;
import org.yangcentral.yangkit.data.codec.json.JsonCodecUtil;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class YangSubtreeValidationTest {

    @Test
    void subtreeValidationTest() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadValidSchema("../yang/subtree/subtree.yang");
        assertTrue(schemaContext.validate().isOk());
        AbsolutePath absolutePath = new AbsolutePath();
        absolutePath.addStep(new XPathStep(new QName("urn:example", "root")));
        absolutePath.addStep(new XPathStep(new QName("urn:example", "content")));
        var subscription = schemaContext.getSchemaNode(absolutePath);
        ContainerDataJsonCodec codec = new ContainerDataJsonCodec((Container) subscription);
        JsonNode validData = YangkitUtils.loadJson("../data/subtree/subtree.json");
        ValidatorResultBuilder resultBuilder = new ValidatorResultBuilder();
        var containerData = codec.deserialize(validData, resultBuilder);
        containerData.setPath(new AbsolutePath());
        resultBuilder.merge(JsonCodecUtil.buildChildrenData(containerData, validData));
        resultBuilder.merge(containerData.validate());
        var result = resultBuilder.build();
        assertTrue(result.isOk());
    }

}
