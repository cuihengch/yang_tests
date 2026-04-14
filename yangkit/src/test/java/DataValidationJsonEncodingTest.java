import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataValidationJsonEncodingTest {

    private JsonNode json(String rawJson) throws Exception {
        return new ObjectMapper().readTree(rawJson);
    }

    /**
     * Heng:
     *     1. for these two test cases, they are covering the case of port as
     *     numeric v.s. string. it might be also good to test the case for
     *     hostname as string v.s. numeric.
     *     2. for port 70000, it's only tested for string v.s. numeric, it would
     *     be also useful to test 70000 port as numeric to see whether it's
     *     violate its bounds, e.g. max as 65535 (2^16 - 1)
     * two more testcases are added below
      */

    @Test
    void testValidJsonValidation() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/schema-test.yang");
        JsonNode validData = YangkitUtils.loadJson("../data/valid.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, validData);
        assertTrue(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, validData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testInvalidJsonValidation() throws DocumentException, IOException, YangParserException {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/schema-test.yang");
        JsonNode invalidData = YangkitUtils.loadJson("../data/invalid.json");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());
        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, invalidData);
        assertFalse(firstDataValidation.isOk());
        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, invalidData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testHostnameNumericValidation() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/schema-test.yang");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        JsonNode invalidData = json("""
            {
              "schema-test:system": {
                "hostname": 12345,
                "port": 830
              }
            }
            """);

        ValidatorResult firstDataValidation = YangkitUtils.parsingData(schemaContext, invalidData);
        assertFalse(firstDataValidation.isOk());

        ValidatorResult secondDataValidation = YangkitUtils.validateData(schemaContext, invalidData);
        assertTrue(secondDataValidation.isOk());
    }

    @Test
    void testPortOutOfBoundsValidation() throws Exception {
        YangSchemaContext schemaContext = YangkitUtils.loadSchema("../yang/schema-test.yang");
        ValidatorResult schemaValidation = YangkitUtils.validateSchema(schemaContext);
        assertTrue(schemaValidation.isOk());

        JsonNode invalidData = json("""
            {
              "schema-test:system": {
                "hostname": "router1",
                "port": 70000
              }
            }
            """);

        // TODO: Heng - is it better to detect it from firstDataValidation.isOk() == false?
        assertThrows(Exception.class, () -> {
            YangkitUtils.parsingData(schemaContext, invalidData);
        }, "Port 70000 out of bounds should throw an exception during parsing");
    }

}
