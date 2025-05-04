package org.bigraphs.model.provider.spatial.bigrid;

import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;

public class ConvexShapeBuilder {

    public static List<PureBigraph> generateAsList(
            List<Point2D.Float> convexPoints,
            float stepSize,
            BiGridElementFactory factory
    ) throws InvalidConnectionException {
        List<PureBigraph> gridElements = new ArrayList<>();

        // Step 1: Create a polygon path
        Path2D.Float polygon = new Path2D.Float();
        Point2D.Float first = convexPoints.get(0);
        polygon.moveTo(first.x, first.y);
        for (int i = 1; i < convexPoints.size(); i++) {
            polygon.lineTo(convexPoints.get(i).x, convexPoints.get(i).y);
        }
        polygon.closePath();

        // Step 2: Compute bounding box
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (Point2D.Float p : convexPoints) {
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
        }

        // Step 3: Grid iteration and filtering
        for (float x = minX; x <= maxX; x += stepSize) {
            for (float y = minY; y <= maxY; y += stepSize) {
                Point2D.Float point = new Point2D.Float(x, y);
                if (polygon.contains(point)) {
                    PureBigraph grid = factory.crossingFour(x, y, stepSize);
                    gridElements.add(grid);
                }
            }
        }

        return gridElements;
    }

    public static PureBigraph generateAsSingle(
            List<Point2D.Float> convexPoints,
            float stepSize,
            BiGridElementFactory factory
    ) throws InvalidConnectionException {
        List<PureBigraph> gridElements = generateAsList(convexPoints, stepSize, factory);
        return gridElements.stream()
                .reduce((b1, b2) -> {
                    try {
                        return ops(b1).parallelProduct(b2).getOuterBigraph();
                    } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new IllegalStateException("No bigraphs to reduce"));
    }

    // Helper method to check if a point is inside a convex polygon
    private static boolean isPointInsideConvexPolygon(Point2D.Float point, List<Point2D.Float> polygon) {
        int n = polygon.size();
        boolean isInside = true;
        float prevCross = 0;

        for (int i = 0; i < n; i++) {
            Point2D.Float a = polygon.get(i);
            Point2D.Float b = polygon.get((i + 1) % n);

            float dx1 = b.x - a.x;
            float dy1 = b.y - a.y;
            float dx2 = point.x - a.x;
            float dy2 = point.y - a.y;

            float cross = dx1 * dy2 - dy1 * dx2;

            if (i == 0) {
                prevCross = cross;
            } else {
                if (cross * prevCross < 0) {
                    isInside = false;
                    break;
                }
            }
        }

        return isInside;
    }
}
