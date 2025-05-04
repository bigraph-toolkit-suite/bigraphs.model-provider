package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.bigrid.*;
import org.graphstream.ui.view.Viewer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BigridCreationTest implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/bigrid/";

    @BeforeMethod
    public void setUp() {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");
    }

    @Test
    void create_bigrid_nxm() throws Exception {
        int m = 4;
        int n = 4;
        BLocationModelData lmpd = BLocationModelDataFactory.createGrid(m, n, 0, 0, 1, 1f);
        String json = BLocationModelDataFactory.toJson(lmpd);
        writeToFile(json, DUMP_PATH + String.format("bigrid-%dx%d.json", m, n));

        // Create bigraph grid
        BiGridProvider provider = new BiGridProvider(lmpd)
                .setRouteDirection(BiGridProvider.RouteDirection.BIDIRECTIONAL);
        PureBigraph bigrid = provider.getBigraph();
        eb(bigrid, String.format("bigrid-%dx%d", m, n), DUMP_PATH);
        print(bigrid);
        printMetaModel(bigrid);
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigrid.getSignature(), System.out);
        BigraphFileModelManagement.Store.exportAsMetaModel(bigrid.getSignature(), System.out);


//        PureLinkGraphConnectedComponents cc = new PureLinkGraphConnectedComponents();
//        cc.decompose(bigrid);
//        List<PureBigraph> connectedComponents = cc.getConnectedComponents();
//        PureLinkGraphConnectedComponents.UnionFind uf = cc.getUnionFindDataStructure();
//        System.out.println("Connected Components: " + uf.getCount());
//        System.out.println("Connected Components: " + connectedComponents.size());
//        System.out.println("# of Partition Roots: " + uf.countRoots(uf.getChildParentMap()));
//        Set<Integer> rootsOfPartitions = uf.getRootsOfPartitions(uf.getChildParentMap());
//        System.out.println("rootsOfPartitions: " + rootsOfPartitions);
////        System.out.println(uf.getRank());
//        Map<Integer, List<BigraphEntity<?>>> partitions = cc.getPartitions();
//        System.out.println("partitions: " + partitions);


        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(bigrid)
                .renderSites(false)
                .renderRoots(false);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(10000);
    }

    @Test
    public void test_convexShape_PointList() throws InvalidConnectionException, IOException, InterruptedException {
        float stepSize = 0.2f;
        Point2D.Float originPoint = new Point2D.Float(0, 0);
        List<Point2D.Float> convexPoints = new LinkedList<>();
        convexPoints.add(originPoint);
        convexPoints.add(new Point2D.Float(0, 1.35f));
        convexPoints.add(new Point2D.Float(4.75f, 0.5f));
        convexPoints.add(new Point2D.Float(1.5f, -2.8f));

        PureBigraph result = ConvexShapeBuilder.generateAsSingle(convexPoints, stepSize, BiGridElementFactory.create());
        BigraphFileModelManagement.Store.exportAsInstanceModel(result, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(result, new FileOutputStream("test.xmi"));
        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(result)
                .renderSites(false)
                .renderRoots(false);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(10000);
    }

    @Test
    public void test_linearRegressionBuilder() throws Exception {

        List<Point2D.Float> originalPoints = List.of(
                new Point2D.Float(0, 0),
                new Point2D.Float(1, 1),
                new Point2D.Float(2, 2)
        );

        PureBigraph generated = LinearInterpolationBuilder.generate(originalPoints, 0.5f,0.5f);
        BigraphFileModelManagement.Store.exportAsInstanceModel(generated, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(generated, new FileOutputStream("src/test/resources/dump/generated.xmi"));
        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(generated)
                .renderSites(false)
                .renderRoots(false);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(10000);

    }
}
