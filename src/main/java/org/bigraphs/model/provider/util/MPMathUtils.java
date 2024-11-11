package org.bigraphs.model.provider.util;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MPMathUtils {


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

    public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }
}
