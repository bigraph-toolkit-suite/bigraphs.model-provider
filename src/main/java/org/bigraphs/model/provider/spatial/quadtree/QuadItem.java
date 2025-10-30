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
    String id;
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

    public static QuadItem create(String id, double x, double y) {
        return create(id, new Point2D.Double(x, y));
    }

    public static QuadItem create(String id, Point2D pos) {
        return new QuadItem(id, (Double) pos);
    }

    public static QuadItem create(double x, double y) {
        return create(new Point2D.Double(x, y));
    }

    public static QuadItem create(Point2D pos) {
        return new QuadItem((Double) pos);
    }

    public QuadItem(String id, Point2D.Double pos) {
        this(id, pos, null, null, null);
    }

    public QuadItem(Point2D.Double pos) {
        this(pos, null);
    }

    public QuadItem(Point2D.Double pos, Color color) {
        this("", pos, null, null, color);
    }

    public QuadItem(String id, Point2D.Double pos, Point2D.Double vel, Point2D.Double size, Color color) {
        this.id = id;
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