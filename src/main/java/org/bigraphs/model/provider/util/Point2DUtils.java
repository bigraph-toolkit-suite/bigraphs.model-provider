package org.bigraphs.model.provider.util;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A utility class for working with points in location models.
 *
 * @author Dominik Grzelak
 */
public class Point2DUtils {

    private static final Random random = new Random();

    // Jitter amount controls the maximum displacement for the points
    private static final double JITTER_AMOUNT_DEFAULT = 1;

    public static List<Point2D> pointCircle(Point2D center, double radius, double pointCount) {
        ArrayList<Point2D> points = new ArrayList<>();
        for (int i = 0; i < pointCount; i++) {
            double angle = 2 * Math.PI * i / pointCount;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            points.add(new Point2D.Double(x, y));
        }
        return points;
    }

    public static List<Point2D> jitterPoints(List<Point2D> points) {
        return jitterPoints(points, JITTER_AMOUNT_DEFAULT);
    }

    public static List<Point2D> jitterPoints(List<Point2D> points, double jitterAmount) {
        return points.stream()
                .map(point -> {
                    // Apply jitter within the range of -JITTER_AMOUNT to +JITTER_AMOUNT
                    double jitteredX = point.getX() + (random.nextDouble() * 2 - 1) * jitterAmount;
                    double jitteredY = point.getY() + (random.nextDouble() * 2 - 1) * jitterAmount;
                    return new Point2D.Double(jitteredX, jitteredY);
                })
                .collect(Collectors.toList());
    }

    // Computes the Euclidean distance between two coordinates
    public static boolean coordinatesAreClose(Point2D.Float p1, Point2D.Float p2, float threshold) {
        double distance = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
        return Math.abs(distance) <= threshold;
    }

    // Computes the Euclidean distance between two coordinates
    public static boolean coordinatesAreClose(Point p1, Point p2, float threshold) {
        double distance = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
        return Math.abs(distance) <= threshold;
    }

    // Provides a consistent rounding mechanism for xy-coordinates.
    // Helpful to create suitable control names for a signature or for faster collision checks in a consistent manner
    // throughout the application
    public static Point2D.Float roundXYCoordinate(Point2D.Float xy_coordinate) {
        return new Point2D.Float(Math.round(xy_coordinate.x), Math.round(xy_coordinate.y));
    }
}
