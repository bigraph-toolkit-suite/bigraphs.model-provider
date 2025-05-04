package org.bigraphs.model.provider.spatial.bigrid;

import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

import java.awt.geom.Point2D;
import java.util.*;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;

public class LinearInterpolationBuilder {
    private static final float stepSize = 0.2f; // cm
    private static final float minDist = 0.2f;

    private static final BiGridElementFactory factory = BiGridElementFactory.create();

    /**
     * Often it is good to have stepSize == minDist
     *
     * @param originalPoints
     * @param stepSize
     * @param minDist
     * @return
     * @throws InvalidConnectionException
     */
    public static PureBigraph generate(List<Point2D.Float> originalPoints, float stepSize, float minDist) throws InvalidConnectionException {
        // 1. Generate new points with minimum distance
        Set<Point2D.Float> allPoints = distributeAdditionalPoints(originalPoints, minDist);

        // 2. Create bigraph nodes at each grid point
        Map<Point2D.Float, PureBigraph> pointToBigraph = new HashMap<>();
        for (Point2D.Float pt : allPoints) {
            PureBigraph g = factory.crossingFour(pt.x, pt.y, stepSize);
            pointToBigraph.put(pt, g);
        }

        PureBigraph result = pointToBigraph.values().stream()
                .reduce((b1, b2) -> {
                    try {
                        return ops(b1).parallelProduct(b2).getOuterBigraph();
                    } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new IllegalStateException("No bigraphs to reduce"));
        return result;
    }

    // Helper: generates additional points between existing ones, snapping to grid (rounding)
    private static Set<Point2D.Float> distributeAdditionalPoints(List<Point2D.Float> original, float spacing) {
        Set<Point2D.Float> result = new HashSet<>(original);

        for (int i = 0; i < original.size(); i++) {
            for (int j = i + 1; j < original.size(); j++) {
                Point2D.Float a = original.get(i);
                Point2D.Float b = original.get(j);
                float dx = b.x - a.x;
                float dy = b.y - a.y;
                float dist = (float) a.distance(b);

                int steps = (int) (dist / spacing);
                for (int s = 1; s < steps; s++) {
                    float t = s / (float) steps;
                    float x = a.x + t * dx;
                    float y = a.y + t * dy;

                    // Snap to grid
                    float snappedX = Math.round(x / spacing) * spacing;
                    float snappedY = Math.round(y / spacing) * spacing;
                    result.add(new Point2D.Float(snappedX, snappedY));
                }
            }
        }

        return result;
    }
}
