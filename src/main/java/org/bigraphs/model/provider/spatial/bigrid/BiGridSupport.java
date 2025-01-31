package org.bigraphs.model.provider.spatial.bigrid;

import com.google.common.graph.Traverser;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.util.MPMathUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    public static class Assertations {
        //TODO asssertIsBiGrid()
    }

    public static class Search {

        // By the internal node name (the name id attribute of the bigraph node) of a locale
        @Deprecated
        public static int findLocaleSiteIndexByLocaleID(String localeNodeID, PureBigraph bigrid) {
            //TODO asssertIsBiGrid()
            return -1;
        }

        /**
         *
         * @param coordControlLbl
         * @param bigrid
         * @return -1 if index could not be determined.
         */
        public static int findLocaleSiteIndexByCoordinate(String coordControlLbl, PureBigraph bigrid) {
            //TODO asssertIsBiGrid()
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
