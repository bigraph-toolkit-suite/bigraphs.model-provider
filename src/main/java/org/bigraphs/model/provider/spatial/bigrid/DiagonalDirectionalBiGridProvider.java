package org.bigraphs.model.provider.spatial.bigrid;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.model.provider.base.BAbstractBigraphProvider;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.signature.DiagonalDirectionalBiSpaceSignatureProvider;

import java.util.*;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider.*;
import static org.bigraphs.model.provider.spatial.signature.DiagonalDirectionalBiSpaceSignatureProvider.*;
import static org.bigraphs.model.provider.spatial.signature.DirectionalBiSpaceSignatureProvider.*;

/**
 * A diagonal directional bigraph model provider for a bigrid.
 * This provider creates 8-directional routes (cardinal + diagonal directions)
 * instead of generic Route nodes. Each locale gets routes based on its position in the grid:
 * <p>
 * Grid layout (v0 at bottom-right, coordinates: x=Forward, y=Left, based on Rviz coordinate system):
 * <pre>
 *     col2   col1   col0
 * row2 [2,2]  [2,1]  [2,0]   <- Top row
 * row1 [1,2]  [1,1]  [1,0]
 * row0 [0,2]  [0,1]  [0,0]   <- Bottom row (v0 here)
 *       ↑                ↑
 *    Left max         Right
 * </pre>
 * <p>
 * Coordinate system:
 * - X-axis: Forward (up/forward) is positive, Back (down/backward) is negative
 * - Y-axis: Left is positive, Right is negative
 * <p>
 * Route directions (8 total):
 * Cardinal directions:
 * - ForwardRoute: connects to (row + 1, col) - higher x coordinate
 * - BackRoute: connects to (row - 1, col) - lower x coordinate
 * - LeftRoute: connects to (row, col + 1) - higher y coordinate
 * - RightRoute: connects to (row, col - 1) - lower y coordinate
 * <p>
 * Diagonal directions:
 * - ForwardLeftRoute: connects to (row + 1, col + 1) - upper left
 * - ForwardRightRoute: connects to (row + 1, col - 1) - upper right
 * - BackLeftRoute: connects to (row - 1, col + 1) - lower left
 * - BackRightRoute: connects to (row - 1, col - 1) - lower right
 * <p>
 * Route count by position:
 * - Corner positions (e.g., v0 at row=0, col=0): 3 routes
 * - Edge positions: 5 routes
 * - Center positions: 8 routes
 *
 * @author Tianxiong Zhang
 */
public class DiagonalDirectionalBiGridProvider extends BAbstractBigraphProvider<DynamicSignature, PureBigraph> {

    protected BLocationModelData lmpd;
    private boolean makeWorldModelGround = false;
    protected BiMap<String, Integer> mLocale2Index = HashBiMap.create();

    // Grid dimensions - these must be set for directional routing to work correctly
    private int rows;
    private int cols;

    public DiagonalDirectionalBiGridProvider(BLocationModelData lmpd, int rows, int cols) {
        super(DiagonalDirectionalBiSpaceSignatureProvider.getInstance());
        this.lmpd = lmpd;
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public DynamicSignature getSignature() {
        return signatureProvider.getSignature();
    }

    public BLocationModelData getLocationModelData() {
        return lmpd;
    }

    public void setLocationModelData(BLocationModelData lmpd) {
        this.lmpd = lmpd;
    }

    @Override
    public PureBigraph getBigraph() throws Exception {
        // Clear previous data
        lmpd.getLocaleNameToCoordinates().clear();
        lmpd.getBNodeIdToExternalLocaleName().clear();
        lmpd.getLocaleNameToRootOrSiteIndex().clear();

        for (BLocationModelData.Locale each : lmpd.getLocales()) {
            getLocationModelData().getLocaleNameToCoordinates().putIfAbsent(each.getName(), new ArrayList<>());
            getLocationModelData().getLocaleNameToCoordinates().get(each.getName()).add(each.getCenter());
        }

        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(getSignature());
        Map<String, PureBigraphBuilder<DynamicSignature>.Hierarchy> localeNameToBigraphMap = new LinkedHashMap<>();
        Map<String, String> localeNameToOuternameMap = new LinkedHashMap<>();

        // Make copies
        LinkedList<BLocationModelData.Locale> locales = new LinkedList<>(lmpd.getLocales());
        lmpd.setNumOfLocModelSites(locales.size());

        Collections.sort(locales);

        if (LOG_DEBUG) {
            System.out.format("Total Locales found: %d\n", locales.size());
            System.out.format("Grid size: %d rows x %d cols\n", rows, cols);
        }

        // Build locale map: index -> locale mapping
        // Index = row * cols + col
        Map<Integer, BLocationModelData.Locale> indexToLocaleMap = new LinkedHashMap<>();
        int sIx = 0;
        for (BLocationModelData.Locale each : locales) {
            assert each.getName() != null;

            // Create locale hierarchy
            if (makeWorldModelGround) {
                localeNameToBigraphMap.put(each.getName(), builder.hierarchy(LOCALE_TYPE).top());
            } else {
                localeNameToBigraphMap.put(each.getName(), builder.hierarchy(LOCALE_TYPE).site().top());
            }

            String nodeId = localeNameToBigraphMap.get(each.getName()).getLastCreatedNode().getName();
            assert nodeId.equalsIgnoreCase(each.getName());
            lmpd.getBNodeIdToExternalLocaleName().put(nodeId, each.getName());

            String coordLabel = BiGridSupport.formatParamControl(each.getCenter());
            localeNameToOuternameMap.put(nodeId, coordLabel);
            localeNameToBigraphMap.get(nodeId).top().linkOuter(localeNameToOuternameMap.get(nodeId));

            mLocale2Index.put(each.getName(), sIx);
            indexToLocaleMap.put(sIx, each);
            sIx++;
        }
        lmpd.setLocaleNameToRootOrSiteIndex(mLocale2Index);

        // Now create 8-directional routes for each locale based on its grid position
        // Coordinate system: v0 at (0,0) bottom-right, Forward (x) and Left (y) are positive
        // Index calculation: index = row * cols + col
        for (int index = 0; index < locales.size(); index++) {
            int row = index / cols;
            int col = index % cols;

            BLocationModelData.Locale currentLocale = indexToLocaleMap.get(index);
            String currentLocaleName = currentLocale.getName();

            // ========== Cardinal Directions (4) ==========

            // ForwardRoute: connects to (row + 1, col) - upward
            if (row < rows - 1) {
                int forwardIndex = (row + 1) * cols + col;
                createRoute(indexToLocaleMap, localeNameToBigraphMap, localeNameToOuternameMap,
                        currentLocaleName, forwardIndex, FORWARD_ROUTE_TYPE);
            }

            // BackRoute: connects to (row - 1, col) - downward
            if (row > 0) {
                int backIndex = (row - 1) * cols + col;
                createRoute(indexToLocaleMap, localeNameToBigraphMap, localeNameToOuternameMap,
                        currentLocaleName, backIndex, BACK_ROUTE_TYPE);
            }

            // LeftRoute: connects to (row, col + 1) - leftward
            if (col < cols - 1) {
                int leftIndex = row * cols + (col + 1);
                createRoute(indexToLocaleMap, localeNameToBigraphMap, localeNameToOuternameMap,
                        currentLocaleName, leftIndex, LEFT_ROUTE_TYPE);
            }

            // RightRoute: connects to (row, col - 1) - rightward
            if (col > 0) {
                int rightIndex = row * cols + (col - 1);
                createRoute(indexToLocaleMap, localeNameToBigraphMap, localeNameToOuternameMap,
                        currentLocaleName, rightIndex, RIGHT_ROUTE_TYPE);
            }

            // ========== Diagonal Directions (4) ==========

            // ForwardLeftRoute: connects to (row + 1, col + 1) - upper left
            if (row < rows - 1 && col < cols - 1) {
                int forwardLeftIndex = (row + 1) * cols + (col + 1);
                createRoute(indexToLocaleMap, localeNameToBigraphMap, localeNameToOuternameMap,
                        currentLocaleName, forwardLeftIndex, FORWARD_LEFT_ROUTE_TYPE);
            }

            // ForwardRightRoute: connects to (row + 1, col - 1) - upper right
            if (row < rows - 1 && col > 0) {
                int forwardRightIndex = (row + 1) * cols + (col - 1);
                createRoute(indexToLocaleMap, localeNameToBigraphMap, localeNameToOuternameMap,
                        currentLocaleName, forwardRightIndex, FORWARD_RIGHT_ROUTE_TYPE);
            }

            // BackLeftRoute: connects to (row - 1, col + 1) - lower left
            if (row > 0 && col < cols - 1) {
                int backLeftIndex = (row - 1) * cols + (col + 1);
                createRoute(indexToLocaleMap, localeNameToBigraphMap, localeNameToOuternameMap,
                        currentLocaleName, backLeftIndex, BACK_LEFT_ROUTE_TYPE);
            }

            // BackRightRoute: connects to (row - 1, col - 1) - lower right
            if (row > 0 && col > 0) {
                int backRightIndex = (row - 1) * cols + (col - 1);
                createRoute(indexToLocaleMap, localeNameToBigraphMap, localeNameToOuternameMap,
                        currentLocaleName, backRightIndex, BACK_RIGHT_ROUTE_TYPE);
            }
        }

        // Finally, merge all locales under one root
        PureBigraphBuilder<DynamicSignature>.Hierarchy root = builder.root();
        for (PureBigraphBuilder<DynamicSignature>.Hierarchy each : localeNameToBigraphMap.values()) {
            root.child(each);
        }

        return builder.create();
    }

    /**
     * Helper method to create a route from current locale to target locale.
     */
    private void createRoute(
            Map<Integer, BLocationModelData.Locale> indexToLocaleMap,
            Map<String, PureBigraphBuilder<DynamicSignature>.Hierarchy> localeNameToBigraphMap,
            Map<String, String> localeNameToOuternameMap,
            String currentLocaleName,
            int targetIndex,
            String routeType) throws Exception {
        BLocationModelData.Locale targetLocale = indexToLocaleMap.get(targetIndex);
        if (targetLocale != null) {
            String targetOuterName = localeNameToOuternameMap.get(targetLocale.getName());
            localeNameToBigraphMap.get(currentLocaleName).child(routeType).linkOuter(targetOuterName).top();
        }
    }

    public <T extends DiagonalDirectionalBiGridProvider> T makeGround(boolean makeGround) {
        this.makeWorldModelGround = makeGround;
        return (T) this;
    }
}

