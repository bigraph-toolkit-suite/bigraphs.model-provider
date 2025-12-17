package org.bigraphs.model.provider.spatial.quadtree;

/**
 * Minimal interface of any quadtree implementation
 *
 * @author Dominik Grzelak
 */
public interface Quadtree {
    Quadtree getNortheast();

    Quadtree getNorthwest();

    Quadtree getSoutheast();

    Quadtree getSouthwest();
}
