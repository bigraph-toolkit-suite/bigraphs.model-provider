package org.bigraphs.model.provider.util;

import java.awt.geom.Point2D;
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
    private static final double JITTER_AMOUNT = 1;

    public static List<Point2D> jitterPoints(List<Point2D> points) {
        return jitterPoints(points, JITTER_AMOUNT);
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
}
