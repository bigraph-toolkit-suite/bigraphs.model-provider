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
 * This class provides basic operations and analysis capabilities for bi-grids (i.e., bigraphical grids).
 *
 * @author Dominik Grzelak
 * @author Tianxiong Zhang (3D bigrid operations)
 */
public abstract class BiGridSupport {

    /**
     * Determines whether a given agent is within a specified distance of a point.
     * <p>
     * The check is based on the Euclidean distance between the agent's center and
     * the target point.
     *
     * @param agent        the agent whose position is evaluated
     * @param pointToCheck the spatial point to compare against
     * @param threshold    the maximum allowed distance for proximity
     * @return {@code true} if the agent’s center is within {@code threshold} distance of {@code pointToCheck}; {@code false} otherwise
     */
    public static boolean agentIsCloseAtPoint(BLocationModelData.Agent agent, Point2D.Float pointToCheck, float threshold) {
        Point2D.Float pointAgent = (agent.getCenter());
        return Point2DUtils.coordinatesAreClose(pointToCheck, pointAgent, threshold);
    }

    /**
     * Determines whether an agent is spatially near a given point.
     * <p>
     * The threshold is derived from the diagonal of the agent’s bounding
     * box (√2 x width).
     *
     * @param agent        the agent whose proximity to the point is evaluated
     * @param pointToCheck the spatial point to compare against
     * @return {@code true} if any part of the agent (including its bounding box) lies within
     * the computed threshold distance from {@code pointToCheck}; {@code false} otherwise
     */
    public static boolean agentIsCloseAtPoint(BLocationModelData.Agent agent, Point2D.Float pointToCheck) {
        float agentBoundingBoxDiagonal = (float) (Math.sqrt(2) * agent.getWidth());
        return agentIsCloseAtPoint(agent, pointToCheck, agentBoundingBoxDiagonal);
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


    public static String formatParamControl(Point2D.Float coordinate) {
        String formattedX = formatCoordinate(coordinate.x);
        String formattedY = formatCoordinate(coordinate.y);
        return String.format("C_%s__%s", formattedX, formattedY);
    }

    /**
     * Parses a 2D parameter control string back to coordinates as {@link Point2D}.
     * <p>
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

    /**
     * Formats a 3D coordinate using Point2D for x,y and separate z value.
     *
     * @param point2D the x,y coordinates
     * @param z       the z coordinate (height/layer)
     * @return formatted coordinate string
     */
    public static String formatParamControl3D(Point2D.Float point2D, float z) {
        return formatParamControl3D(point2D.x, point2D.y, z);
    }

    /**
     * Formats a 3D coordinate (x, y, z) into a parameter control string.
     * Format: C_{x}_{y}_{z}
     * Example: (0.0, 0.0, 0.0) -> "C_0_00__0_00__0_00"
     *
     * @param x the x coordinate (Forward/Back direction)
     * @param y the y coordinate (Left/Right direction)
     * @param z the z coordinate (Up/Down direction, height/layer)
     * @return formatted coordinate string
     */
    public static String formatParamControl3D(float x, float y, float z) {
        String formattedX = formatCoordinate(x);
        String formattedY = formatCoordinate(y);
        String formattedZ = formatCoordinate(z);
        return String.format("C_%s__%s__%s", formattedX, formattedY, formattedZ);
    }

    /**
     * Parses a 3D parameter control string back to coordinates.
     * Opposite of formatParamControl3D().
     *
     * @param formattedString output of formatParamControl3D()
     * @return float array [x, y, z]
     * @throws IllegalArgumentException if format is invalid
     */
    public static float[] parseParamControl3D(String formattedString) throws IllegalArgumentException {
        if (formattedString == null || !formattedString.startsWith("C_") || formattedString.split("__").length != 3) {
            throw new IllegalArgumentException("Invalid 3D format");
        }

        try {
            String[] parts = formattedString.substring(2).split("__");
            float x = parseCoordinate(parts[0]);
            float y = parseCoordinate(parts[1]);
            float z = parseCoordinate(parts[2]);
            return new float[]{x, y, z};
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid 3D format", e);
        }
    }

    /**
     * Parses a single coordinate string back to float value.
     * <p>
     * Locale-independent
     *
     * @param part the coordinate string (e.g., "1_50" or "N2_25")
     * @return the float value
     */
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
                                    .filter(BigraphEntityType::isSite)
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
