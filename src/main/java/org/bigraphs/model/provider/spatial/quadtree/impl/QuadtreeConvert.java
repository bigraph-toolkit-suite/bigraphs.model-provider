package org.bigraphs.model.provider.spatial.quadtree.impl;

import org.bigraphs.model.provider.base.BLocationModelData;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Supplier;

/**
 * This converter class helps to create a bigrid-style quadtree.
 * It creates the intermediate representation {@link BLocationModelData} that a {@link org.bigraphs.model.provider.spatial.bigrid.BiGridProvider}
 * takes as input.
 *
 * @author Dominik Grzelak
 */
public class QuadtreeConvert {

    Supplier<String> suppLocalLbls = QuadtreeConvert.createNameSupplier("v");

    public QuadtreeConvert() {
    }

    public BLocationModelData createBLocationModelDataFromQuadtree(QuadtreeImpl quadtree) {
        BLocationModelData locationData = new BLocationModelData();
        Map<QuadtreeImpl, BLocationModelData.Locale> quadToLocaleMap = new HashMap<>();

        // Step 1: Retrieve leaf nodes and sort them by position (top-left origin)
        // power lattice order
        List<QuadtreeImpl> leafNodes = getLeafNodes(quadtree);
        sortLeafNodesByPosition(leafNodes);

        // Step 2: Create Locale objects for each quadtree cell
        for (QuadtreeImpl leaf : leafNodes) {
            Point2D.Float center = calculateCenter(leaf.getBoundary());
            BLocationModelData.Locale locale = BLocationModelData.Locale.builder()
                    .name(generateLocaleName(leaf))
                    .center(center)
                    .width((float) leaf.getBoundary().getWidth())
                    .depth((float) leaf.getBoundary().getHeight())
                    .build();
            locationData.getLocales().add(locale);
            quadToLocaleMap.put(leaf, locale);
        }

        // Step 3: Create Routes for neighboring quadrants (left-right, top-bottom)
        for (int i = 0; i < leafNodes.size(); i++) {
            QuadtreeImpl currentLeaf = leafNodes.get(i);
            BLocationModelData.Locale currentLocale = quadToLocaleMap.get(currentLeaf);

            // Check for a right neighbor (horizontal route)
            // Get all right neighbors for the current node
            List<QuadtreeImpl> rightNeighbors = getRightNeighbors(leafNodes, i);
            for (QuadtreeImpl rightNeighbor : rightNeighbors) {
                BLocationModelData.Locale rightLocale = quadToLocaleMap.get(rightNeighbor);
                createAndAddRoute(locationData, currentLocale, rightLocale);
            }

            // Check for a bottom neighbor (vertical route)
            // Get all bottom neighbors for the current node
            List<QuadtreeImpl> bottomNeighbors = getBottomNeighbors(leafNodes, i);
            for (QuadtreeImpl bottomNeighbor : bottomNeighbors) {
                BLocationModelData.Locale bottomLocale = quadToLocaleMap.get(bottomNeighbor);
                createAndAddRoute(locationData, currentLocale, bottomLocale);
            }
        }

        return locationData;
    }

    // Helper method to calculate center point of a quadrant's boundary
    private Point2D.Float calculateCenter(QuadtreeImpl.Boundary boundary) {
        float centerX = (float) (boundary.getX() + boundary.getWidth() / 2);
        float centerY = (float) (boundary.getY() + boundary.getHeight() / 2);
        return new Point2D.Float(centerX, centerY);
    }

    // Helper method to sort leaf nodes by position (top-left origin)
    private void sortLeafNodesByPosition(List<QuadtreeImpl> leafNodes) {
//        // Another variant
//        leafNodes.sort(Comparator.comparingDouble((Quadtree node) -> calculateCenter(node.getBoundary()).getY())
//                .thenComparingDouble(node -> calculateCenter(node.getBoundary()).getX()));
        leafNodes.sort((node1, node2) -> {
            float x1 = (float) node1.getBoundary().getX();
            float y1 = (float) node1.getBoundary().getY();
            float x2 = (float) node2.getBoundary().getX();
            float y2 = (float) node2.getBoundary().getY();

            // Compare y-position first (top to bottom), then x-position (left to right)
            if (y1 != y2) return Float.compare(y1, y2);
            return Float.compare(x1, x2);
        });
    }

    // Generate a unique name for each locale based on quadtree node properties
    private String generateLocaleName(QuadtreeImpl node) {
        return suppLocalLbls.get();
    }

    private boolean isRightNeighbor(QuadtreeImpl current, QuadtreeImpl neighbor) {
        float currentX = (float) current.getBoundary().getX();
        float neighborX = (float) neighbor.getBoundary().getX();
        float currentY = (float) current.getBoundary().getY();
        float neighborY = (float) neighbor.getBoundary().getY();
        return currentY == neighborY && neighborX > currentX;
    }

    private List<QuadtreeImpl> getRightNeighbors(List<QuadtreeImpl> leafNodes, int currentIndex) {
        QuadtreeImpl current = leafNodes.get(currentIndex);
        float currentX = (float) current.getBoundary().getX();
        float currentY = (float) current.getBoundary().getY();
        List<QuadtreeImpl> rightNeighbors = new ArrayList<>();

        for (int i = currentIndex + 1; i < leafNodes.size(); i++) {
            QuadtreeImpl potentialRight = leafNodes.get(i);
            float potentialRightX = (float) potentialRight.getBoundary().getX();
            float potentialRightY = (float) potentialRight.getBoundary().getY();

            // If the Y position matches, then it's in the same row (right neighbor)
            // and the X position of the potential right neighbor is greater (to the right of the current node)
            if (currentY == potentialRightY && potentialRightX > currentX) {
                rightNeighbors.add(potentialRight);
            }
        }
        return rightNeighbors;
    }

    // Get the bottom neighbor (vertical adjacency)
    private List<QuadtreeImpl> getBottomNeighbors(List<QuadtreeImpl> leafNodes, int currentIndex) {
        QuadtreeImpl current = leafNodes.get(currentIndex);
        float currentX = (float) current.getBoundary().getX();
        float currentY = (float) current.getBoundary().getY();
        List<QuadtreeImpl> bottomNeighbors = new ArrayList<>();

        for (int i = currentIndex + 1; i < leafNodes.size(); i++) {
            QuadtreeImpl potentialBottom = leafNodes.get(i);
            float potentialBottomX = (float) potentialBottom.getBoundary().getX();
            float potentialBottomY = (float) potentialBottom.getBoundary().getY();

            // If the X position matches, then it's in the same column (bottom neighbor)
            // and the Y position of the potential bottom is greater (below current node)
            if (currentX == potentialBottomX && potentialBottomY > currentY) {
                bottomNeighbors.add(potentialBottom);
            }
        }
        return bottomNeighbors;
    }

    // Helper to create a Route and add it to the data object
    private void createAndAddRoute(BLocationModelData data, BLocationModelData.Locale start, BLocationModelData.Locale end) {
        BLocationModelData.Route route = BLocationModelData.Route.builder()
//                .name("route_" + start.getName() + "_to_" + end.getName())
                .name(end.getName())
                .startingPoint(start.getCenter())
                .endingPoint(end.getCenter())
                .build();
        data.getRoutes().add(route);
    }

    // Retrieve leaf nodes in the quadtree
    private List<QuadtreeImpl> getLeafNodes(QuadtreeImpl root) {
        List<QuadtreeImpl> leaves = new ArrayList<>();
        if (!root.isDivided()) {
            leaves.add(root);
        } else {
            if (root.getNortheast() != null) leaves.addAll(getLeafNodes(root.getNortheast()));
            if (root.getNorthwest() != null) leaves.addAll(getLeafNodes(root.getNorthwest()));
            if (root.getSoutheast() != null) leaves.addAll(getLeafNodes(root.getSoutheast()));
            if (root.getSouthwest() != null) leaves.addAll(getLeafNodes(root.getSouthwest()));
        }
        return leaves;
    }

    static Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }
}
