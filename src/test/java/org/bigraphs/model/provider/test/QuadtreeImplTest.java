package org.bigraphs.model.provider.test;

import org.bigraphs.model.provider.spatial.quadtree.impl.MaxDepthReachedException;
import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeImpl;
import org.bigraphs.model.provider.spatial.quadtree.JQuadtreeVisualizer;
import org.bigraphs.model.provider.util.Point2DUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testng.annotations.BeforeMethod;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * Demonstration of the quadtree implementation.
 * <p>
 * GUI windows are opened to demonstrate some quadtree applications.
 *
 * @author Dominik Grzelak
 */
@Disabled
public class QuadtreeImplTest implements BigraphUnitTestSupport {

    /**
     * Allow the Swing GUI to be drawn on the screen.
     */
    @BeforeMethod
    public void setUp() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void test_simple_quadtree() throws InterruptedException {
        QuadtreeImpl.Boundary boundary = new QuadtreeImpl.Boundary(-5, -5, 10, 10);
        double marginPoint = 0.1;
        int maxTreeDepth = QuadtreeImpl.getMaxTreeDepthFrom(boundary, marginPoint);
        int maxPointsPerLeaf = 1;  // Configurable max points
        QuadtreeImpl quadtree = new QuadtreeImpl(boundary, maxPointsPerLeaf, maxTreeDepth);

        List<Point2D> data = new ArrayList<>();
        data.add(new Point2D.Double(1, 1));
        data.add(new Point2D.Double(4, 4));
        data.add(new Point2D.Double(4, 0.2));
        data.add(new Point2D.Double(4, -0.2));
        data.add(new Point2D.Double(4, -0.1));
        data.add(new Point2D.Double(4, 0.4));
        data.forEach(p -> {
            try {
                quadtree.insert(p);
            } catch (MaxDepthReachedException e) {
                System.out.println("Collision detected for point: " + p);
            }
        });

        // Define range for query and print points found in range
        QuadtreeImpl.Boundary range = new QuadtreeImpl.Boundary(0, -0.3, 4.1, 1);
        List<Point2D> pointsInRange = quadtree.queryRange(range);
        System.out.println("Points in range " + range + ":");
        assert pointsInRange.size() == 4;
        for (Point2D point : pointsInRange) {
            System.out.println(point);
        }

        JFrame frame = new JFrame("Agent Position with Jitter (Simulation)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JQuadtreeVisualizer(quadtree));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Thread.sleep(60*5*1000);
    }

    /**
     * A smaller and larger circle is drawn using the point cloud.
     * <p>
     * Draws two circular particle filters to approximate the outline of a robotic arm.
     */
    @Test
    void gui_circle_jitter() throws InterruptedException {
        double areaSizeW = 400;
        double areaSizeH = 400;
        double marginAgent = 2;
        int td0 = (int) (Math.ceil(Math.log(areaSizeW / marginAgent) / Math.log(2d))) + 1;
        int td1 = (int) (Math.ceil(Math.log(areaSizeH / marginAgent) / Math.log(2d))) + 1;
        int td = Math.min(td0, td1);
        System.out.println("Max Tree Depth = " + td);
        int numAgentsPerRow = (int) Math.floor(areaSizeW / marginAgent);
        int numAgentsPerCol = (int) Math.floor(areaSizeH / marginAgent);
        System.out.println("NumAgentsPerRow = " + numAgentsPerRow);
        System.out.println("NumAgentsPerRow = " + numAgentsPerCol);
        // Define boundary for quadtree
//        QuadtreeImpl.Boundary boundary = new QuadtreeImpl.Boundary(0, 0, areaSizeW, areaSizeH);
        QuadtreeImpl.Boundary boundary = new QuadtreeImpl.Boundary(-5, -5, areaSizeW, areaSizeH);

        int maxPointsPerLeaf = 1;  // Configurable max points
        int maxTreeDepth = td;      // Configurable max depth

        QuadtreeImpl quadtree = new QuadtreeImpl(boundary, maxPointsPerLeaf, maxTreeDepth);

        // Create the visualization window
        JFrame frame = new JFrame("Agent Position with Jitter (Simulation)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JQuadtreeVisualizer(quadtree));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        System.out.println("Creating now points");
        List<Point2D> pointsOnCircleBase = Point2DUtils.pointCircle(new Point2D.Double(200, 200), 50, 10);
        pointsOnCircleBase.add(new Point2D.Double(0, 0));
        pointsOnCircleBase.forEach(p -> {
            try {
                quadtree.insert(p);
                Thread.sleep(50);
            } catch (MaxDepthReachedException e) {
                System.out.println("Collision detected for point: " + p);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        List<Point2D> pointsOnCircleGripper = Point2DUtils.pointCircle(new Point2D.Double(200, 80), 10, 5);
        pointsOnCircleGripper.add(new Point2D.Double(1, 1));
        pointsOnCircleGripper.forEach(p -> {
            try {
                quadtree.insert(p);
                Thread.sleep(50);
            } catch (MaxDepthReachedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        int jitterCycles = 10000;
        double jitterAmount = 0.1;
        int timeSleepBetweenCycles = 30;

        for (int i = 0; i < jitterCycles; i++) {
            List<Point2D> pointsQueried = new LinkedList<>(quadtree.queryRange(boundary));
//            System.out.println(Arrays.toString(pointsQueried.toArray()));
            List<Point2D> pointsJittered = Point2DUtils.jitterPoints(pointsQueried, jitterAmount);
            for (int j = 0; j < pointsQueried.size(); j++) {
                try {
                    quadtree.delete(pointsQueried.get(j));
                    quadtree.insert(pointsJittered.get(j));
                } catch (MaxDepthReachedException e) {
                    System.out.println("Collision detected for point: " + pointsQueried.get(j));
                    try {
                        quadtree.insert(pointsQueried.get(j));
                    } catch (MaxDepthReachedException e1) {
                        System.out.println("Collision detected for point: " + pointsQueried.get(j));
                    }
                }
            }
            Thread.sleep(timeSleepBetweenCycles);
            quadtree.cleanup();
        }

        while (true) {
            Thread.sleep(1000);
        }
    }

    /**
     * Creates random points and inserts them into the quadtree.
     * All points are jittered and then inserted into the quadtree again.
     * If collisions occur, the point is not inserted.
     */
    @Test
    void gui_jitter() throws InterruptedException {
        double areaSizeW = 400;
        double areaSizeH = 400;
        double marginAgent = 20;
        int td0 = (int) (Math.ceil(Math.log(areaSizeW / marginAgent) / Math.log(2d))) + 1;
        int td1 = (int) (Math.ceil(Math.log(areaSizeH / marginAgent) / Math.log(2d))) + 1;
        int td = Math.min(td0, td1);
        System.out.println("Max Tree Depth = " + td);
        int numAgentsPerRow = (int) Math.floor(areaSizeW / marginAgent);
        int numAgentsPerCol = (int) Math.floor(areaSizeH / marginAgent);
        System.out.println("NumAgentsPerRow = " + numAgentsPerRow);
        System.out.println("NumAgentsPerRow = " + numAgentsPerCol);
        // Define boundary for quadtree
        QuadtreeImpl.Boundary boundary = new QuadtreeImpl.Boundary(0, 0, areaSizeW, areaSizeH);
        int maxPointsPerLeaf = 1;  // Configurable max points
        int maxTreeDepth = td;      // Configurable max depth

        QuadtreeImpl quadtree = new QuadtreeImpl(boundary, maxPointsPerLeaf, maxTreeDepth);

        // Create the visualization window
        JFrame frame = new JFrame("Agent Position with Jitter (Simulation)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JQuadtreeVisualizer(quadtree));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        System.out.println("Creating now points");
        // Insert random points into the quadtree for visualization
        for (int i = 0; i < 60; i++) {
            try {
                int x = (int) (Math.random() * boundary.width);
                int y = (int) (Math.random() * boundary.height);
                Point2D.Double p = new Point2D.Double(x, y);
                quadtree.insert(p);
                Thread.sleep(50);
            } catch (MaxDepthReachedException e) {
                e.printStackTrace();
            }
        }


        int jitterCycles = 10000;
        double jitterAmount = 0.37;
        int timeSleepBetweenCycles = 10;

        for (int i = 0; i < jitterCycles; i++) {
            List<Point2D> pointsQueried = new LinkedList<>(quadtree.queryRange(boundary));
            List<Point2D> pointsJittered = Point2DUtils.jitterPoints(pointsQueried, jitterAmount);
            for (int j = 0; j < pointsQueried.size(); j++) {
                try {
                    quadtree.delete(pointsQueried.get(j));
                    quadtree.insert(pointsJittered.get(j));
                } catch (MaxDepthReachedException e) {
                    System.out.println("Collision detected for point: " + pointsQueried.get(j));
                    try {
                        quadtree.insert(pointsQueried.get(j));
                    } catch (MaxDepthReachedException e1) {
                        System.out.println("2nd Collision detected for point: " + pointsQueried.get(j));
                    }
                }
            }
            Thread.sleep(timeSleepBetweenCycles);
            quadtree.cleanup();
        }

        while (true) {
            Thread.sleep(1000);
        }
    }

    /**
     * Random points are inserted into the quadtree and then deleted one by one again.
     */
    @Test
    void gui_random_insertion_and_deletion() throws InterruptedException {
        double areaSizeW = 400;
        double areaSizeH = 400;
        double marginAgent = 30;
        int td0 = (int) (Math.ceil(Math.log(areaSizeW / marginAgent) / Math.log(2d))) + 1;
        int td1 = (int) (Math.ceil(Math.log(areaSizeH / marginAgent) / Math.log(2d))) + 1;
        int td = Math.min(td0, td1);
        System.out.println("Max Tree Depth = " + td);
        int numAgentsPerRow = (int) Math.floor(areaSizeW / marginAgent);
        int numAgentsPerCol = (int) Math.floor(areaSizeH / marginAgent);
        System.out.println("NumAgentsPerRow = " + numAgentsPerRow);
        System.out.println("NumAgentsPerRow = " + numAgentsPerCol);
        // Define boundary for quadtree
        QuadtreeImpl.Boundary boundary = new QuadtreeImpl.Boundary(0, 0, areaSizeW, areaSizeH);
        int maxPointsPerLeaf = 1;  // Configurable max points
        int maxTreeDepth = td;      // Configurable max depth

        QuadtreeImpl quadtree = new QuadtreeImpl(boundary, maxPointsPerLeaf, maxTreeDepth);

        // Create the visualization window
        JFrame frame = new JFrame("Quadtree Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JQuadtreeVisualizer(quadtree));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Thread.sleep(250);

        System.out.println("Creating now points");
        // Insert random points into the quadtree for visualization
        for (int i = 0; i < 50; i++) {
            Point2D.Double p = null;
            try {
                int x = (int) (Math.random() * boundary.width);
                int y = (int) (Math.random() * boundary.height);
                p = new Point2D.Double(x, y);
                quadtree.insert(p);
                Thread.sleep(50);
            } catch (MaxDepthReachedException e) {
                System.out.println("Collision detected for point: " + Objects.requireNonNull(p));
            }
        }

        System.out.println("Deleting now points");
        List<Point2D> pointsQueried = quadtree.queryRange(boundary);
        System.out.println(Arrays.toString(pointsQueried.toArray()));
        Thread.sleep(1000);
        pointsQueried.forEach(point -> {
            try {
                Thread.sleep(250);
                boolean delete = quadtree.delete(point);
                System.out.println("deleted? " + delete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * API Test: Insertion and range query
     */
    @Test
    void test_insertion_and_range() throws InterruptedException {
        // Define boundary for quadtree
        QuadtreeImpl.Boundary boundary = new QuadtreeImpl.Boundary(0, 0, 100, 100);
        int maxPointsPerLeaf = 1;  // Configurable max points
        int maxTreeDepth = 4;      // Configurable max depth

        QuadtreeImpl quadtree = new QuadtreeImpl(boundary, maxPointsPerLeaf, maxTreeDepth);

        // Insert points into the quadtree
        quadtree.insert(new Point2D.Double(10, 10));
        quadtree.insert(new Point2D.Double(15, 15));
        quadtree.insert(new Point2D.Double(30, 30));
//        quadtree.insert(new Point2D.Double(35, 35));
        quadtree.insert(new Point2D.Double(38, 38));
//        quadtree.insert(new Point2D.Double(45, 45));
        quadtree.insert(new Point2D.Double(50, 51));

        // Define range for query
        QuadtreeImpl.Boundary range = new QuadtreeImpl.Boundary(30, 30, 60, 60);
        List<Point2D> pointsInRange = quadtree.queryRange(range);

        // Print points found in range
        System.out.println("Points in range " + range + ":");
        assert pointsInRange.size() == 3;
        for (Point2D point : pointsInRange) {
            System.out.println(point);
        }
    }
}
