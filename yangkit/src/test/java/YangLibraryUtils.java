import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.ModuleSet;
import org.yangcentral.yangkit.model.api.schema.YangModuleDescription;
import org.yangcentral.yangkit.model.api.schema.YangSchema;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.register.YangStatementImplRegister;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class YangLibraryUtils {

    private YangLibraryUtils() {
    }

    public static YangSchemaContext newContextFromYangLibraryData(
            Path searchDir,
            YangDataDocument ylibTree
    ) throws DocumentException, YangParserException, IOException {

        YangStatementImplRegister.registerImpl();

        YangSchemaContext ctx = YangYinParser.parse(searchDir.toString());

        List<ModuleSpec> implementedModules = extractModuleSpecs(ylibTree);

        YangSchema yangSchema = new YangSchema();
        yangSchema.setName("schema-from-yang-library");

        ModuleSet moduleSet = new ModuleSet();
        moduleSet.setName("implemented-modules");
        yangSchema.addModuleSet(moduleSet);

        Set<ModuleId> implementedIds = new HashSet<>();

        for (ModuleSpec spec : implementedModules) {
            Optional<Module> moduleOpt = ctx.getModule(spec.name, spec.revision);

            if (!moduleOpt.isPresent()) {
                throw new IllegalArgumentException(
                        "Module not found in search directory: name="
                                + spec.name
                                + ", revision="
                                + spec.revision
                );
            }

            Module module = moduleOpt.get();
            implementedIds.add(module.getModuleId());

            YangModuleDescription description =
                    new YangModuleDescription(module.getModuleId());

            for (String feature : spec.features) {
                description.addFeature(feature);
            }

            moduleSet.addModule(description);
        }

        ctx.setYangSchema(yangSchema);

        List<Module> allModules = new ArrayList<>(ctx.getModules());

        for (Module module : allModules) {
            if (!implementedIds.contains(module.getModuleId())) {
                ctx.removeModule(module.getModuleId());
                ctx.addImportOnlyModule(module);
            }
        }

        ValidatorResult result = ctx.validate();

        if (!result.isOk()) {
            throw new IllegalStateException(
                    "Invalid Yangkit schema context: " + result.getRecords()
            );
        }

        return ctx;
    }

    private static List<ModuleSpec> extractModuleSpecs(YangDataDocument ylibTree) {
        List<YangData> moduleNodes = new ArrayList<>();
        collectNodesByLocalName(ylibTree, "module", moduleNodes);

        List<ModuleSpec> specs = new ArrayList<>();

        for (YangData moduleNode : moduleNodes) {
            if (!(moduleNode instanceof YangDataContainer)) {
                continue;
            }

            YangDataContainer moduleContainer = (YangDataContainer) moduleNode;

            String name = getSingleStringChild(moduleContainer, "name");
            String revision = getSingleStringChild(moduleContainer, "revision");
            List<String> features = getStringChildren(moduleContainer, "feature");

            if (name == null || name.isBlank()) {
                continue;
            }

            if (revision != null && revision.isBlank()) {
                revision = null;
            }

            specs.add(new ModuleSpec(name, revision, features));
        }

        return specs;
    }

    private static void collectNodesByLocalName(
            YangDataContainer container,
            String localName,
            List<YangData> result
    ) {
        for (Object childObj : container.getDataChildren()) {
            YangData child = (YangData) childObj;

            if (localName.equals(child.getQName().getLocalName())) {
                result.add(child);
            }

            if (child instanceof YangDataContainer) {
                collectNodesByLocalName(
                        (YangDataContainer) child,
                        localName,
                        result
                );
            }
        }
    }

    private static String getSingleStringChild(
            YangDataContainer container,
            String childName
    ) {
        List children = container.getDataChildren(childName);

        if (children == null || children.isEmpty()) {
            return null;
        }

        YangData child = (YangData) children.get(0);

        if (!(child instanceof TypedData)) {
            return null;
        }

        return ((TypedData) child).getStringValue();
    }

    private static List<String> getStringChildren(
            YangDataContainer container,
            String childName
    ) {
        List<String> values = new ArrayList<>();
        List children = container.getDataChildren(childName);

        if (children == null) {
            return values;
        }

        for (Object childObj : children) {
            YangData child = (YangData) childObj;

            if (child instanceof TypedData) {
                values.add(((TypedData) child).getStringValue());
            }
        }

        return values;
    }

    private static final class ModuleSpec {
        private final String name;
        private final String revision;
        private final List<String> features;

        private ModuleSpec(String name, String revision, List<String> features) {
            this.name = name;
            this.revision = revision;
            this.features = features;
        }
    }
}