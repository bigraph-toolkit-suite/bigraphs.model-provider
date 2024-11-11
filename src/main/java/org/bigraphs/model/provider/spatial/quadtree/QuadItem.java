package org.bigraphs.model.provider.spatial.quadtree;

import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeImpl;

import java.awt.geom.Point2D;

/**
 * This interface represents a spatial element within a quadrant of a quadtree, which are
 * generally called "points".
 * Thus, they have a position attribute ({@link #getPosition()}).
 * Moreover, they can define a rectangular boundary, which can be used for additional collision checking procedures.
 *
 * @author Dominik Grzelak
 */
public interface QuadItem {

    /**
     * Get the position of the quad item within a quad tree.
     *
     * @return the position of the quad item
     */
    Point2D.Double getPosition();

    // Dimension of an entry in a quadtree

    /**
     * Get the width and height of a "point" in the quadtree.
     *
     * @return the dimension of the quad item
     */
    Point2D.Double getSize();

    /**
     * The rectangular boundary of a quad item computed using {@link #getSize()}.
     *
     * @return the boundary of the quad item
     */
    QuadtreeImpl.Boundary getBounds();
}
