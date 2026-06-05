package org.bigraphs.model.provider.spatial.bigrid;

import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;

/**
 * Builds grid-based bigraph inside a convex boundary.
 * <p>
 * Padding semantics:
 * - padding > 0 shrinks the interior (erodes the polygon) by approximately {@code padding}
 * by subtracting a stroked outline from the polygon's filled area.
 */
public class ConvexShapeBuilder {

    private static final float EPS = 1e-2f;

    public static List<PureBigraph> generateMultiRoot(
            List<Point2D.Float> convexPoints,
            float stepSize,
            float padding,
            BiGridElementFactory factory
    ) throws InvalidConnectionException {

        if (convexPoints == null || convexPoints.isEmpty()) {
            throw new IllegalArgumentException("convexPoints must not be null/empty");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null");
        }
        if (!(stepSize > 0f)) {
            throw new IllegalArgumentException("stepSize must be > 0");
        }
        if (padding < 0f) {
            padding = 0f;
        }

        // 1) Normalize boundary points to avoid degenerate/self-overlapping paths
        List<Point2D.Float> polyPts = normalizeCycle(convexPoints);
        if (polyPts.size() < 3) {
            throw new IllegalArgumentException("Need at least 3 unique points after normalization");
        }

        // 2) Create polygon path
        Path2D.Float polygon = new Path2D.Float(Path2D.WIND_NON_ZERO, polyPts.size());
        Point2D.Float first = polyPts.getFirst();
        polygon.moveTo(first.x, first.y);
        for (int i = 1; i < polyPts.size(); i++) {
            Point2D.Float p = polyPts.get(i);
            polygon.lineTo(p.x, p.y);
        }
        polygon.closePath();

        // 3) Inner area (possibly eroded by padding)
        Area innerArea = new Area(polygon);

        if (padding > 0f) {
            // Subtract a boundary "ring" of thickness 2*padding
            // BEVEL join avoids sharp miter spikes on acute angles.
            BasicStroke stroke = new BasicStroke(
                    padding,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER
            );
            Shape outline = stroke.createStrokedShape(polygon);
            innerArea.subtract(new Area(outline));
        }

        // If the padding erodes everything, return empty list
        if (innerArea.isEmpty()) {
            return new ArrayList<>();
        }

        // 4) Compute bounds from the *effective* area and snap bounds to the grid
        Rectangle2D bounds = innerArea.getBounds2D();
        float minX = (float) bounds.getMinX();
        float maxX = (float) bounds.getMaxX();
        float minY = (float) bounds.getMinY();
        float maxY = (float) bounds.getMaxY();

        float startX = snapDown(minX, stepSize);
        float endX = snapDown(maxX, stepSize);
        float startY = snapDown(minY, stepSize);
        float endY = snapDown(maxY, stepSize);

        int nx = safeSteps(startX, endX, stepSize);
        int ny = safeSteps(startY, endY, stepSize);

        List<PureBigraph> gridElements = new ArrayList<>();

        // 5) Grid iteration and filtering (integer indexing avoids float drift)
        for (int ix = 0; ix <= nx; ix++) {
            float x = startX + ix * stepSize;
            for (int iy = 0; iy <= ny; iy++) {
                float y = startY + iy * stepSize;

                if (innerArea.contains(x, y)) {
                    PureBigraph grid = factory.crossingFour(x, y, stepSize);
                    gridElements.add(grid);
                }
            }
        }

        return gridElements;
    }

    public static PureBigraph generateSingleRoot(
            List<Point2D.Float> convexPoints,
            float stepSize,
            float padding,
            BiGridElementFactory factory
    ) throws InvalidConnectionException {

        List<PureBigraph> gridElements = generateMultiRoot(convexPoints, stepSize, padding, factory);
        return gridElements.stream()
                .reduce((b1, b2) -> {
                    try {
                        return ops(b1).parallelProduct(b2).getOuterBigraph();
                    } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new IllegalStateException("No bigraphs to reduce (empty interior after padding?)"));
    }

    /**
     * Normalize a polygon cycle:
     * - remove consecutive duplicates (within EPS)
     * - remove duplicated last==first
     * - remove exact duplicates while preserving first-seen order (best-effort)
     * <p>
     * For strictly convex inputs, this is usually sufficient to avoid self-overlap
     * when callers accidentally append reverse/duplicate point sequences.
     */
    private static List<Point2D.Float> normalizeCycle(List<Point2D.Float> pts) {
        List<Point2D.Float> cleaned = new ArrayList<>();
        Point2D.Float prev = null;
        for (Point2D.Float p : pts) {
            if (p == null) continue;
            if (prev == null || !samePoint(prev, p)) {
                cleaned.add(new Point2D.Float(p.x, p.y));
                prev = p;
            }
        }
        // remove trailing duplicates of the first point
        while (cleaned.size() >= 2 && samePoint(cleaned.getFirst(), cleaned.getLast())) {
            cleaned.removeLast();
        }

        // best-effort: remove duplicate vertices while preserving order
        // (helps if input had repeated points or partial reversed lists)
        Set<Long> seen = new LinkedHashSet<>();
        List<Point2D.Float> unique = new ArrayList<>();
        for (Point2D.Float p : cleaned) {
            long key = quantKey(p.x, p.y);
            if (seen.add(key)) {
                unique.add(p);
            }
        }
        return unique;
    }

    private static boolean samePoint(Point2D.Float a, Point2D.Float b) {
        return Math.abs(a.x - b.x) < EPS && Math.abs(a.y - b.y) < EPS;
    }

    private static long quantKey(float x, float y) {
        // quantize to EPS scale for stable "duplicate" detection
        long qx = Math.round(x / EPS);
        long qy = Math.round(y / EPS);
        return (qx << 32) ^ (qy & 0xffffffffL);
    }

    private static float snapDown(float v, float step) {
        return (float) Math.floor(v / step) * step;
    }

    private static float snapUp(float v, float step) {
        return (float) Math.ceil(v / step) * step;
    }

    private static int safeSteps(float start, float end, float step) {
        if (end < start) return 0;
        // Add a tiny epsilon to tolerate rounding
        return (int) Math.ceil(((end - start) / step));
    }
}
