import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YangkitSchemaComparatorTest {

    @Test
    void compareTwoYangSchemas() throws Exception {
        YangSchemaContext oldContext = YangkitUtils.loadValidSchema("../yang/schema-comparison/old");
        YangSchemaContext newContext = YangkitUtils.loadValidSchema("../yang/schema-comparison/new");

        assertTrue(oldContext.validate().isOk());
        assertTrue(newContext.validate().isOk());

        List<YangkitSchemaComparator.SchemaChange> changes =
                YangkitSchemaComparator.compare(oldContext, newContext);

        changes.forEach(System.out::println);

        assertFalse(changes.isEmpty());
    }
}