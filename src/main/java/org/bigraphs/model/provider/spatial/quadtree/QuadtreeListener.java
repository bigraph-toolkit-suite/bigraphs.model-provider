package org.bigraphs.model.provider.spatial.quadtree;

/**
 * Custom listener interface for the quadtree visualization implementation.
 *
 * @author Dominik Grzelak
 */
public interface QuadtreeListener {
    default void onPointInserted(QuadItem point) {

    }

    default void onPointRejected(QuadItem point) {

    }

    default void onPointDeleted(QuadItem point) {
        
    }
}
