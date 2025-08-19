package org.bigraphs.model.provider.spatial.quadtree.impl;

import lombok.Getter;
import lombok.Setter;
import org.bigraphs.model.provider.spatial.quadtree.Quadtree;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of a quadtree supporting maximum depth and maximum allowed points per quads.
 * Moreover, points can be discarded while inserted when a proximity threshold is violated.
 * <p>
 * Quad items are simply {@link Point2D} objects.
 *
 * @author Dominik Grzelak
 */
public class QuadtreeImpl implements Quadtree {
    private final List<QuadtreeListener> listeners = new ArrayList<>();

    // Define a proximity threshold (e.g., 1.0 units)
    @Getter
    @Setter
    private float proximityDistance = 0.1f;
    private final int maxPoints; // Max points per node
    private final int maxDepth;  // Max depth of the tree
    private final int depth;     // Current depth of this node
    @Getter
    private QuadtreeImpl parent;  // Reference to the parent node
    @Getter
    private final Boundary boundary;
    @Getter
    private final List<Point2D> points;
    @Getter
    private boolean divided;
    @Getter
    private QuadtreeImpl northeast;
    @Getter
    private QuadtreeImpl northwest;
    @Getter
    private QuadtreeImpl southeast;
    @Getter
    private QuadtreeImpl southwest;

    /**
     * Constructor with custom boundary, max points per leaf, and max depth
     *
     * @param rectangle the boundary
     * @param maxPoints maximum allowed points per quad before subdivision
     * @param maxDepth  maximum allowed depth
     * @param depth     the current depth
     */
    public QuadtreeImpl(Boundary rectangle, int maxPoints, int maxDepth, int depth, float proximityDistance) {
        this.boundary = rectangle;
        this.maxPoints = maxPoints;
        this.maxDepth = maxDepth;
        this.depth = depth;
        this.points = new ArrayList<>();
        this.divided = false;
        this.parent = null;
        this.proximityDistance = proximityDistance;
    }

    /**
     * Overloaded constructor for initial quadtree creation (depth starts at 0)
     *
     * @param rectangle the boundary
     * @param maxPoints maximum allowed points per quad before subdivision
     * @param maxDepth  maximum allowed depth
     */
    public QuadtreeImpl(Boundary rectangle, int maxPoints, int maxDepth) {
        this(rectangle, maxPoints, maxDepth, 0, 0.1f);
    }

    /**
     * Method to add listeners
     *
     * @param listener a listener implementation
     */
    public void addListener(QuadtreeListener listener) {
        if (listener == null) return;

        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // Recursively add the listener to all sub-nodes (if subdivided)
        if (divided) {
            if (northeast != null) northeast.addListener(listener);
            if (northwest != null) northwest.addListener(listener);
            if (southeast != null) southeast.addListener(listener);
            if (southwest != null) southwest.addListener(listener);
        }
    }

    /**
     * Notify all listeners after point is inserted.
     *
     * @param point the last point inserted
     */
    private void informListeners_added(Point2D point) {
        for (QuadtreeListener listener : listeners) {
            listener.onPointInserted(point);
        }
    }

    private void informListeners_delete(Point2D point) {
        for (QuadtreeListener listener : listeners) {
            listener.onPointDeleted(point);
        }
    }

    /**
     * Insert a point into the quadtree
     *
     * @return {@code true} if successfully inserted, otherwise {@code false}
     */
    public boolean insert(Point2D point) {
        if (!boundary.contains(point)) {
            return false; // Ignore points outside the boundary
        }

        // Check for proximity or duplicate points within this node
        for (Point2D existingPoint : points) {
            if (existingPoint.distance(point) < proximityDistance) {
//                System.out.println("Point not insert = " + point);
                return false; // Reject the point if it’s too close to an existing point
            }
        }

        // Check if max depth is reached and we can't subdivide further
        if (depth >= maxDepth) {
            throw new MaxDepthReachedException("Max depth reached and cannot insert more points in this cell. depth = " + depth);
        }

        if (divided) {
            boolean inserted = northeast.insert(point);
            if (!inserted) inserted = northwest.insert(point);
            if (!inserted) inserted = southeast.insert(point);
            if (!inserted) inserted = southwest.insert(point);
            if (inserted) {
//                System.out.println("Inserted point " + point + " at depth " + depth);
                informListeners_added(point);
                return true;
            }
        }

        // Add point if there's space in the current node
        if (points.size() < maxPoints) {
            points.add(point);
            informListeners_added(point);
            return true;
        } else {

            // Subdivide if not already divided and max depth not reached
            if (!divided) {
                subdivide();
                return this.insert(point);
            }
        }
        // If insertion fails in all subdivisions, throw an exception
        throw new MaxDepthReachedException("Failed to insert point");
    }

    /**
     * Remove all points and re-insert them again.
     * If the points of the quadtree are changed, this allows the quadtree to lay out the quads again.
     */
    public void cleanup() {
        List<Point2D> point2DS = queryRange(boundary);
        points.clear();
        northeast = null;
        northwest = null;
        southeast = null;
        southwest = null;
        divided = false;
        point2DS.forEach(p -> {
            try {
                insert(p);
            } catch (NullPointerException e) {
//                e.printStackTrace();
            }
        });
    }

    private void applyListeners(QuadtreeImpl quadtree, List<QuadtreeListener> listeners) {
        listeners.forEach(quadtree::addListener);
    }

    /**
     * Subdivide the Quadtree into 4 quadrants.
     */
    private void subdivide() {
        double x = boundary.x;
        double y = boundary.y;
        double halfWidth = boundary.width / 2;
        double halfHeight = boundary.height / 2;

        northeast = new QuadtreeImpl(new Boundary(x + halfWidth, y, halfWidth, halfHeight), maxPoints, maxDepth, depth + 1, proximityDistance);
        northeast.parent = this;
        northwest = new QuadtreeImpl(new Boundary(x, y, halfWidth, halfHeight), maxPoints, maxDepth, depth + 1, proximityDistance);
        northwest.parent = this;
        southeast = new QuadtreeImpl(new Boundary(x + halfWidth, y + halfHeight, halfWidth, halfHeight), maxPoints, maxDepth, depth + 1, proximityDistance);
        southeast.parent = this;
        southwest = new QuadtreeImpl(new Boundary(x, y + halfHeight, halfWidth, halfHeight), maxPoints, maxDepth, depth + 1, proximityDistance);
        southwest.parent = this;

        // Attach listeners
        applyListeners(northeast, listeners);
        applyListeners(northwest, listeners);
        applyListeners(southeast, listeners);
        applyListeners(southwest, listeners);

        divided = true;

        // Reinsert all points into the child nodes
        for (Point2D point : points) {
            if (!northeast.insert(point) &&
                    !northwest.insert(point) &&
                    !southeast.insert(point) &&
                    !southwest.insert(point)) {
                throw new IllegalStateException("Point could not be reinserted into a child node.");
            }
        }

        // Clear points from the parent node after redistribution
        points.clear();
    }

    /**
     * Delete a point from the quadtree
     *
     * @return {@code true} if successfully deleted, otherwise {@code false}
     */
    public boolean delete(Point2D point) {
        if (!boundary.contains(point)) {
            return false; // Point not within the quadtree boundary
        }

        // If the point is not found in the current node, try to delete from the subdivided nodes
        boolean deleted = false;
        if (divided) {
            deleted = northeast.delete(point);
            if (!deleted) deleted = northwest.delete(point);
            if (!deleted) deleted = southeast.delete(point);
            if (!deleted) deleted = southwest.delete(point);
        }
        if (!deleted) deleted = points.remove(point);

        // After trying all sub-nodes, check if any node was able to remove the point
        if (deleted) {
            informListeners_delete(point);
            return true;
        }

        return false;  // Point not found
    }

    private void collapse() {
        // If all child nodes are empty, collapse the quadtree
        if (northeast != null && northeast.getPoints().isEmpty() &&
                northwest != null && northwest.getPoints().isEmpty() &&
                southeast != null && southeast.getPoints().isEmpty() &&
                southwest != null && southwest.getPoints().isEmpty()) {

            // Clear the child nodes as they are no longer necessary
            northeast = null;
            northwest = null;
            southeast = null;
            southwest = null;
            divided = false;  // Mark the node as undivided
        }
    }

    /**
     * Query points within a range
     *
     * @param range the search boundary
     * @return a list of points within the given boundary
     */
    public List<Point2D> queryRange(Boundary range) {
        List<Point2D> found = new ArrayList<>();

        if (!boundary.intersects(range)) {
            return found; // Return an empty list if range doesn't intersect boundary
        }

        for (Point2D point : points) {
            if (range.contains(point)) {
                found.add(point);
            }
        }

        if (divided) {
            found.addAll(northeast.queryRange(range));
            found.addAll(northwest.queryRange(range));
            found.addAll(southeast.queryRange(range));
            found.addAll(southwest.queryRange(range));
        }

        return found;
    }

    /**
     * Rectangle class representing a boundary for a quadtree node
     */
    @Setter
    @Getter
    public static class Boundary {
        public double x, y, width, height;

        public Boundary() {
            x = y = width = height = 0;
        }

        public Boundary(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean contains(Point2D point) {
            return (point.getX() >= x && point.getX() < x + width &&
                    point.getY() >= y && point.getY() < y + height);
        }

        public boolean intersects(Boundary range) {
            return !(range.x > x + width ||
                    range.x + range.width < x ||
                    range.y > y + height ||
                    range.y + range.height < y);
        }

        // TODO add:
//        public boolean contains(Rect r) {
//            return (r.pos.getX() >= pos.getX()) && (r.pos.getX() + r.size.getX() < pos.getX() + size.getX()) &&
//                    (r.pos.getY() >= pos.getY()) && (r.pos.getY() + r.size.getY() < pos.getY() + size.getY());
//        }

    }

    public static double[] getSpans(QuadtreeImpl.Boundary boundary) {
        double minX = Math.min(boundary.x, boundary.x + boundary.width);
        double maxX = Math.max(boundary.x, boundary.x + boundary.width);
        double minY = Math.min(boundary.y, boundary.y + boundary.height);
        double maxY = Math.max(boundary.y, boundary.y + boundary.height);

        double spanX = maxX - minX;   // true width
        double spanY = maxY - minY;   // true height

        return new double[]{spanX, spanY};
    }

    public static int getMaxTreeDepthFrom(QuadtreeImpl.Boundary boundary, double marginPoint) {
        double[] spans = getSpans(boundary);
        double spanX = spans[0];
        double spanY = spans[1];

        // depth ~ ceil(log2(span / margin)) + 1, clamped to >= 0
        int td0 = (int) Math.ceil(Math.log(spanX / marginPoint) / Math.log(2.0d)) + 1;
        int td1 = (int) Math.ceil(Math.log(spanY / marginPoint) / Math.log(2.0d)) + 1;

        return Math.max(0, Math.min(td0, td1));
    }
}
