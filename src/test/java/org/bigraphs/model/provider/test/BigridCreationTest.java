package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.bigrid.*;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;

public class BigridCreationTest implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/bigrid/";

    @BeforeMethod
    public void setUp() {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");
    }

    @Test
    public void test_elementary() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException, InterruptedException {
        DynamicSignature sig = BiSpaceSignatureProvider.getInstance().getSignature();
        PureBigraph a1 = pureBuilder(sig).root()
                .child("Locale", "y0").down().child("Route", "y1")
                .up()
                .child("Locale", "y1")
                .create();

        GUI(a1, true, true);

        PureBigraph b1 = pureBuilder(sig).root()
                .child("Locale", "y0").down().child("Route", "y1")
                .create();
        GUI(b1, true, true);

        PureBigraph b2 = pureBuilder(sig).root()
                .child("Locale", "y1")
                .create();
        GUI(b2, true, true);
        PureBigraph b3 = ops(b1).merge(b2).getOuterBigraph();
        GUI(b3, true, true);

        while (true) {
            Thread.sleep(100);
        }
    }

    @Test
    void create_bigrid_nxm() throws Exception {
        int m = 2;
        int n = 5;
        BLocationModelData lmpd = BLocationModelDataFactory.createGrid(m, n, 0, 0, 1, 1f);
        String json = BLocationModelDataFactory.toJson(lmpd);
        writeToFile(json, DUMP_PATH + String.format("bigrid-%dx%d.json", m, n));


        // Create bigraph grid
        BiGridProvider provider = new BiGridProvider(lmpd)
                .setRouteDirection(BiGridProvider.RouteDirection.BIDIRECTIONAL);
        PureBigraph bigrid = provider.getBigraph();
        eb(bigrid, String.format("bigrid-%dx%d", m, n), DUMP_PATH);
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigrid, new FileOutputStream(String.format("bigrid-%dx%d.xmi", m, n)));
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

        GUI(bigrid, false, false);

        while (true)
            Thread.sleep(12000);
    }

    @Test
    public void test_convexShape_PointList() throws InvalidConnectionException, IOException, InterruptedException {
        float stepSize = 0.45f;
//        Point2D.Float originPoint = new Point2D.Float(0, 0);
//        List<Point2D.Float> convexPoints = new LinkedList<>();
//        convexPoints.add(originPoint);
//        convexPoints.add(new Point2D.Float(0, 1.35f));
//        convexPoints.add(new Point2D.Float(4.75f, 0.5f));
//        convexPoints.add(new Point2D.Float(1.5f, -2.8f));

        List<Point2D.Float> convexPoints = new LinkedList<>();
        convexPoints.add(new Point2D.Float(0f, 0f));
        convexPoints.add(new Point2D.Float(-1.24f, 0.58f));
        convexPoints.add(new Point2D.Float(2.86f, 2.93f));
        convexPoints.add(new Point2D.Float(3.08f, 0f));

//        List<Point2D.Float> convexPoints = new LinkedList<>();
//        convexPoints.add(new Point2D.Float(0f, 0f));
//        convexPoints.add(new Point2D.Float(0.03f, 2.68f));
//        convexPoints.add(new Point2D.Float(3.72f, -0.19f));
//        convexPoints.add(new Point2D.Float(0.88f, -1.57f));

        PureBigraph result = ConvexShapeBuilder.generateAsSingle(convexPoints, stepSize, BiGridElementFactory.create());
        BigraphFileModelManagement.Store.exportAsInstanceModel(result, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(result, new FileOutputStream("test.xmi"));
        GUI(result, true, false);
        while (true)
            Thread.sleep(10000);
    }

    @Test
    public void test_linearInterpolationBuilder() throws Exception {

        List<Point2D.Float> originalPoints = List.of(
                new Point2D.Float(0, 0),
                new Point2D.Float(1, 1)
//                new Point2D.Float(1, 1),
//                new Point2D.Float(1, 0)
        );

        PureBigraph generated = LinearInterpolationBuilder.generate(originalPoints, 0.25f, 0.25f);
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
