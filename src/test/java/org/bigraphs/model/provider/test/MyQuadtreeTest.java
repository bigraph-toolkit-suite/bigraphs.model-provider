package org.bigraphs.model.provider.test;

import org.bigraphs.model.provider.spatial.quadtree.impl.MaxDepthReachedException;
import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeImpl;
import org.bigraphs.model.provider.spatial.quadtree.JQuadtreeVisualizer;
import org.bigraphs.model.provider.util.QuadTreeUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testng.annotations.BeforeMethod;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Demonstration of the quadtree implementation
 *
 * @author Dominik Grzelak
 */
@Disabled
public class MyQuadtreeTest implements BigraphUnitTestSupport {
//    static final String DUMP_PATH = "src/test/resources/dump/bigrid/";

    @BeforeMethod
    public void setUp() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void gui_jitter_simulation() throws InterruptedException {
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
        int jitterAmount = 8;
        int timeSleepBetweenCycles = 30;

        for (int i = 0; i < jitterCycles; i++) {
            List<Point2D> pointsQueried = new LinkedList<>(quadtree.queryRange(boundary));
//            System.out.println(Arrays.toString(pointsQueried.toArray()));
            List<Point2D> pointsJittered = QuadTreeUtils.jitterPoints(pointsQueried, jitterAmount);
            for (int j = 0; j < pointsQueried.size(); j++) {
                try {
                    quadtree.delete(pointsQueried.get(j));
                    quadtree.insert(pointsJittered.get(j));
                } catch (MaxDepthReachedException e) {
                    e.printStackTrace();
                    try {
                        quadtree.insert(pointsQueried.get(j));
                    } catch (MaxDepthReachedException e1) {
                        e1.printStackTrace();
                    }
                }
//                Thread.sleep(80);
            }
            Thread.sleep(timeSleepBetweenCycles);
            quadtree.cleanup();
        }

        while (true) {
            Thread.sleep(1000);
        }

//        System.out.println("Deleting now points");
//        Thread.sleep(1000);
//        pointsQueried = quadtree.queryRange(boundary);
//        System.out.println(pointsQueried.size());
//        System.out.println(Arrays.toString(pointsQueried.toArray()));

//        List<SomeObjectWithArea> objects = new ArrayList<>();
//        for (int i = 0; i < 150; i++) {
//            SomeObjectWithArea obj = new SomeObjectWithArea(
//                    new Point2D.Double(Math.random() * areaSizeW, Math.random() * areaSizeH), // random position
//                    new Point2D.Double(Math.random() * 2 - 1, Math.random() * 2 - 1),       // random velocity
//                    new Point2D.Double(5.0, 5.0),                                           // fixed size
//                    new Color((float) Math.random(), (float) Math.random(), (float) Math.random()) // random color
//            );
//            objects.add(obj);
//        }

    }

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
     * insertion and range query
     *
     * @throws InterruptedException
     */
    @Test
    void test_quadtree() throws InterruptedException {
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
        for (Point2D point : pointsInRange) {
            System.out.println(point);
        }
    }

    private static double[] pos(final double x, final double y) {
        return new double[]{x, y};
    }


}
