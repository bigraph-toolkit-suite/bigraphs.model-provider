package org.bigraphs.model.provider.spatial.bigrid;

import com.google.common.graph.Traverser;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;
import org.bigraphs.model.provider.util.MPMathUtils;

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
public class BiGridSupport {

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
        return MPMathUtils.coordinatesAreClose(pointToCheck, pointAgent, agentBoundingBoxDiagonal);
    }

    public static String formatParamControl(Point2D.Float coordinate) {
        String formattedX = formatCoordinate(coordinate.x);
        String formattedY = formatCoordinate(coordinate.y);
        return String.format("C_%s__%s", formattedX, formattedY);
    }

    public static String formatCoordinate(float value) {
        // Check if the value is negative
        boolean isNegative = value < 0;

        // Take the absolute value for easier processing
        value = Math.abs(value);

        // Split the float value into integer and decimal parts
        String[] parts = String.format("%.2f", value).split("\\.");

        // Format the string according to the template
        String formattedString = String.format("%s_%s", isNegative ? "N" + parts[0] : parts[0], parts[1]);

        return formattedString;
    }

    public static Point2D.Float parseParamControl(String formattedString) throws IllegalArgumentException {
        if (formattedString == null || !formattedString.startsWith("C_") || !formattedString.contains("__")) {
            throw new IllegalArgumentException("Invalid format");
        }

        try {
            String[] parts = formattedString.substring(2).split("__");
            parts[0] = parts[0].replace("N", "-").replace("_", ".");
            float x = Float.parseFloat(parts[0]);
            parts[1] = parts[1].replace("N", "-").replace("_", ".");
            float y = Float.parseFloat(parts[1]);
            return new Point2D.Float(x, y);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid format", e);
        }
    }

    public static PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy connectToOuterName(String localeName,
                                                                                           Map<String, PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy> localeMap,
                                                                                           String outerName,
                                                                                           Map<String, String> localeOuterNameMap)
            throws InvalidConnectionException, TypeNotExistsException {
        if (!localeOuterNameMap.containsKey(localeName)) {
            localeOuterNameMap.putIfAbsent(localeName, outerName);
            return localeMap.get(localeName).top().linkToOuter(outerName);
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
            if (MPMathUtils.coordinatesAreClose(roadPosition, each.getCenter(), 0)) {
                return each;
            }
        }
        return null;
    }

    public static class Assertations {
        public static void assertIsBiGrid(PureBigraph bigraph) {
            Set<String> sourceTypeLabels = bigraph.getSignature().getControls().stream().map(c -> c.getNamedType().stringValue()).collect(Collectors.toSet());
            DefaultDynamicSignature signature = BiSpaceSignatureProvider.getInstance().getSignature();
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
                        //                    e.printStackTrace();
                        //                    System.out.println("No coordinate control: " + x.getControl());
                    } catch (NoSuchElementException e) { // from orElseThrow
                        //                    e.printStackTrace();
                        //                    System.out.println("Contains no site: " + x.getControl());
                    }
                }
            }
            return -1;
        }
    }
}
