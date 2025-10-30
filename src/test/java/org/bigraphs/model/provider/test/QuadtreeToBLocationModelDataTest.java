package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.bigrid.BLocationModelDataFactory;
import org.bigraphs.model.provider.spatial.bigrid.BiGridProvider;
import org.bigraphs.model.provider.spatial.quadtree.JQuadtreeVisualizer;
import org.bigraphs.model.provider.spatial.quadtree.QuadItem;
import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeImpl;
import org.bigraphs.model.provider.spatial.quadtree.impl.QuadtreeConvert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testng.annotations.BeforeMethod;

import javax.swing.*;
import java.awt.geom.Point2D;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;

/**
 * Demonstration of the quadtree implementation
 *
 * @author Dominik Grzelak
 */
@Disabled
public class QuadtreeToBLocationModelDataTest implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/bigrid_quadtree/";

    @BeforeMethod
    public void setUp() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void gui_random_insertion_and_deletion() throws Exception {
        double areaSizeW = 500;
        double areaSizeH = 500;
        double marginAgent = 10;
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
        for (int i = 0; i < 12; i++) {
            int x = (int) (Math.random() * boundary.width);
            int y = (int) (Math.random() * boundary.height);
            Point2D.Double p = new Point2D.Double(x, y);
            quadtree.insert(QuadItem.create(p));
            Thread.sleep(50);
        }

        Thread.sleep(250);

        QuadtreeConvert converter = new QuadtreeConvert();
        BLocationModelData dataModel = converter.createBLocationModelDataFromQuadtree(quadtree);
        System.out.println(dataModel);
        System.out.println("dataModel.getLocales().size()=" + dataModel.getLocales().size());
        System.out.println("dataModel.getRoutes().size()=" + dataModel.getRoutes().size());

        BiGridProvider provider = new BiGridProvider(dataModel)
                .setRouteDirection(BiGridProvider.RouteDirection.UNIDIRECTIONAL_FORWARD);
        PureBigraph bigrid = provider.getBigraph();

        String json = BLocationModelDataFactory.toJson(dataModel);
        writeToFile(json, DUMP_PATH + String.format("bigrid-quadtree.json"));


        GUI(bigrid, true, false);

        Bigraph<DynamicSignature> pr = ops(bigrid)
                .parallelProduct(bigrid)
                .parallelProduct(bigrid)
                .getOuterBigraph();
        GUI((PureBigraph) pr, true, false);

        while (true) {
            Thread.sleep(1000);
        }
    }

    private static double[] pos(final double x, final double y) {
        return new double[]{x, y};
    }


}
