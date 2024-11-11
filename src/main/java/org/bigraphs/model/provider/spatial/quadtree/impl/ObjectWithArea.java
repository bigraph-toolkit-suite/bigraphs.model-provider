package org.bigraphs.model.provider.spatial.quadtree.impl;

import lombok.Getter;
import lombok.Setter;
import org.bigraphs.model.provider.spatial.quadtree.QuadItem;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * A specialized variant of a quad item with velocity and color.
 *
 * @author Dominik Grzelak
 */
public class ObjectWithArea implements QuadItem {
    @Getter
    @Setter
    Point2D.Double position;
    @Getter
    @Setter
    Point2D.Double size;
    @Getter
    @Setter
    Point2D.Double velocity;
    @Getter
    @Setter
    Color color;

    public ObjectWithArea(Point2D.Double pos, Point2D.Double vel, Point2D.Double size, Color color) {
        this.position = pos;
        this.velocity = vel;
        this.size = size;
        this.color = color;
    }

    public QuadtreeImpl.Boundary getBounds() {
        return new QuadtreeImpl.Boundary(position.getX(), position.getY(), size.x, size.y);
    }
}