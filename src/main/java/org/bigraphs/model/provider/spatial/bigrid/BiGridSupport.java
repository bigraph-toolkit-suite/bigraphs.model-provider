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

    public static class Assertations {
        //TODO asssertIsBiGrid(), OCL?
    }


    //TODO for LocationModelData: MPMathUtils.roundXYCoordinate()
    // add rounding function to use throughout everything when working with coordinates?
    // through whole application

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
        //TODO take average of w,d,h of BB when it is no cube
        float agentBoundingBoxDiagonal = (float) (Math.sqrt(2) * agent.getWidth());
        //TODO maybe add to threshold `agentBoundingBoxDiagonal` also an "agent space margin"
        return MPMathUtils.coordinatesAreClose(pointToCheck, pointAgent, agentBoundingBoxDiagonal);
    }

    public static String formatParamControl(Point2D.Float coordinate) {
        String formattedX = formatCoordinate(coordinate.x);
        String formattedY = formatCoordinate(coordinate.y);
        return String.format("C_%s__%s", formattedX, formattedY);
    }

//    private static String formatCoordinate(float coordinate) {
//        if (coordinate == (long) coordinate) {
//            return String.format("%d", (long) coordinate);
//        } else {
//            return String.format("%s", coordinate);
//        }
//    }

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

    public static class Ops {

        public static void addLocaleAt(int x, int y, BLocationModelData.Locale locale, PureBigraph bigrid) {
            DefaultDynamicSignature sig = bigrid.getSignature();

            //TODO find index of locale at x,y
            // BigraphUtils/...: instantiateParameter, others are ID_place graph
            // other approach: re-create... but comp. is cheap
            throw new RuntimeException("Not implemented yet.");
        }


        //TODO: add param: "withMargin"
        // Central kann "margin" schrittweise decreasen, wenn sich andere agents darin befinden.
        // margin bezieht sich aber auf die echten koordinaten - es werden nur bigrid-nodes einbeziehen in neighboor
        // hood solage die "äußeren" knoten eine margin zum nächsten agent < eps haben.

        //TODO add bigraph attributes for coords!
        //TODO add bigraph attributes for width/depth of locale!

        //TODO new other method return list: check if x,y überhaupt contained.: possible nodes: throw exception if not deterministic choice
        // default: when many below threshold, choose the best distance

        /**
         * The bigraph must be a bigrid
         *
         * @param x
         * @param y
         * @param bigrid
         */
        public static void getNeighbourHoodOfCoord(float x, float y, PureBigraph bigrid) {
            //TODO assertConformsToBigridSpec() OCL later, prime, as many roots as sites etc. (isolation allowed)

            // TODO First: get neighbourhood based on connectedness (later: perRange, perResolution etc.)
            // search the nodes with coord
            BigraphEntity.RootEntity rootEntity = bigrid.getRoots().get(0);// refer to the rule's redex site -> where the coordinate control is located
            List<BigraphEntity<?>> childrenOf = bigrid.getChildrenOf(rootEntity);
            if (childrenOf.size() != 0) {
                Traverser<BigraphEntity> traverser = Traverser.forTree(be -> {
                    List<BigraphEntity<?>> children = bigrid.getChildrenOf(be);
                    System.out.format("%s has %d children\n", be.getType(), children.size());
                    return children;
                });
                List<BigraphEntity<?>> possibleNodes = new ArrayList<>();
                Iterable<BigraphEntity> bigraphEntities = traverser.breadthFirst(bigrid.getRoots());
                bigraphEntities.forEach(be -> {
//                    System.out.println(be);
                    if (be instanceof BigraphEntity.NodeEntity<?> && ((BigraphEntity.NodeEntity<?>) be).getControl().getNamedType().stringValue().startsWith("C_")) {
                        possibleNodes.add(be);
                    }
                });
//                System.out.println(possibleNodes);

//                String ctrlLbl = bigrid.getChildrenOf(rootEntity).get(0).getControl().getNamedType().stringValue();
//                String[] split = ctrlLbl.split("_");
//                if (ctrlLbl.startsWith("C") && split.length == 3) {
//                    int x = Integer.parseInt(split[1]);
//                    int y = Integer.parseInt(split[2]);
//                    System.out.println("---: MoveRuleActionStarted: Call ROS Service - move UAV to [" + x + ", " + y + ", 5.3]");
////                    boolean tflUAV = instance.call("/home/dominik/git/multi-drone-simulation-using-ros-and-bigraphs/ros/src/uav_state_machine_srv",
////                            "callService.py", Arrays.asList("tflUAV", "" + x, "" + y, "5.3"));
//                    boolean tflUAV = true;
//                    return tflUAV;
//                }
            }

            // TODO Second re-create the LocationModelParseData
            // when traversing for searching the nodes


            //TODO Third: Return the part and the Rest.
            // check if holes are created anyway?
            // for the rest to bigraph matching otherwise it part be hard to merge product??
            // or is it easy?

//        LocationModelParseData lmpd = new LocationModelParseData();
            // First row
//        lmpd.getLocales().add(LocationModelParseData.Locale.builder().name("v0").center(new Point2D.Float(0, 0)).build());
            // Because of bidirectionality we only need to define one direction from locale to locale
            // Horizontal ones first
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l1").startingPoint(new Point2D.Float(0, 0)).endingPoint(new Point2D.Float(0, 1)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l2").startingPoint(new Point2D.Float(0, 1)).endingPoint(new Point2D.Float(0, 2)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l3").startingPoint(new Point2D.Float(1, 3)).endingPoint(new Point2D.Float(1, 4)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l4").startingPoint(new Point2D.Float(1, 4)).endingPoint(new Point2D.Float(1, 5)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l5").startingPoint(new Point2D.Float(2, 6)).endingPoint(new Point2D.Float(2, 7)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l6").startingPoint(new Point2D.Float(2, 7)).endingPoint(new Point2D.Float(2, 8)).build());
//        // Vertical ones next, first to second row,
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l7").startingPoint(new Point2D.Float(0, 0)).endingPoint(new Point2D.Float(1, 3)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l8").startingPoint(new Point2D.Float(0, 1)).endingPoint(new Point2D.Float(1, 4)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l9").startingPoint(new Point2D.Float(0, 2)).endingPoint(new Point2D.Float(1, 5)).build());
//        // second row to third row
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l10").startingPoint(new Point2D.Float(1, 3)).endingPoint(new Point2D.Float(2, 6)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l11").startingPoint(new Point2D.Float(1, 4)).endingPoint(new Point2D.Float(2, 7)).build());
//        lmpd.getRoutes().add(LocationModelParseData.Route.builder().name("l12").startingPoint(new Point2D.Float(1, 5)).endingPoint(new Point2D.Float(2, 8)).build());
//
//        // Create bigraph grid
//        CustomGridWorldModelProvider dynamicWorldModelProvider = new CustomGridWorldModelProvider(lmpd)
//                .setRouteDirection(CustomGridWorldModelProvider.RouteDirection.BIDIRECTIONAL);
//        PureBigraph gameBoard = dynamicWorldModelProvider.getBigraph();


        }

    }
}
