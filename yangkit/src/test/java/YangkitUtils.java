import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.data.impl.model.LeafDataImpl;
import org.yangcentral.yangkit.data.impl.model.SingleInstanceDataIdentifier;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class YangkitUtils {

    public static boolean DEBUG = true;

    private static void debugValidatorResult(ValidatorResult result) {
        if (!DEBUG) return;
        if (result.getRecords() == null) return;
        if (result.getRecords().isEmpty()) {
            System.out.println("Validator result is empty");
        } else {

            for (var r : result.getRecords()) {
                if (r.getSeverity().toString().equals("ERROR")) {
                    System.out.println(r.getBadElement() + " " + r.getErrorMsg().getMessage());
                }
            }
        }
    }

    public static YangSchemaContext loadValidSchema(String yangFiles) throws DocumentException, IOException, YangParserException {
        return loadSchema(yangFiles, false);
    }

    public static YangSchemaContext loadInvalidSchema(String yangFiles) throws DocumentException, IOException, YangParserException {
        return loadSchema(yangFiles, true);
    }

    public static YangSchemaContext loadSchema(String yangFiles, boolean expectError) throws DocumentException, IOException, YangParserException {
        var f = Paths.get(yangFiles).toAbsolutePath();
        YangSchemaContext context = null;
        boolean getError;
        try {
            context = YangYinParser.parse(f.toFile());
            var validatorResult = context.validate();
            debugValidatorResult(validatorResult);
            getError = !validatorResult.isOk();
        } catch (IOException | YangParserException | DocumentException ignored) {
            getError = true;
        }

        if (!expectError) assertNotNull(context, "context is null");

        assertEquals(expectError, getError, expectError ?
                "Expected invalid schema but schema is valid" :
                "Expected valid schema but schema is not valid");


        return context;
    }

    public static YangDataDocument loadValidYangDataDoc(YangSchemaContext context, String jsonFile) throws DocumentException, IOException, YangParserException {
        return loadYangDataDoc(context, jsonFile, false, false);
    }

    public static YangDataDocument loadValidYangDataDoc(String yangFiles, String jsonFile) throws DocumentException, IOException, YangParserException {
        return loadYangDataDoc(yangFiles, jsonFile, false, false);
    }

    public static YangDataDocument loadInvalidYangDataDocParseError(YangSchemaContext context, String jsonFile) throws DocumentException, IOException, YangParserException {
        return loadYangDataDoc(context, jsonFile, true, false);
    }

    public static YangDataDocument loadInvalidYangDataDocParseError(String yangFiles, String jsonFile) throws DocumentException, IOException, YangParserException {
        return loadYangDataDoc(yangFiles, jsonFile, true, false);
    }

    public static YangDataDocument loadInvalidYangDataDocValidateError(String yangFiles, String jsonFile) throws DocumentException, IOException, YangParserException {
        return loadYangDataDoc(yangFiles, jsonFile, false, true);
    }

    public static YangDataDocument loadYangDataDoc(String yangFiles, String jsonFile,
                                                   boolean expectErrorDuringParsing,
                                                   boolean expectErrorDuringValidation)
            throws DocumentException, IOException, YangParserException {

        YangSchemaContext context = loadValidSchema(yangFiles);
        return loadYangDataDoc(context, jsonFile, expectErrorDuringParsing, expectErrorDuringValidation);

    }

    public static JsonNode loadJson(String jsonFile) {
        JsonNode data = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            data = objectMapper.readTree(new File(jsonFile));
        } catch (IOException ignored) {
        }
        assertNotNull(data, "data is null");
        return data;
    }

    public static YangDataDocument loadYangDataDoc(YangSchemaContext context, String jsonFile,
                                                   boolean expectErrorDuringParsing,
                                                   boolean expectErrorDuringValidation) {

        JsonNode data = loadJson(jsonFile);

        YangDataDocument doc = null;

        ValidatorResultBuilder parseValidatorErrorBuilder = new ValidatorResultBuilder();
        ValidatorResult parseValidatorError = null;
        boolean parseError;

        try {
            doc = new YangDataDocumentJsonParser(context).parse(data, parseValidatorErrorBuilder);
            parseValidatorError = parseValidatorErrorBuilder.build();
            parseError = !parseValidatorError.isOk();
        } catch (Exception e) {
            parseValidatorError = parseValidatorErrorBuilder.build();
            parseError = true;
        }

        debugValidatorResult(parseValidatorError);

        assertEquals(expectErrorDuringParsing, parseError, expectErrorDuringParsing ?
                "Expected error during parsing but no error" :
                "Expected no error during parsing but data is not valid");

        if (expectErrorDuringParsing) return doc;

        assertNotNull(doc, "doc is null");

        doc.update();
        ValidatorResult validateValidatorError = doc.validate();

        debugValidatorResult(validateValidatorError);
        assertEquals(expectErrorDuringValidation, !validateValidatorError.isOk(), expectErrorDuringValidation ?
                "Expected error during validation but no error" :
                "Expected no error during validation but data is not valid");

        return doc;

    }


    public static void getTree(Object node, String indent) throws YangCodecException {
        if (!(node instanceof org.yangcentral.yangkit.data.api.model.YangData<?> data)) {
            System.out.println(indent + node);
            return;
        }

        if (node instanceof LeafDataImpl<?>) {
            System.out.println(indent + data.getQName().getQualifiedName() + " -> {" + ((LeafDataImpl) node).getValue().getValue().toString() + "} (" + data.getClass() + ")");
        } else {
            System.out.println(indent + data.getQName().getQualifiedName() + " -> (" + data.getClass() + ")");
        }


        if (data instanceof org.yangcentral.yangkit.data.api.model.YangDataContainer container) {
            for (var child : container.getDataChildren()) {
                getTree(child, indent + "  ");
            }
        }
    }

    public static SingleInstanceDataIdentifier getIdentifier(String namespace, String localName) {
        QName qname = new QName(namespace, localName);
        return new SingleInstanceDataIdentifier(qname);
    }


}
