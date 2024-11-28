package org.bigraphs.model.provider.spatial.quadtree.impl;

import java.awt.geom.Point2D;

/**
 * Custom listener interface for the quadtree visualization implementation.
 *
 * @author Dominik Grzelak
 */
public interface QuadtreeListener {
    void onPointInserted(Point2D point);

    void onPointDeleted(Point2D point);
}
