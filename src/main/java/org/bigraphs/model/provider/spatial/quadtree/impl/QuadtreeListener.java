package org.bigraphs.model.provider.spatial.quadtree.impl;

import java.awt.geom.Point2D;

// Define a custom listener interface for the Quadtree
public interface QuadtreeListener {
    void onPointInserted(Point2D point);
    void onPointDeleted(Point2D point);
}
