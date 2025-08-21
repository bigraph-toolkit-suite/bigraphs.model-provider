package org.bigraphs.model.provider.spatial.quadtree.impl;

import java.awt.geom.Point2D;

/**
 * Custom listener interface for the quadtree visualization implementation.
 *
 * @author Dominik Grzelak
 */
public interface QuadtreeListener {
    default void onPointInserted(Point2D point) {

    }

    default void onPointRejected(Point2D point) {

    }

    default void onPointDeleted(Point2D point) {
        
    }
}
