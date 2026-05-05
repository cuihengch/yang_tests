import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Entity;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.MaxElements;
import org.yangcentral.yangkit.model.api.stmt.MinElements;
import org.yangcentral.yangkit.model.api.stmt.MultiInstancesDataNode;
import org.yangcentral.yangkit.model.api.stmt.Must;
import org.yangcentral.yangkit.model.api.stmt.MustSupport;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.api.stmt.When;
import org.yangcentral.yangkit.model.api.stmt.WhenSupport;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class YangkitSchemaComparator {

    private YangkitSchemaComparator() {
    }

    public enum ChangeKind {
        NODE_ADDED,
        NODE_DELETED,
        NODE_CHANGED
    }

    public enum Compatibility {
        BACKWARD_COMPATIBLE,
        NON_BACKWARD_COMPATIBLE,
        UNKNOWN
    }

    public record SchemaChange(
            ChangeKind kind,
            String path,
            String property,
            String oldValue,
            String newValue,
            Compatibility compatibility
    ) {
        @Override
        public String toString() {
            return kind +
                    " | " + compatibility +
                    " | " + path +
                    " | " + property +
                    " | old=" + oldValue +
                    " | new=" + newValue;
        }
    }

    public static List<SchemaChange> compare(YangSchemaContext oldContext,
                                             YangSchemaContext newContext) {
        Map<String, SchemaNode> oldNodes = indexEffectiveSchemaTree(oldContext);
        Map<String, SchemaNode> newNodes = indexEffectiveSchemaTree(newContext);

        TreeSet<String> allPaths = new TreeSet<>();
        allPaths.addAll(oldNodes.keySet());
        allPaths.addAll(newNodes.keySet());

        List<SchemaChange> changes = new ArrayList<>();

        for (String path : allPaths) {
            SchemaNode oldNode = oldNodes.get(path);
            SchemaNode newNode = newNodes.get(path);

            if (oldNode == null) {
                changes.add(new SchemaChange(
                        ChangeKind.NODE_ADDED,
                        path,
                        "node",
                        null,
                        nodeSummary(newNode),
                        compatibilityForAddedNode(newNode)
                ));
                continue;
            }

            if (newNode == null) {
                changes.add(new SchemaChange(
                        ChangeKind.NODE_DELETED,
                        path,
                        "node",
                        nodeSummary(oldNode),
                        null,
                        Compatibility.NON_BACKWARD_COMPATIBLE
                ));
                continue;
            }

            compareNode(path, oldNode, newNode, changes);
        }

        return changes;
    }

    private static void compareNode(String path,
                                    SchemaNode oldNode,
                                    SchemaNode newNode,
                                    List<SchemaChange> changes) {
        compareProperty(
                path,
                "statement",
                statementKeyword(oldNode),
                statementKeyword(newNode),
                Compatibility.NON_BACKWARD_COMPATIBLE,
                changes
        );

        compareProperty(
                path,
                "config",
                String.valueOf(oldNode.isConfig()),
                String.valueOf(newNode.isConfig()),
                compatibilityForConfigChange(oldNode.isConfig(), newNode.isConfig()),
                changes
        );

        compareProperty(
                path,
                "mandatory",
                String.valueOf(oldNode.isMandatory()),
                String.valueOf(newNode.isMandatory()),
                compatibilityForMandatoryChange(oldNode.isMandatory(), newNode.isMandatory()),
                changes
        );

        compareProperty(
                path,
                "status",
                statusOf(oldNode),
                statusOf(newNode),
                Compatibility.BACKWARD_COMPATIBLE,
                changes
        );

        compareProperty(
                path,
                "type",
                typeSignature(oldNode),
                typeSignature(newNode),
                Compatibility.NON_BACKWARD_COMPATIBLE,
                changes
        );

        compareProperty(
                path,
                "default",
                defaultValue(oldNode),
                defaultValue(newNode),
                Compatibility.UNKNOWN,
                changes
        );

        compareProperty(
                path,
                "min-elements",
                minElements(oldNode),
                minElements(newNode),
                compatibilityForMinElementsChange(minElementsInt(oldNode), minElementsInt(newNode)),
                changes
        );

        compareProperty(
                path,
                "max-elements",
                maxElements(oldNode),
                maxElements(newNode),
                compatibilityForMaxElementsChange(maxElementsInt(oldNode), maxElementsInt(newNode)),
                changes
        );

        compareProperty(
                path,
                "presence",
                presence(oldNode),
                presence(newNode),
                Compatibility.NON_BACKWARD_COMPATIBLE,
                changes
        );

        compareProperty(
                path,
                "when",
                whenCondition(oldNode),
                whenCondition(newNode),
                Compatibility.NON_BACKWARD_COMPATIBLE,
                changes
        );

        compareProperty(
                path,
                "must",
                mustConditions(oldNode),
                mustConditions(newNode),
                Compatibility.NON_BACKWARD_COMPATIBLE,
                changes
        );
    }

    private static void compareProperty(String path,
                                        String property,
                                        String oldValue,
                                        String newValue,
                                        Compatibility compatibility,
                                        List<SchemaChange> changes) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(new SchemaChange(
                    ChangeKind.NODE_CHANGED,
                    path,
                    property,
                    oldValue,
                    newValue,
                    compatibility
            ));
        }
    }

    private static Compatibility compatibilityForAddedNode(SchemaNode node) {
        if (node.isMandatory()) {
            return Compatibility.NON_BACKWARD_COMPATIBLE;
        }

        if (node instanceof MultiInstancesDataNode multi) {
            MinElements min = multi.getMinElements();
            if (min != null && min.getValue() != null && min.getValue() > 0) {
                return Compatibility.NON_BACKWARD_COMPATIBLE;
            }
        }

        return Compatibility.BACKWARD_COMPATIBLE;
    }

    private static Compatibility compatibilityForMandatoryChange(boolean oldValue, boolean newValue) {
        if (!oldValue && newValue) {
            return Compatibility.NON_BACKWARD_COMPATIBLE;
        }

        return Compatibility.BACKWARD_COMPATIBLE;
    }

    private static Compatibility compatibilityForConfigChange(boolean oldValue, boolean newValue) {
        if (oldValue && !newValue) {
            return Compatibility.NON_BACKWARD_COMPATIBLE;
        }

        return Compatibility.BACKWARD_COMPATIBLE;
    }

    private static Compatibility compatibilityForMinElementsChange(Integer oldValue, Integer newValue) {
        if (oldValue == null || newValue == null) {
            return Compatibility.UNKNOWN;
        }

        if (newValue > oldValue) {
            return Compatibility.NON_BACKWARD_COMPATIBLE;
        }

        return Compatibility.BACKWARD_COMPATIBLE;
    }

    private static Compatibility compatibilityForMaxElementsChange(Integer oldValue, Integer newValue) {
        if (oldValue == null || newValue == null) {
            return Compatibility.UNKNOWN;
        }

        if (newValue < oldValue) {
            return Compatibility.NON_BACKWARD_COMPATIBLE;
        }

        return Compatibility.BACKWARD_COMPATIBLE;
    }

    private static Map<String, SchemaNode> indexEffectiveSchemaTree(YangSchemaContext context) {
        Map<String, SchemaNode> result = new LinkedHashMap<>();
        visitContainer(context, "", result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void visitContainer(SchemaNodeContainer container,
                                       String parentPath,
                                       Map<String, SchemaNode> result) {
        List<SchemaNode> children = container.getEffectiveSchemaNodeChildren(false);

        children.stream()
                .filter(Objects::nonNull)
                .filter(SchemaNode::isActive)
                .sorted(Comparator.comparing(YangkitSchemaComparator::nodeSegment))
                .forEach(child -> {
                    String path = parentPath + "/" + nodeSegment(child);
                    result.put(path, child);

                    if (child instanceof SchemaNodeContainer childContainer) {
                        visitContainer(childContainer, path, result);
                    }
                });
    }

    private static String nodeSegment(SchemaNode node) {
        QName qName = node.getIdentifier();
        return "{" + qName.getNamespace() + "}" + qName.getLocalName();
    }

    private static String nodeSummary(SchemaNode node) {
        return statementKeyword(node) +
                " config=" + node.isConfig() +
                " mandatory=" + node.isMandatory() +
                " type=" + typeSignature(node);
    }

    private static String statementKeyword(SchemaNode node) {
        if (node == null || node.getYangKeyword() == null) {
            return null;
        }

        return node.getYangKeyword().getLocalName();
    }

    private static String statusOf(SchemaNode node) {
        if (node instanceof Entity entity && entity.getEffectiveStatus() != null) {
            return entity.getEffectiveStatus().getStatus();
        }

        return "current";
    }

    private static String typeSignature(SchemaNode node) {
        if (!(node instanceof TypedDataNode typedDataNode)) {
            return null;
        }

        Type type = typedDataNode.getType();
        if (type == null) {
            return null;
        }

        Type builtin = type.getBuiltinType();
        Type base = type.getBaseType();

        String typeName = safeArg(builtin != null ? builtin : type);
        String baseName = safeArg(base);

        String restriction = type.getRestriction() == null
                ? null
                : type.getRestriction().toString();

        return "type=" + typeName +
                ";base=" + baseName +
                ";restriction=" + restriction;
    }

    private static String defaultValue(SchemaNode node) {
        if (node instanceof Leaf leaf) {
            return safeArg(leaf.getEffectiveDefault());
        }

        if (node instanceof LeafList leafList) {
            if (leafList.getEffectiveDefaults() == null) {
                return null;
            }

            return ((List<?>) leafList.getEffectiveDefaults())
                    .stream()
                    .map(value -> safeArg((YangStatement) value))
                    .sorted()
                    .collect(Collectors.joining(","));
        }

        return null;
    }

    private static String minElements(SchemaNode node) {
        Integer value = minElementsInt(node);
        return value == null ? null : String.valueOf(value);
    }

    private static Integer minElementsInt(SchemaNode node) {
        if (!(node instanceof MultiInstancesDataNode multi)) {
            return null;
        }

        MinElements min = multi.getMinElements();
        if (min == null || min.getValue() == null) {
            return 0;
        }

        return min.getValue();
    }

    private static String maxElements(SchemaNode node) {
        Integer value = maxElementsInt(node);
        if (value == null) {
            return null;
        }

        if (value == Integer.MAX_VALUE) {
            return "unbounded";
        }

        return String.valueOf(value);
    }

    private static Integer maxElementsInt(SchemaNode node) {
        if (!(node instanceof MultiInstancesDataNode multi)) {
            return null;
        }

        MaxElements max = multi.getMaxElements();
        if (max == null || max.isUnbounded()) {
            return Integer.MAX_VALUE;
        }

        return max.getValue();
    }

    private static String presence(SchemaNode node) {
        if (node instanceof Container container) {
            return String.valueOf(container.isPresence());
        }

        return null;
    }

    private static String whenCondition(SchemaNode node) {
        if (!(node instanceof WhenSupport whenSupport)) {
            return null;
        }

        When when = whenSupport.getWhen();
        return safeArg(when);
    }

    private static String mustConditions(SchemaNode node) {
        if (!(node instanceof MustSupport mustSupport)) {
            return null;
        }

        List<?> musts = mustSupport.getMusts();
        if (musts == null || musts.isEmpty()) {
            return null;
        }

        return musts.stream()
                .map(must -> safeArg((Must) must))
                .sorted()
                .collect(Collectors.joining(" && "));
    }

    private static String safeArg(YangStatement statement) {
        return statement == null ? null : statement.getArgStr();
    }
}