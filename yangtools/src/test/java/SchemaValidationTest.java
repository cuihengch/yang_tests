import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaValidationTest {

    @Test
    void testLoadValidSchema() throws Exception {
        List<String> schemaFile = List.of("../yang/example.yang");
        EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        assertNotNull(schema);
    }

    @Test
    void testLoadInvalidSchema() throws Exception {
        assertThrows(Exception.class, () -> {
            List<String> schemaFile = List.of("../yang/bad-example.yang");
            EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        });
    }
}