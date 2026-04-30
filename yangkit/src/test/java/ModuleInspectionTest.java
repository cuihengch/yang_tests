import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.parser.YangParser;
import org.yangcentral.yangkit.parser.YangParserEnv;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.register.YangStatementImplRegister;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

// TODO: Heng - there're unused imports, it would be great to clean them up.
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleInspectionTest {

    @Test
    void testInspectModuleVersion() throws IOException, YangParserException {
        YangStatementImplRegister.registerImpl();

        String yang = "../yang/schema-validation/valid-schema-test.yang";

        // Initialize a parser like it's done in YinYangParser.parse
        YangParser parser = new YangParser();
        Path path = Path.of(yang);
        String content = Files.readString(path);
        YangParserEnv env = new YangParserEnv();
        env.setCurPos(0);
        env.setFilename(path.getFileName().toString());
        env.setYangStr(content);

        // Walk the AST
        List<YangElement> elements = parser.parseYang(content, env);
        for (YangElement el: elements) {
            if (el instanceof Module module) {
                YangStatement version = module.getSubStatement(YangBuiltinKeyword.YANGVERSION.getQName()).getFirst();
                assertEquals("1.1", version.getArgStr());
            }
        }
    }
}
