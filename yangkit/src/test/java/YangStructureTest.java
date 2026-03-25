import com.fasterxml.jackson.databind.JsonNode;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class YangStructureTest {

    @Test
    void testValidMinimalJsonStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        JsonNode validData = YangkitUtils.loadJson("../data/scotthuang_valid_structure_minimal.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testValidJsonStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        JsonNode validData = YangkitUtils.loadJson("../data/scotthuang_valid_structure_message.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testInvalidTypeJsonStructure() throws DocumentException, IOException, YangParserException {
        assertThrows(Exception.class, ()->{
            YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
            JsonNode validData = YangkitUtils.loadJson("../data/scotthuang_invalid_structure_type.json");
            ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
            ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        });
    }

    @Test
    void testMissingMandatoryJsonStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        JsonNode validData = YangkitUtils.loadJson("../data/scotthuang_invalid_structure_missing_mandatory.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertFalse(secondDataValidation.isOk());
    }
}
