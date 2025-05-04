package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.EcoreBigraph;
import org.bigraphs.framework.core.analysis.BigraphDecomposer;
import org.bigraphs.framework.core.analysis.PureBigraphDecomposerImpl;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.spatial.bigrid.BiGridElementFactory;
import org.bigraphs.model.provider.spatial.bigrid.BiGridConnectivityCheckerDFS;
import org.bigraphs.model.provider.spatial.bigrid.World;
import org.bigraphs.model.provider.spatial.bigrid.BiGridConnectivityChecker;
import org.graphstream.ui.swing_viewer.util.DefaultShortcutManager;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;
import static org.bigraphs.framework.core.factory.BigraphFactory.purePlacings;

public class BigridCreationWFCTest implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/bigrid/";

    @BeforeMethod
    public void setUp() {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");
    }

    @Test
    public void create_elem_bigrid() throws InvalidConnectionException, InterruptedException, IncompatibleSignatureException, IncompatibleInterfaceException {
        BiGridElementFactory factory = BiGridElementFactory.create();

        float stepSize = 0.1f;
        PureBigraph bigrid0 = factory.crossingFour(0, 0*stepSize, stepSize);
        PureBigraph bigrid1 = factory.crossingFour(0, 1*stepSize, stepSize);
        PureBigraph bigrid2 = factory.crossingFour(0, 2*stepSize, stepSize);
        PureBigraph bigrid3 = factory.crossingFour(0, 3*stepSize, stepSize);

        PureBigraph bigrid = //bigrid0;
                ops(bigrid0).parallelProduct(bigrid1)
                .parallelProduct(bigrid2)
                .parallelProduct(bigrid3)
                .getOuterBigraph();

        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(bigrid)
                .renderSites(true)
                .renderRoots(true);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(10000);
    }

    /**
     * Key Actions for GraphStream viewer:
     * Key	Modifier	Action
     * Page Up (33)	None	Zoom in
     * Page Down (34)	None	Zoom out
     * Left Arrow (37)	Shift	Rotate view counterclockwise
     * Left Arrow (37)	None	Pan view left
     * Right Arrow (39)	Shift	Rotate view clockwise
     * Right Arrow (39)	None	Pan view right
     * Up Arrow (38)	None	Pan view up
     * Down Arrow (40)	None	Pan view down
     *
     * @throws Exception
     */
    @Test
    void create_bigrid_nxm() throws Exception {

        int n = 5; // rows
        int m = 5; // columns

        World world = new World(m, n);
        boolean done = false;
        while (!done) {
            int result = world.waveFunctionCollapse();
            if (result == 0) {
                done = true;
            }
        }

        int lowestEntropy = world.getLowestEntropy();

        List<PureBigraph> localesBigraphs = new ArrayList<>();
        BiGridElementFactory factory = BiGridElementFactory.create();
        float stepSize = 1f;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
//                int tileEntropy = world.getEntropy(x, y);
                int tileType = world.getType(x, y);
//                System.out.println("(" + x + "," + y + ")@ tileEntropy: " + tileEntropy + " tileType: " + tileType);
                // Add logic to process tileEntropy and tileType if needed
                PureBigraph element = factory.createElement(tileType, x, y, stepSize);
                localesBigraphs.add(element);
            }
        }

        if (localesBigraphs.size() > 1) {
            PureBigraph bigrid = localesBigraphs.get(0);
            for (int i = 1; i < localesBigraphs.size(); i++) {
                PureBigraph currentLocale = localesBigraphs.get(i);
                bigrid = ops(bigrid).parallelProduct(currentLocale).getOuterBigraph();
            }


            int widthOfBigrid = bigrid.getRoots().size();
            Placings<DefaultDynamicSignature>.Merge merge = purePlacings(bigrid.getSignature()).merge(widthOfBigrid);
            bigrid = ops(merge).nesting(bigrid).getOuterBigraph();

            System.out.println("BFS: BigraphConnectivityChecker.isFullyConnected(bigrid) = " + BiGridConnectivityChecker.isFullyConnected(bigrid));
            System.out.println("DFS: BigraphConnectivityChecker.isFullyConnected(bigrid) = " + BiGridConnectivityCheckerDFS.isFullyConnected(bigrid));

            EcoreBigraph.Stub<DefaultDynamicSignature> cloned = new EcoreBigraph.Stub<>(bigrid).clone();
            PureBigraph copy = BigraphUtil.toBigraph(cloned.getMetaModel(), cloned.getInstanceModel(), bigrid.getSignature());
            PureBigraphDecomposerImpl decomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY);
            decomposer.decompose(copy);
            System.out.println("Count: " + decomposer.getUnionFindDataStructure().getCount());
//            System.out.println("Partitions: " + decomposer.getPartitions());
            System.out.println("Roots: " + bigrid.getRoots().size());
            System.out.println("Sites: " + bigrid.getSites().size());
            System.out.println("Outernames: " + bigrid.getOuterNames().size());
            System.out.println(bigrid.getInnerNames().size());
            System.out.println(bigrid.isPrime());
            System.out.println(bigrid.isGround());
            System.out.println(bigrid.isDiscrete());
            System.out.println(bigrid.isEpimorphic());
            System.out.println(bigrid.isMonomorphic());
            System.out.println(bigrid.isActive());
            System.out.println(bigrid.isGuarding());
            System.out.println(bigrid.isLean());



            SwingGraphStreamer graphStreamer = new SwingGraphStreamer(bigrid)
                    .renderSites(false)
                    .renderRoots(false);
            graphStreamer.prepareSystemEnvironment();
            Viewer graphViewer = graphStreamer.getGraphViewer();
            View defaultView = graphViewer.getDefaultView();
            defaultView.getCamera().setViewPercent(0.6);
            defaultView.setShortcutManager(new DefaultShortcutManager());
            while (true)
                Thread.sleep(10000);
        }


    }

}
