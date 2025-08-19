package org.bigraphs.model.provider.spatial.quadtree;

import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeImpl;
import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Visualize a quadtree in a GUI for debugging purposes.
 *
 * @author Dominik Grzelak
 */
public class JQuadtreeVisualizer extends JPanel implements QuadtreeListener, KeyListener {
    private final QuadtreeImpl quadtree;
    private boolean applyTransformations = false;
    private double scale = 1.0; // initial scale factor for zooming
    public JQuadtreeVisualizer(QuadtreeImpl quadtree) {
        this.quadtree = quadtree;
        double[] spans = QuadtreeImpl.getSpans(quadtree.getBoundary());
        int realWidth = (int) spans[0]; // quadtree.getBoundary().getWidth(); //int) spans[0];
        int realHeight = (int) spans[1]; // quadtree.getBoundary().getHeight();
        setPreferredSize(new Dimension(realWidth, realHeight));
        quadtree.addListener(this);  // Register this class as a listener to the quadtree
        addKeyListener(this);
        setFocusable(true);  // Ensure the panel can receive key events
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        AffineTransform originalTransform = g2d.getTransform();
        g2d.scale(scale, scale);


        // Apply transformations if the flag is true
        if (applyTransformations) {
            // Mirror vertically (flip horizontally)
            System.out.println("applyTransformations = " + applyTransformations);
            int width = getWidth();
            int height = getHeight();

            // ROTATION
            g2d.translate(width / 2, height / 2);  // Move the origin to the center
            g2d.rotate(-Math.PI / 2);  // Rotate around the center
            g2d.translate(-width / 2, -height / 2);  // Move the origin back to the top-left corner

            // Horizontal Mirror
            g2d.translate(width, 0);  // Move the origin to the right edge
            g2d.scale(-1, 1);         // Flip horizontally
        }

        // Draw the quadtree structure recursively
        drawQuadtree(g2d, quadtree);

//        if (applyTransformations) {
//        g2d.setTransform(originalTransform);
//         }

    }

    private void drawQuadtree(Graphics2D g2d, QuadtreeImpl node) {
        if (node == null) return;

        // Draw the boundary of this quadtree node
        QuadtreeImpl.Boundary boundary = node.getBoundary();
        int scaledX = (int) (boundary.getX() * scale);
        int scaledY = (int) (boundary.getY() * scale);
        int scaledWidth = (int) (boundary.getWidth() * scale);
        int scaledHeight = (int) (boundary.getHeight() * scale);

        g2d.setColor(Color.LIGHT_GRAY); // Color for the grid lines
//        g2d.drawRect((int) boundary.x, (int) boundary.y, (int) boundary.width, (int) boundary.height);
        g2d.drawRect(scaledX, scaledY, scaledWidth, scaledHeight);

        // Draw the points in this node
        g2d.setColor(Color.RED); // Color for points
        for (Point2D point : node.getPoints()) {
//            g2d.fillOval((int) point.getX() - 2, (int) point.getY() - 2, 4, 4); // Small circles for points
            int scaledPointX = (int) (point.getX() * scale);
            int scaledPointY = (int) (point.getY() * scale);
            // Use a size that scales with the zoom level
            int circleSize = (int) (0.5 * scale);
            g2d.fillOval(scaledPointX - circleSize / 2, scaledPointY - circleSize / 2, circleSize, circleSize);
        }

        // Recursively draw subdivisions if this node is divided
        if (node.isDivided()) {
            drawQuadtree(g2d, node.getNortheast());
            drawQuadtree(g2d, node.getNorthwest());
            drawQuadtree(g2d, node.getSoutheast());
            drawQuadtree(g2d, node.getSouthwest());
        }
    }

    @Override
    public void onPointInserted(Point2D point) {
        repaint(); // Repaint the panel whenever a point is inserted
    }

    @Override
    public void onPointDeleted(Point2D point) {
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("key pressed = " + keyCode);
        if (keyCode == KeyEvent.VK_PLUS || keyCode == KeyEvent.VK_EQUALS) { // Zoom in with + or =
            scale += 0.1;
            repaint();
        } else if (keyCode == KeyEvent.VK_MINUS) { // Zoom out with "-"
            scale = Math.max(0.1, scale - 0.1); // Prevent scale from going below 0.1
            repaint();
        } else if (keyCode == KeyEvent.VK_T) { // Toggle transformations with "T" key
            System.out.println("Toggle: Mirror horizontally and then rotate 90° left (" + applyTransformations + ")");
            applyTransformations = !applyTransformations;
            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
