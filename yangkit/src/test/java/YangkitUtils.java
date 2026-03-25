import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public final class YangkitUtils {
    public static YangSchemaContext loadSchema(String yangFiles) throws DocumentException, IOException, YangParserException {
        var f = Paths.get(yangFiles).toAbsolutePath();
        return YangYinParser.parse(f.toFile());
    }

    public static JsonNode loadJson(String jsonFile){
        JsonNode data = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            data = objectMapper.readTree(new File(jsonFile));
        } catch (IOException ignored) {
        }
        return  data;
    }

    public static ValidatorResult validateSchema(YangSchemaContext schema){
        return schema.validate();
    }

    public static ValidatorResult parsingData(YangSchemaContext schema, JsonNode data){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataDocumentJsonParser(schema).parse(data, validatorResultBuilder);
        return validatorResultBuilder.build();
    }

    public static ValidatorResult validateData(YangSchemaContext schema, JsonNode data){
        ValidatorResultBuilder tmp = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataDocumentJsonParser(schema).parse(data, tmp);
        yangDataDocument.update();
        return yangDataDocument.validate();
    }
}