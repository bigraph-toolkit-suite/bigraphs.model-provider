package org.bigraphs.model.provider.spatial.quadtree;

import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeImpl;

public interface Quadtree {
    QuadtreeImpl getNortheast();
    QuadtreeImpl getNorthwest();
    QuadtreeImpl getSoutheast();
    QuadtreeImpl getSouthwest();
}
