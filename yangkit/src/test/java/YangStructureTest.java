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
        YangkitUtils.loadValidYangDataDoc("../yang/scotthuang-structure",
                "../data/structure/scotthuang_valid_structure_minimal.json");
    }

    @Test
    void testValidJsonStructure() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadValidYangDataDoc("../yang/scotthuang-structure",
                "../data/structure/scotthuang_valid_structure_message.json");
    }

    @Test
    void testInvalidTypeJsonStructure() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/scotthuang-structure",
                "../data/structure/scotthuang_invalid_structure_type.json");
    }

    @Test
    void testMissingMandatoryJsonStructure() throws DocumentException, IOException, YangParserException {
        YangkitUtils.loadInvalidYangDataDocValidateError("../yang/scotthuang-structure",
                "../data/structure/scotthuang_invalid_structure_missing_mandatory.json");
    }

    @Test
    void testInvalidStructureTopLevelPrefix() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/scotthuang-structure",
                "../data/structure/scotthuang_invalid_structure_top_level_prefix.json");
    }

    @Test
    void testInvalidStructureUnknownLeafInMetadata() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/scotthuang-structure",
                "../data/structure/scotthuang_invalid_structure_unknown_leaf.json");
    }

    @Test
    void testValidStructureSequenceNumberBoundaryMax() throws Exception {
        YangkitUtils.loadValidYangDataDoc("../yang/scotthuang-structure",
                "../data/structure/scotthuang_invalid_structure_boundary_max.json");
    }

    @Test
    void testInvalidStructureSequenceNumberOutOfRange() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/scotthuang-structure",
                "../data/structure/scotthuang_invalid_structure_out_of_range.json");
    }

    @Test
    void testInvalidStructureTimestampFormat() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/scotthuang-structure",
                "../data/structure/scotthuang_invalid_structure_invalid_timestamp_format.json");
    }

    @Test
    void testInvalidStructureTimestampValue() throws Exception {
        YangkitUtils.loadInvalidYangDataDocParseError("../yang/scotthuang-structure",
                "../data/structure/scotthuang_invalid_structure_invalid_timestamp_value.json");
    }

}
