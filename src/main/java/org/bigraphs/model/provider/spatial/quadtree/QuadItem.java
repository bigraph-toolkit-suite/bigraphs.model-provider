package org.bigraphs.model.provider.spatial.quadtree;

import lombok.Getter;
import lombok.Setter;
import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeImpl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * A quad item with velocity and color.
 *
 * @author Dominik Grzelak
 */
public class QuadItem extends Point2D {
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

    public QuadItem(Point2D.Double pos) {
        this(pos, null);
    }

    public QuadItem(Point2D.Double pos, Color color) {
        this(pos, null, null, color);
    }

    public QuadItem(Point2D.Double pos, Point2D.Double vel, Point2D.Double size, Color color) {
        this.position = pos;
        this.velocity = vel;
        this.size = size;
        this.color = color;
    }

    public QuadtreeImpl.Boundary getBounds() {
        return new QuadtreeImpl.Boundary(position.getX(), position.getY(), size.x, size.y);
    }

    @Override
    public double getX() {
        return position.getX();
    }

    @Override
    public double getY() {
        return position.getY();
    }

    @Override
    public void setLocation(double v, double v1) {
        position.setLocation(v, v1);
    }
}