import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class YangAnydataStructureTest {

    private static JsonNode json(String text) throws IOException {
        return new ObjectMapper().readTree(text);
    }

    @Test
    void testPrimitiveTypeAnydataStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-structure");
        JsonNode validData = YangkitUtils.loadJson("../data/primitive_anydata_structure.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        // TODO: Heng - same as AnydataValidationTest, is primitive expected to have "true" validation result?
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testObjectWithSchemaAnydataStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-structure");
        JsonNode validData = YangkitUtils.loadJson("../data/object-with-schema-anydata-structure.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testObjectWithoutSchemaAnydataStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-structure");
        JsonNode validData = YangkitUtils.loadJson("../data/object-without-schema-anydata-structure.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testNullAnydataStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-structure");
        JsonNode validData = json("""
                {
                  "test-structure:message": {
                    "metadata": {
                      "timestamp": "2025-03-05T10:30:00.000Z",
                      "source": "router-01.example.com",
                      "sequence-number": 42
                    },
                    "payload": null
                  }
                }
                """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        // TODO: Heng -not sure if null should be considered as a valid anydata
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testArrayAnydataStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-structure");
        JsonNode validData = json("""
                {
                  "test-structure:message": {
                    "metadata": {
                      "timestamp": "2025-03-05T10:30:00.000Z",
                      "source": "router-01.example.com",
                      "sequence-number": 42
                    },
                    "payload": []
                  }
                }
                """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testNullObjectAnydataStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/anydata-structure");
        JsonNode validData = json("""
                {
                  "test-structure:message": {
                    "metadata": {
                      "timestamp": "2025-03-05T10:30:00.000Z",
                      "source": "router-01.example.com",
                      "sequence-number": 42
                    },
                    "payload": {}
                  }
                }
                """);
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testStructureInAnydata() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/structure-in-anydata");
        JsonNode validData = YangkitUtils.loadJson("../data/structure-in-anydata.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }
}
