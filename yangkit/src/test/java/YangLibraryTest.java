import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import static org.junit.jupiter.api.Assertions.*;


import java.nio.file.Path;

public class YangLibraryTest {

    @Test
    void testYangLibraryWithEnabledIfFeature() throws Exception {
        YangSchemaContext bootstrapContext = YangkitUtils.loadValidSchema("../yang/yang-library");
        YangDataDocument yangLibraryDocument = YangkitUtils.loadValidYangDataDoc(bootstrapContext, "../data/yang-library/yang-library-with-feature.json");

        YangSchemaContext generatedContext =
                YangLibraryUtils.newContextFromYangLibraryData(
                        Path.of("../yang/yang-library"),
                        yangLibraryDocument
                );

        assertTrue(
                generatedContext
                        .getModule("feature-test", "2026-04-29")
                        .isPresent()
        );

        ValidatorResult generatedSchemaValidation = generatedContext.validate();
        assertTrue(generatedSchemaValidation.isOk());

        YangkitUtils.loadValidYangDataDoc(generatedContext, "../data/yang-library/valid-feature-data.json");
    }

    @Test
    void testYangLibraryWithoutIfFeatureShouldRejectFeatureLeaf() throws Exception {
        YangSchemaContext bootstrapContext = YangkitUtils.loadValidSchema("../yang/yang-library");
        YangDataDocument yangLibraryDocument = YangkitUtils.loadValidYangDataDoc(bootstrapContext, "../data/yang-library/yang-library-without-feature.json");

        YangSchemaContext generatedContext =
                YangLibraryUtils.newContextFromYangLibraryData(
                        Path.of("../yang/yang-library"),
                        yangLibraryDocument
                );

        ValidatorResult generatedSchemaValidation = generatedContext.validate();
        assertTrue(generatedSchemaValidation.isOk());

        YangkitUtils.loadInvalidYangDataDocParseError(generatedContext, "../data/yang-library/valid-feature-data.json");

    }


}
