package org.bigraphs.model.provider.spatial.bigrid;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.model.provider.base.BAbstractBigraphProvider;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.signature.ThreeDimensionalBiSpaceSignatureProvider;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider.*;
import static org.bigraphs.model.provider.spatial.signature.DiagonalDirectionalBiSpaceSignatureProvider.*;
import static org.bigraphs.model.provider.spatial.signature.ThreeDimensionalBiSpaceSignatureProvider.*;

/**
 * A 3D bigraph model provider for a bigrid with 10-directional routes.
 * Creates multiple layers of 2D grids connected vertically with UpRoute and DownRoute.
 * <p>
 * Grid layout (v0 at bottom-right of bottom layer):
 * <pre>
 * Layer 2 (z=2*layerHeight):  [Top layer - only DownRoute]
 *     col2   col1   col0
 * row2 [2,2]  [2,1]  [2,0]
 * row1 [1,2]  [1,1]  [1,0]
 * row0 [0,2]  [0,1]  [0,0]
 *       ↓      ↓      ↓
 *
 * Layer 1 (z=layerHeight):  [Middle layer - both UpRoute and DownRoute]
 *     col2   col1   col0
 * row2 [2,2]  [2,1]  [2,0]
 * row1 [1,2]  [1,1]  [1,0]
 * row0 [0,2]  [0,1]  [0,0]
 *       ↓      ↓      ↓
 *
 * Layer 0 (z=0):  [Bottom layer - only UpRoute, v0 here]
 *     col2   col1   col0
 * row2 [2,2]  [2,1]  [2,0]
 * row1 [1,2]  [1,1]  [1,0]
 * row0 [0,2]  [0,1]  [0,0]  <- v0 at (0,0,0)
 * </pre>
 * <p>
 * Coordinate system:
 * - X-axis: Forward (up/forward) is positive
 * - Y-axis: Left is positive
 * - Z-axis: Up is positive (layer 0 is bottom, increases upward)
 * <p>
 * Route directions (10 total per locale, except boundaries):
 * - Horizontal plane (8): Forward, Back, Left, Right, ForwardLeft, ForwardRight, BackLeft, BackRight
 * - Vertical (2): Up, Down
 * <p>
 * Route count by position:
 * - Bottom layer corners: 3 + 1 (UpRoute) = 4 routes
 * - Top layer corners: 3 + 1 (DownRoute) = 4 routes
 * - Middle layer corners: 3 + 2 (Up + Down) = 5 routes
 * - Bottom layer edges: 5 + 1 (UpRoute) = 6 routes
 * - Top layer edges: 5 + 1 (DownRoute) = 6 routes
 * - Middle layer edges: 5 + 2 (Up + Down) = 7 routes
 * - Bottom layer center: 8 + 1 (UpRoute) = 9 routes
 * - Top layer center: 8 + 1 (DownRoute) = 9 routes
 * - Middle layer center: 8 + 2 (Up + Down) = 10 routes (all directions)
 *
 * @author Tianxiong Zhang
 */
public class ThreeDimensionalBiGridProvider extends BAbstractBigraphProvider<DynamicSignature, PureBigraph> {

    protected BLocationModelData lmpd;
    private boolean makeWorldModelGround = false;
    protected BiMap<String, Integer> mLocale2Index = HashBiMap.create();

    // Grid dimensions
    private int rows;
    private int cols;
    private int layers;
    private float layerHeight;

    // Grid origin and step sizes
    private float startX;
    private float startY;
    private float startZ;
    private float stepSizeX;
    private float stepSizeY;

    /**
     * Constructor for 3D bigrid provider.
     *
     * @param lmpd        BLocationModelData containing 2D grid data (will be replicated for each layer)
     * @param rows        number of rows in each layer
     * @param cols        number of columns in each layer
     * @param layers      number of vertical layers
     * @param startX      starting X coordinate
     * @param startY      starting Y coordinate
     * @param startZ      starting Z coordinate
     * @param stepSizeX   step size in X direction
     * @param stepSizeY   step size in Y direction
     * @param layerHeight height between layers
     */
    public ThreeDimensionalBiGridProvider(BLocationModelData lmpd, int rows, int cols, int layers,
                                          float startX, float startY, float startZ,
                                          float stepSizeX, float stepSizeY, float layerHeight) {
        super(ThreeDimensionalBiSpaceSignatureProvider.getInstance());
        this.lmpd = lmpd;
        this.rows = rows;
        this.cols = cols;
        this.layers = layers;
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.stepSizeX = stepSizeX;
        this.stepSizeY = stepSizeY;
        this.layerHeight = layerHeight;
    }

    @Override
    public DynamicSignature getSignature() {
        return signatureProvider.getSignature();
    }

    public BLocationModelData getLocationModelData() {
        return lmpd;
    }

    @Override
    public PureBigraph getBigraph() throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(getSignature());

        // Maps for all locales across all layers
        Map<String, PureBigraphBuilder<DynamicSignature>.Hierarchy> localeNameToBigraphMap = new LinkedHashMap<>();
        Map<String, String> localeNameToOuternameMap = new LinkedHashMap<>();

        // 3D index: [layer][2D_index] -> locale
        Map<Integer, Map<Integer, String>> layer2DIndexToLocaleName = new LinkedHashMap<>();

        if (LOG_DEBUG) {
            System.out.format("Creating 3D Bigrid: %d rows x %d cols x %d layers\n", rows, cols, layers);
            System.out.format("Layer height: %.2f\n", layerHeight);
        }

        int globalLocaleIndex = 0;

        // Create locales for all layers
        for (int layer = 0; layer < layers; layer++) {
            float zCoord = startZ + layer * layerHeight;
            Map<Integer, String> currentLayerMap = new LinkedHashMap<>();
            layer2DIndexToLocaleName.put(layer, currentLayerMap);

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int index2D = row * cols + col;

                    // Calculate 3D coordinates using start position and step sizes
                    float xCoord = startX + row * stepSizeX;
                    float yCoord = startY + col * stepSizeY;

                    // Generate unique locale name including layer
                    String localeName = String.format("v%d_L%d", index2D, layer);
                    currentLayerMap.put(index2D, localeName);

                    // Create locale hierarchy
                    if (makeWorldModelGround) {
                        localeNameToBigraphMap.put(localeName, builder.hierarchy(LOCALE_TYPE).top());
                    } else {
                        localeNameToBigraphMap.put(localeName, builder.hierarchy(LOCALE_TYPE).site().top());
                    }

                    // Create 3D coordinate label
                    String coordLabel = BiGridSupport.formatParamControl3D(xCoord, yCoord, zCoord);
                    localeNameToOuternameMap.put(localeName, coordLabel);
                    localeNameToBigraphMap.get(localeName).top().linkOuter(coordLabel);

                    mLocale2Index.put(localeName, globalLocaleIndex);
                    globalLocaleIndex++;
                }
            }
        }

        // Create routes for each layer and between layers
        for (int layer = 0; layer < layers; layer++) {
            Map<Integer, String> currentLayerMap = layer2DIndexToLocaleName.get(layer);

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int index2D = row * cols + col;
                    String currentLocaleName = currentLayerMap.get(index2D);

                    // ========== Horizontal Routes (same as 2D diagonal directional bigrid) ==========

                    // Cardinal directions
                    if (row < rows - 1) {
                        int forwardIndex = (row + 1) * cols + col;
                        createRoute(currentLayerMap, localeNameToBigraphMap, localeNameToOuternameMap,
                                currentLocaleName, forwardIndex, FORWARD_ROUTE_TYPE);
                    }

                    if (row > 0) {
                        int backIndex = (row - 1) * cols + col;
                        createRoute(currentLayerMap, localeNameToBigraphMap, localeNameToOuternameMap,
                                currentLocaleName, backIndex, BACK_ROUTE_TYPE);
                    }

                    if (col < cols - 1) {
                        int leftIndex = row * cols + (col + 1);
                        createRoute(currentLayerMap, localeNameToBigraphMap, localeNameToOuternameMap,
                                currentLocaleName, leftIndex, LEFT_ROUTE_TYPE);
                    }

                    if (col > 0) {
                        int rightIndex = row * cols + (col - 1);
                        createRoute(currentLayerMap, localeNameToBigraphMap, localeNameToOuternameMap,
                                currentLocaleName, rightIndex, RIGHT_ROUTE_TYPE);
                    }

                    // Diagonal directions
                    if (row < rows - 1 && col < cols - 1) {
                        int forwardLeftIndex = (row + 1) * cols + (col + 1);
                        createRoute(currentLayerMap, localeNameToBigraphMap, localeNameToOuternameMap,
                                currentLocaleName, forwardLeftIndex, FORWARD_LEFT_ROUTE_TYPE);
                    }

                    if (row < rows - 1 && col > 0) {
                        int forwardRightIndex = (row + 1) * cols + (col - 1);
                        createRoute(currentLayerMap, localeNameToBigraphMap, localeNameToOuternameMap,
                                currentLocaleName, forwardRightIndex, FORWARD_RIGHT_ROUTE_TYPE);
                    }

                    if (row > 0 && col < cols - 1) {
                        int backLeftIndex = (row - 1) * cols + (col + 1);
                        createRoute(currentLayerMap, localeNameToBigraphMap, localeNameToOuternameMap,
                                currentLocaleName, backLeftIndex, BACK_LEFT_ROUTE_TYPE);
                    }

                    if (row > 0 && col > 0) {
                        int backRightIndex = (row - 1) * cols + (col - 1);
                        createRoute(currentLayerMap, localeNameToBigraphMap, localeNameToOuternameMap,
                                currentLocaleName, backRightIndex, BACK_RIGHT_ROUTE_TYPE);
                    }

                    // ========== Vertical Routes ==========

                    // UpRoute: connect to same position in layer above
                    if (layer < layers - 1) {
                        Map<Integer, String> upperLayerMap = layer2DIndexToLocaleName.get(layer + 1);
                        String upperLocaleName = upperLayerMap.get(index2D);
                        String upperOuterName = localeNameToOuternameMap.get(upperLocaleName);
                        localeNameToBigraphMap.get(currentLocaleName).child(UP_ROUTE_TYPE).linkOuter(upperOuterName).top();
                    }

                    // DownRoute: connect to same position in layer below
                    if (layer > 0) {
                        Map<Integer, String> lowerLayerMap = layer2DIndexToLocaleName.get(layer - 1);
                        String lowerLocaleName = lowerLayerMap.get(index2D);
                        String lowerOuterName = localeNameToOuternameMap.get(lowerLocaleName);
                        localeNameToBigraphMap.get(currentLocaleName).child(DOWN_ROUTE_TYPE).linkOuter(lowerOuterName).top();
                    }
                }
            }
        }

        // Finally, merge all locales under one root
        PureBigraphBuilder<DynamicSignature>.Hierarchy root = builder.root();
        for (PureBigraphBuilder<DynamicSignature>.Hierarchy each : localeNameToBigraphMap.values()) {
            root.child(each);
        }

        lmpd.setLocaleNameToRootOrSiteIndex(mLocale2Index);
        return builder.create();
    }

    /**
     * Helper method to create a route from current locale to target locale within the same layer.
     */
    private void createRoute(
            Map<Integer, String> layerMap,
            Map<String, PureBigraphBuilder<DynamicSignature>.Hierarchy> localeNameToBigraphMap,
            Map<String, String> localeNameToOuternameMap,
            String currentLocaleName,
            int targetIndex2D,
            String routeType) throws Exception {
        String targetLocaleName = layerMap.get(targetIndex2D);
        if (targetLocaleName != null) {
            String targetOuterName = localeNameToOuternameMap.get(targetLocaleName);
            localeNameToBigraphMap.get(currentLocaleName).child(routeType).linkOuter(targetOuterName).top();
        }
    }

    public <T extends ThreeDimensionalBiGridProvider> T makeGround(boolean makeGround) {
        this.makeWorldModelGround = makeGround;
        return (T) this;
    }
}

