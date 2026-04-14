import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class YangStructureTest {

    private static JsonNode json(String text) throws IOException {
        return new ObjectMapper().readTree(text);
    }

    @Test
    void testValidMinimalJsonStructure() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        /* TODO: Heng - it's good to have file name the same as its yang module name
            (instead of "scotthuang-structure.yang" or similar),
            ref: https://datatracker.ietf.org/doc/html/rfc7950#section-5.2:
              "The name of the file SHOULD be of the form: module-or-submodule-name ['@' revision-date]"
         */
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
            JsonNode inValidData = YangkitUtils.loadJson("../data/scotthuang_invalid_structure_type.json");
            /* TODO: Heng - in this test sample data has
                  {
                    "timestamp": "not-a-valid-timestamp",
                    "sequence-number": "should-be-uint32"
                  }
                  which has two constraints. it would be better to have one test for timestamp rejects as bad formats,
                  and a separate test for sequence-number rejects bad integer.
             */
            ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
            ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, inValidData);
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

        boolean hasExpectedError = secondDataValidation
                .getRecords()
                .stream()
                .anyMatch(record -> {
            String details = record.getErrorMsg() != null ?
                    record.getErrorMsg().getMessage().toLowerCase() : record.toString().toLowerCase();
            return details.contains("missing") || details.contains("timestamp");
        });
        assertTrue(hasExpectedError, "Expected one of the errors mentioning 'missing mandatory timestamp'");
    }

    @Test
    void testInvalidStructureTopLevelPrefix() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        JsonNode invalidData = json("""
                {
                  "test-structure:wrong-message": {
                    "metadata": {
                      "timestamp": "2025-03-05T10:30:00.000Z"
                    }
                  }
                }
                """);
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, invalidData);
        assertFalse(firstDataValidation.isOk());

        boolean hasExpectedError = firstDataValidation
                .getRecords()
                .stream()
                .anyMatch(record -> {
            String details = record.getErrorMsg() != null ?
                    record.getErrorMsg().getMessage().toLowerCase() : record.toString().toLowerCase();
            return details.contains("wrong-message") || details.contains("unknown") || details.contains("unrecognized");
        });
        assertTrue(hasExpectedError, "Expected one of the errors mentioning the unknown structure name");
    }

    @Test
    void testInvalidStructureUnknownLeafInMetadata() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        JsonNode invalidData = json("""
                {
                  "test-structure:message": {
                    "metadata": {
                      "timestamp": "2025-03-05T10:30:00.000Z",
                      "unknown": "unexpected"
                    }
                  }
                }
                """);
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, invalidData);
        assertFalse(firstDataValidation.isOk());

        boolean hasExpectedError = firstDataValidation
                .getRecords()
                .stream()
                .anyMatch(record -> {
            String details = record.getErrorMsg() != null ?
                    record.getErrorMsg().getMessage().toLowerCase() : record.toString().toLowerCase();
            return details.contains("unknown") || details.contains("unrecognized");
        });
        assertTrue(hasExpectedError, "Expected one of the errors mentioning the unknown leaf");
    }

    @Test
    void testValidStructureSequenceNumberBoundaryMax() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        JsonNode validData = json("""
                {
                  "test-structure:message": {
                    "metadata": {
                      "timestamp": "2025-03-05T10:30:00.000Z",
                      "sequence-number": 4294967295
                    }
                  }
                }
                """);
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testInvalidStructureSequenceNumberOutOfRange() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        JsonNode invalidData = json("""
                {
                  "test-structure:message": {
                    "metadata": {
                      "timestamp": "2025-03-05T10:30:00.000Z",
                      "sequence-number": 4294967296
                    }
                  }
                }
                """);
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, invalidData);
        assertFalse(firstDataValidation.isOk());

        boolean hasExpectedError = firstDataValidation
                .getRecords()
                .stream()
                .anyMatch(record -> {
            String details = record.getErrorMsg() != null ?
                    record.getErrorMsg().getMessage().toLowerCase() : record.toString().toLowerCase();
            return details.contains("sequence-number") || details.contains("range") || details.contains("invalid");
        });
        assertTrue(hasExpectedError, "Expected one of the errors mentioning 'sequence-number' out of range");
    }

    // TODO: Heng - feel free to test any other invalid or valid timestamp format
    @Test
    void testInvalidStructureTimestampFormat() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        JsonNode invalidFormatData = json("""
                {
                  "test-structure:message": {
                    "metadata": {
                      "timestamp": "2025-03-05 10:30:00.000Z"
                    }
                  }
                }
                """);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            YangkitUtils.parsingData(schemaContext, invalidFormatData);
        });

        String details = exception.getMessage();
        if (exception.getCause() != null) {
            details += " " + exception.getCause().getMessage();
        }
        
        final String finalDetails = details != null ? details.toLowerCase() : "";
        assertTrue(finalDetails.contains("invalid"), "Expected error details to mention invalid 'timestamp' format");
    }

    @Test
    void testInvalidStructureTimestampValue() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/scotthuang-structure");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        JsonNode invalidDayData = json("""
                {
                  "test-structure:message": {
                    "metadata": {
                      "timestamp": "2025-03-70T10:30:00.000Z"
                    }
                  }
                }
                """);
                
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            YangkitUtils.parsingData(schemaContext, invalidDayData);
        });

        String details = exception.getMessage();
        if (exception.getCause() != null) {
            details += " " + exception.getCause().getMessage();
        }
        
        final String finalDetails = details != null ? details.toLowerCase() : "";
        assertTrue(finalDetails.contains("invalid"), "Expected one of the errors mentioning 'timestamp' with invalid value");
    }
}
