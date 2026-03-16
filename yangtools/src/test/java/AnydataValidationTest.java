import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AnydataValidationTest {

    @Test
    void testAnydataValidation() throws Exception {
        List<String> schemaFile = List.of("../yang/anydata-test.yang", "../yang/example.yang");
        EffectiveModelContext schema = YangToolsUtils.loadSchema(schemaFile);
        assertNotNull(schema);

        NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        var parser = JsonParserStream.create(
                writer,
                JSONCodecFactorySupplier.RFC7951.getShared(schema)
        );

        assertDoesNotThrow(() -> {
            try (JsonReader reader = new JsonReader(
                    new InputStreamReader(
                            Files.newInputStream(Paths.get("../data/anydata.json"))
                    ))) {
                parser.parse(reader);
            }
        });
    }
}