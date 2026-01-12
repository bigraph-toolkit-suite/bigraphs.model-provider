package org.bigraphs.model.provider.spatial.bigrid;

import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;

public class ConvexShapeBuilder {

    public static List<PureBigraph> generateMultiRoot(
            List<Point2D.Float> convexPoints,
            float stepSize,
            float padding,
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

        Area innerArea = new Area(polygon);

        if (padding > 0f) {
            BasicStroke stroke = new BasicStroke(
                    2f * padding,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER
            );

            Shape outline = stroke.createStrokedShape(polygon);
            innerArea.subtract(new Area(outline));
        }

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
        // use integer loop variable instead of floating point arithmetic
        int nx = (int) Math.ceil((maxX - minX) / stepSize);
        int ny = (int) Math.ceil((maxY - minY) / stepSize);
        for (int ix = 0; ix <= nx; ix++) {
            float x = minX + ix * stepSize;
            for (int iy = 0; iy <= ny; iy++) {
                float y = minY + iy * stepSize;
                Point2D.Float point = new Point2D.Float(x, y);

                if (innerArea.contains(point)) {
//                if (polygon.contains(point)) {
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
                .orElseThrow(() -> new IllegalStateException("No bigraphs to reduce"));
    }
}
