package org.bigraphs.model.provider.spatial.bigrid;

import com.google.common.graph.Traverser;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;
import org.bigraphs.model.provider.util.Point2DUtils;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes graph properties of a bigrid, a bigraphical grid.
 * - get neighbourhood
 * - etc.
 *
 * @author Dominik Grzelak
 */
public abstract class BiGridSupport {

    /**
     * Bounding Box of agent is also considered
     * Whether the agent is at/near a point (plus BB as margin) wrt the distance threshold.
     * <p>
     * E.g., point for a locale is the center coordinate.
     *
     * @param agent        the agent to check
     * @param pointToCheck the point to check against
     * @return true if agent is in locale
     */
    public static boolean agentIsCloseAtPoint(BLocationModelData.Agent agent, Point2D.Float pointToCheck) {
        Point2D.Float pointAgent = (agent.getCenter());
        float agentBoundingBoxDiagonal = (float) (Math.sqrt(2) * agent.getWidth());
        return Point2DUtils.coordinatesAreClose(pointToCheck, pointAgent, agentBoundingBoxDiagonal);
    }

    public static String formatParamControl(Point2D.Float coordinate) {
        String formattedX = formatCoordinate(coordinate.x);
        String formattedY = formatCoordinate(coordinate.y);
        return String.format("C_%s__%s", formattedX, formattedY);
    }

    // Locale-independent
    public static String formatCoordinate(float value) {
        boolean isNegative = value < 0;
        value = Math.abs(value);

        int integerPart = (int) value;
        int fractionalPart = Math.round((value - integerPart) * 100); // 2 decimal places

        String prefix = isNegative ? "N" : "";

        return String.format("%s%d_%02d", prefix, integerPart, fractionalPart);
    }

    /**
     * Opposite method to formatParamControl().
     *
     * @param formattedString output of formatParamControl()
     * @return the original float value
     */
    public static Point2D.Float parseParamControl(String formattedString) throws IllegalArgumentException {
        if (formattedString == null || !formattedString.startsWith("C_") || !formattedString.contains("__")) {
            throw new IllegalArgumentException("Invalid format");
        }

        try {
            String[] parts = formattedString.substring(2).split("__");
            float x = parseCoordinate(parts[0]);
            float y = parseCoordinate(parts[1]);
            return new Point2D.Float(x, y);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid format", e);
        }
    }

    // Locale-independent
    private static float parseCoordinate(String part) {
        part = part.replace("N", "-").replace("_", ".");
        return Float.parseFloat(part);
    }

    public static PureBigraphBuilder<DynamicSignature>.Hierarchy connectToOuterName(String localeName,
                                                                                           Map<String, PureBigraphBuilder<DynamicSignature>.Hierarchy> localeMap,
                                                                                           String outerName,
                                                                                           Map<String, String> localeOuterNameMap)
            throws InvalidConnectionException, TypeNotExistsException {
        if (!localeOuterNameMap.containsKey(localeName)) {
            localeOuterNameMap.putIfAbsent(localeName, outerName);
            return localeMap.get(localeName).top().linkOuter(outerName);
        }
        return localeMap.get(localeName);
    }

    /**
     * A point of a route cannot overlap multiple locales (its spatial dimensions) but there can be many routes starting/ending from the same locale.
     * Locales have the property that they have distinct, non-overlapping realms.
     * So this method only retrieves a unique locale for a route position if there is any.
     *
     * @param roadPosition a starting or ending position of a route element
     * @param locales      a list of locales to observe
     * @return a unique local element, where the starting or ending point of a route is within the locale's spatial dimension
     */
    public static BLocationModelData.Locale getConnectedLocale(Point2D.Float roadPosition, List<BLocationModelData.Locale> locales) {
        for (BLocationModelData.Locale each : locales) {
            if (Point2DUtils.coordinatesAreClose(roadPosition, each.getCenter(), 0)) {
                return each;
            }
        }
        return null;
    }

    public static class Assertations {
        public static void assertIsBiGrid(PureBigraph bigraph) {
            Set<String> sourceTypeLabels = bigraph.getSignature().getControls().stream().map(c -> c.getNamedType().stringValue()).collect(Collectors.toSet());
            DynamicSignature signature = BiSpaceSignatureProvider.getInstance().getSignature();
            signature.getControls().stream().map(c -> c.getNamedType().stringValue()).forEach(controllbl -> {
                if (!sourceTypeLabels.contains(controllbl)) {
                    throw new RuntimeException("Assertion failed (assertIsBigrid): Control label " + controllbl + " not in the given a bigrid-style bigraph.");
                }
            });
        }
    }

    public static class Search {

        /**
         * @param coordControlLbl
         * @param bigrid
         * @return -1 if index could not be determined.
         */
        public static int findLocaleSiteIndexByCoordinate(String coordControlLbl, PureBigraph bigrid) {
            Assertations.assertIsBiGrid(bigrid);
            Traverser<BigraphEntity> traverser = Traverser.forTree(bigrid::getChildrenOf);
            Iterable<BigraphEntity> bigraphEntities = traverser.breadthFirst(bigrid.getRoots());
            for (BigraphEntity<?> x : bigraphEntities) {
                if (x instanceof BigraphEntity.NodeEntity<?> && ((BigraphEntity.NodeEntity<?>) x).getControl().getNamedType().stringValue().startsWith("C_")) {
                    try {
                        if (((BigraphEntity.NodeEntity<?>) x).getControl().getNamedType().stringValue().equals(coordControlLbl)) {
                            BigraphEntity<?> locale = bigrid.getParent(bigrid.getParent(x));
                            return bigrid.getChildrenOf(locale).stream()
                                    .filter(n -> BigraphEntityType.isSite(n))
                                    .map(n -> (BigraphEntity.SiteEntity) n)
                                    .findFirst()
                                    .orElseThrow()
                                    .getIndex();
                        }
                    } catch (IllegalArgumentException e) { // from parseParamControl
                    } catch (NoSuchElementException e) { // from orElseThrow
                    }
                }
            }
            return -1;
        }
    }
}
