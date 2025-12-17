package org.bigraphs.model.provider.spatial.bigrid;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.model.provider.base.BAbstractBigraphProvider;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.signature.DirectionalBiSpaceSignatureProvider;

import java.util.*;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider.LOCALE_TYPE;
import static org.bigraphs.model.provider.spatial.signature.DirectionalBiSpaceSignatureProvider.*;

/**
 * A directional bigraph model provider for a bigrid.
 * This provider creates directional routes (LeftRoute, RightRoute, ForwardRoute, BackRoute)
 * instead of generic Route nodes. Each locale gets routes based on its position in the grid:
 * <p>
 * Grid layout (v0 at bottom-right, coordinates: x=Forward, y=Left, based on Rviz coordinate system):
 * <pre>
 *     col2   col1   col0
 * row2 [2,2]  [2,1]  [2,0]   <- Top row (Forward maximum, Back blocked)
 * row1 [1,2]  [1,1]  [1,0]
 * row0 [0,2]  [0,1]  [0,0]   <- Bottom row (v0 here, Forward blocked)
 *       ↑                ↑
 *    Left max         Right (Left blocked)
 * </pre>
 * <p>
 * Coordinate system:
 * - X-axis: Forward (up/forward) is positive, Back (down/backward) is negative
 * - Y-axis: Left is positive, Right is negative
 * <p>
 * Route directions:
 * - ForwardRoute: connects to the locale in the next row (row + 1, higher x coordinate)
 * - BackRoute: connects to the locale in the previous row (row - 1, lower x coordinate)
 * - LeftRoute: connects to the locale in the next column (col + 1, higher y coordinate)
 * - RightRoute: connects to the locale in the previous column (col - 1, lower y coordinate)
 * <p>
 *
 * @author Tianxiong Zhang
 */
public class DirectionalBiGridProvider extends BAbstractBigraphProvider<DynamicSignature, PureBigraph> {

    protected BLocationModelData lmpd;
    private boolean makeWorldModelGround = false;
    protected BiMap<String, Integer> mLocale2Index = HashBiMap.create();

    // Grid dimensions - these must be set for directional routing to work correctly
    private int rows;
    private int cols;

    public DirectionalBiGridProvider(BLocationModelData lmpd, int rows, int cols) {
        super(DirectionalBiSpaceSignatureProvider.getInstance());
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

        // Now create directional routes for each locale based on its grid position
        // Coordinate system: v0 at (0,0) bottom-right, Forward (x) and Left (y) are positive
        // Index calculation: index = row * cols + col
        // For a locale at (row, col), its coordinates are (row, col) where:
        //   - row=0 is bottom row, row increases upward (Forward)
        //   - col=0 is right column, col increases leftward (Left)
        for (int index = 0; index < locales.size(); index++) {
            int row = index / cols;
            int col = index % cols;

            BLocationModelData.Locale currentLocale = indexToLocaleMap.get(index);
            String currentLocaleName = currentLocale.getName();

            // ForwardRoute: connects to the locale in the next row (row + 1, higher x coordinate, upward)
            // Forward is positive direction (upward), so row + 1 means Forward
            if (row < rows - 1) {
                int forwardIndex = (row + 1) * cols + col;
                BLocationModelData.Locale forwardLocale = indexToLocaleMap.get(forwardIndex);
                if (forwardLocale != null) {
                    String targetOuterName = localeNameToOuternameMap.get(forwardLocale.getName());
                    localeNameToBigraphMap.get(currentLocaleName).child(FORWARD_ROUTE_TYPE).linkOuter(targetOuterName).top();
                }
            }

            // BackRoute: connects to the locale in the previous row (row - 1, lower x coordinate, downward)
            // Back is negative direction (downward), so row - 1 means Back
            if (row > 0) {
                int backIndex = (row - 1) * cols + col;
                BLocationModelData.Locale backLocale = indexToLocaleMap.get(backIndex);
                if (backLocale != null) {
                    String targetOuterName = localeNameToOuternameMap.get(backLocale.getName());
                    localeNameToBigraphMap.get(currentLocaleName).child(BACK_ROUTE_TYPE).linkOuter(targetOuterName).top();
                }
            }

            // LeftRoute: connects to the locale in the next column (col + 1, higher y coordinate, leftward)
            // Left is positive direction (leftward), so col + 1 means Left
            if (col < cols - 1) {
                int leftIndex = row * cols + (col + 1);
                BLocationModelData.Locale leftLocale = indexToLocaleMap.get(leftIndex);
                if (leftLocale != null) {
                    String targetOuterName = localeNameToOuternameMap.get(leftLocale.getName());
                    localeNameToBigraphMap.get(currentLocaleName).child(LEFT_ROUTE_TYPE).linkOuter(targetOuterName).top();
                }
            }

            // RightRoute: connects to the locale in the previous column (col - 1, lower y coordinate, rightward)
            // Right is negative direction (rightward), so col - 1 means Right
            if (col > 0) {
                int rightIndex = row * cols + (col - 1);
                BLocationModelData.Locale rightLocale = indexToLocaleMap.get(rightIndex);
                if (rightLocale != null) {
                    String targetOuterName = localeNameToOuternameMap.get(rightLocale.getName());
                    localeNameToBigraphMap.get(currentLocaleName).child(RIGHT_ROUTE_TYPE).linkOuter(targetOuterName).top();
                }
            }
        }

        // Finally, merge all locales under one root
        PureBigraphBuilder<DynamicSignature>.Hierarchy root = builder.root();
        for (PureBigraphBuilder<DynamicSignature>.Hierarchy each : localeNameToBigraphMap.values()) {
            root.child(each);
        }

        return builder.create();
    }

    public <T extends DirectionalBiGridProvider> T makeGround(boolean makeGround) {
        this.makeWorldModelGround = makeGround;
        return (T) this;
    }
}

