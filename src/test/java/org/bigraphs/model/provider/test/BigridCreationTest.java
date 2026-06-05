package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.AbstractEcoreSignature;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.bigrid.*;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;
import org.bigraphs.model.provider.util.ResourceLoader;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.graphstream.ui.view.Viewer;
import org.bigraphs.testing.BigraphUnitTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * Tests that show how to create bigrids in various ways using generators or factories.
 *
 * @author Dominik Grzelak
 */
@Disabled
public class BigridCreationTest implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/bigrid/";

    @Test
    public void create_elem_bigrid() throws InvalidConnectionException, InterruptedException, IncompatibleSignatureException, IncompatibleInterfaceException {
        BiGridElementFactory factory = BiGridElementFactory.create();

        float stepSize = 1f;
        PureBigraph bigrid0 = factory.crossingFour(0, 0 * stepSize, stepSize);
        PureBigraph bigrid1 = factory.crossingFour(0, 1 * stepSize, stepSize);
        PureBigraph bigrid2 = factory.crossingFour(0, 2 * stepSize, stepSize);
        PureBigraph bigrid3 = factory.crossingFour(0, 3 * stepSize, stepSize);

        PureBigraph bigrid = //bigrid0;
                ops(bigrid0).parallelProduct(bigrid1)
//                        .parallelProduct(bigrid2)
//                        .parallelProduct(bigrid3)
                        .getOuterBigraph();

        GUI(bigrid0, true, true, "/graphStreamStyleLight-v2.css");
        GUI(bigrid1, true, true, "/graphStreamStyleLight-v2.css");
//        GUI(bigrid, true, true, "/graphStreamStyleLight-v2.css");

        while (true)
            Thread.sleep(10000);
    }

    @Test
    public void test_elementary() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException, InterruptedException {
        DynamicSignature sig = BiSpaceSignatureProvider.getInstance().getSignature();
//        PureBigraph a1 = pureBuilder(sig).root()
//                .child("Locale", "y0").down().child("Route", "y1")
//                .up()
//                .child("Locale", "y1")
//                .create();
////
//        GUI(a1, false, false, "/graphStreamStyleLight-v2.css");
//
//        PureBigraph b1 = pureBuilder(sig).root()
//                .child("Locale", "y0").down().child("Route", "y1")
//                .create();
//        GUI(b1, false, false, "/graphStreamStyleLight-v2.css");
//
//        PureBigraph b2 = pureBuilder(sig).root()
//                .child("Locale", "y1")
//                .create();
//        GUI(b2, false, false, "/graphStreamStyleLight-v2.css");
//        PureBigraph b3 = ops(b1).merge(b2).getOuterBigraph();
//        GUI(b3, false, false, "/graphStreamStyleLight-v2.css");

//        PureBigraph b4 = pureBuilder(sig).root()
//                .child("Locale", "y0").down().child("Route", "y1").site()
//                .up()
//                .child("Locale", "y1").down().site().child("Route", "y0")
//                .create();
//        GUI(b4, true, true, "/graphStreamStyleLight-v2.css");

//        PureBigraph b4b = pureBuilder(sig).root()
//                .child("Locale", "y0").down().child("Route", "y1").site()
//                .up()
//                .child("Locale", "y1").down()
//                /**/.child("Route", "y0").child("Route", "y2").site()
//                .up()
//                .child("Locale", "y2").down().child("Route", "y1").site()
//                .create();
//        GUI(b4b, true, true, "/graphStreamStyleLight-v2.css");

//        PureBigraph b5 = pureBuilder(sig).root()
//                .child("Locale", "y0").down().child("Route", "y1").child("Route", "y2")
//                .up()
//                .child("Locale", "y1").down().child("Route", "y0").child("Route", "y3")
//                .up()
//                .child("Locale", "y2").down().child("Route", "y0").child("Route", "y3")
//                .up()
//                .child("Locale", "y3").down().child("Route", "y1").child("Route", "y2")
//                .create();
//        GUI(b5, false, false, "/graphStreamStyleLight-v2.css");
//
//        PureBigraph b6 = pureBuilder(sig).root()
//                .child("Locale", "y0").down().child("Route", "y1").child("Route", "y3")
//                .up()
//                .child("Locale", "y1").down().child("Route", "y0").child("Route", "y2").child("Route", "y4")
//                .up()
//                .child("Locale", "y2").down().child("Route", "y1").child("Route", "y5")
//                .up()
//                .child("Locale", "y3").down().child("Route", "y0").child("Route", "y4").child("Route", "y6")
//                .up()
//                .child("Locale", "y4").down().child("Route", "y1").child("Route", "y3").child("Route", "y5").child("Route", "y7")
//                .up()
//                .child("Locale", "y5").down().child("Route", "y2").child("Route", "y4").child("Route", "y8")
//                .up()
//                .child("Locale", "y6").down().child("Route", "y3").child("Route", "y7")
//                .up()
//                .child("Locale", "y7").down().child("Route", "y4").child("Route", "y6").child("Route", "y8")
//                .up()
//                .child("Locale", "y8").down().child("Route", "y5").child("Route", "y7")
//                .create();
//        GUI(b6, false, false, "/graphStreamStyleLight-v2.css");

        while (true) {
            Thread.sleep(100);
        }
    }

    @Test
    void create_bigrid_nxm() throws Exception {
        int m = 2;
        int n = 3;
        BLocationModelData lmpd = BLocationModelDataFactory.createGrid(m, n, 0, 0, 1, 1f);
        String json = BLocationModelDataFactory.toJson(lmpd);
        writeToFile(json, DUMP_PATH + String.format("bigrid-%dx%d.json", m, n));


        // Create bigraph grid
        BiGridProvider provider = new BiGridProvider(lmpd)
                .setRouteDirection(BiGridProvider.RouteDirection.BIDIRECTIONAL);
        PureBigraph bigrid = provider.getBigraph();
        toPNG(bigrid, String.format("bigrid-%dx%d", m, n), DUMP_PATH);
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigrid, new FileOutputStream(String.format("bigrid-%dx%d.xmi", m, n)));
        print(bigrid);
        printMetaModel(bigrid);
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigrid.getSignature(), System.out);
        BigraphFileModelManagement.Store.exportAsMetaModel(bigrid.getSignature(), System.out);



        Set<Integer> isWallIndex = new HashSet<>(Arrays.asList(

        ));
        Set<Integer> isAgentIndex = new HashSet<>(Arrays.asList(
                3, 5, 4
        ));
        Set<Integer> isAgentIndexLocked = new HashSet<>(Arrays.asList(
                5, 4
        ));

        DynamicSignature newSig = BigraphUtil.mergeSignatures(bigrid.getSignature(), pureSignatureBuilder().add("Occupied", 0).add("Robot", 1).create());
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(newSig);

        PureBigraph bigrid1 = loadLocationModel(String.format("./bigrid-%dx%d.xmi", m, n), newSig);

        for (int i = 0; i < m * n; i++) {
            if (isWallIndex.contains(i)) {
                System.out.println("wall");
                b.root().child("Occupied");
            } else if (isAgentIndex.contains(i)) {
                System.out.println("agent");
                if(isAgentIndexLocked.contains(i)) {
                    b.root().child("Robot", "s");
                } else {
                    b.root().child("Robot");
                }
            } else {
                b.root();
            }
        }

        PureBigraph occupancyBigraph = b.create();
        toPNG(occupancyBigraph, String.format("bigrid-%dx%d-occupancy", m, n), DUMP_PATH);

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

        GUI(occupancyBigraph, true, true, "/graphStreamStyleLight-v2.css");
        GUI(bigrid1, true, false, "/graphStreamStyleLight-v2.css");

        PureBigraph world = ops(bigrid1).nesting(occupancyBigraph).getOuterBigraph();
        GUI(world, true, false, "/graphStreamStyleLight-v2.css");

        while (true)
            Thread.sleep(12000);
    }

    @Test
    public void test_convexShape_PointList() throws InvalidConnectionException, IOException, InterruptedException {
//        float stepSize = 0.25f;
//        float padding = 0.2f;
//        List<Point2D.Float> convexPoints = new LinkedList<>();
//        convexPoints.add(new Point2D.Float(0f, 0f));
//        convexPoints.add(new Point2D.Float(0.0f, 1.75f));
//        convexPoints.add(new Point2D.Float(1.5f, 1.5f));
//        convexPoints.add(new Point2D.Float(1.5f, 0.0f));

//        float stepSize = 0.3f;
//        float padding = 0f;
//        List<Point2D.Float> convexPoints = new LinkedList<>();
//        convexPoints.add(new Point2D.Float(0f, 0f));
//        convexPoints.add(new Point2D.Float(-1.24f, 0.58f));
//        convexPoints.add(new Point2D.Float(2.86f, 2.93f));
//        convexPoints.add(new Point2D.Float(3.08f, 0f));

//        List<Point2D.Float> convexPoints = new LinkedList<>();
//        convexPoints.add(new Point2D.Float(0f, 0f));
//        convexPoints.add(new Point2D.Float(0.03f, 2.68f));
//        convexPoints.add(new Point2D.Float(3.72f, -0.19f));
//        convexPoints.add(new Point2D.Float(0.88f, -1.57f));

        float stepSize = 0.5f;
        float padding = 0.7f;
        List<Point2D.Float> convexPoints = new LinkedList<>();
        convexPoints.add(new Point2D.Float(0f, 0f));
        convexPoints.add(new Point2D.Float(0f, 1.5f));
        convexPoints.add(new Point2D.Float(4.5f, 0.15f));
        convexPoints.add(new Point2D.Float(1.70f, -2.65f));

        PureBigraph result = ConvexShapeBuilder.generateSingleRoot(convexPoints, stepSize, padding, BiGridElementFactory.create());
        BigraphFileModelManagement.Store.exportAsInstanceModel(result, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(result, new FileOutputStream(DUMP_PATH + "test.xmi"));
        BigraphFileModelManagement.Store.exportAsMetaModel(result, new FileOutputStream(DUMP_PATH + "test.ecore"));
        BigraphFileModelManagement.Store.exportAsMetaModel(result.getSignature(), new FileOutputStream(DUMP_PATH + "sig.ecore"));
        BigraphFileModelManagement.Store.exportAsInstanceModel(result.getSignature(), new FileOutputStream(DUMP_PATH + "sig.xmi"));
        GUI(result, true, false);
        System.out.println("Roots: " + result.getRoots().size());
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
        BigraphFileModelManagement.Store.exportAsInstanceModel(generated, new FileOutputStream(DUMP_PATH + "generated.xmi"));
        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(generated)
                .renderSites(false)
                .renderRoots(false);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(10000);

    }

    @Test
    void bigrid_diagonal() throws Exception {
        int m = 4;
        int n = 4;
        BLocationModelData lmpd = BLocationModelDataFactory.createGrid(m, n, 0, 0, 1, 1f);
        DiagonalDirectionalBiGridProvider provider = new DiagonalDirectionalBiGridProvider(lmpd, m, n);
        provider.makeGround(false);
        PureBigraph bigrid = provider.getBigraph();

        GUI(bigrid, false, false);
        System.out.println("Roots: " + bigrid.getRoots().size());
        while (true)
            Thread.sleep(10000);
    }

    @Test
    void bigrid_3d() throws Exception {
        int m = 5;
        int n = 5;
        BLocationModelData lmpd = new BLocationModelData();
        ThreeDimensionalBiGridProvider provider = new ThreeDimensionalBiGridProvider(lmpd, m, n, 3, 0, 0, 0, 1, 1, 0.5f);
        provider.makeGround(false);
        PureBigraph bigrid = provider.getBigraph();

        GUI(bigrid, false, false);
        System.out.println("Roots: " + bigrid.getRoots().size());
        while (true)
            Thread.sleep(10000);
    }

    private static PureBigraph loadLocationModel(String resourceFilename,
                                                DynamicSignature sig) throws IOException {
//        String resourcePath = resourceFilename;
        InputStream in = ResourceLoader.getResourceStream(resourceFilename);

        if (in == null) {
            throw new IllegalStateException("Resource not found: " + resourceFilename);
        }


        Path tempFile = Files.createTempFile("bigraph-temp-", ".xmi");
        Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        tempFile.toFile().deleteOnExit();


        EPackage ePackage = createOrGetBigraphMetaModel(sig);
        EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);


        List<EObject> eObjects =
                BigraphFileModelManagement.Load
                        .bigraphInstanceModel(tempFile.toAbsolutePath().toString());

        return BigraphUtil.toBigraph(ePackage, eObjects.get(0), sig);
    }
}
