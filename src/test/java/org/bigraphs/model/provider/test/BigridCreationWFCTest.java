package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.spatial.bigrid.BiGridElementFactory;
import org.bigraphs.model.provider.spatial.bigrid.World;
import org.graphstream.ui.swing_viewer.util.DefaultShortcutManager;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;

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

        PureBigraph bigrid0 = factory.crossingFour(0, 0);
        PureBigraph bigrid1 = factory.crossingFour(0, 1);
        PureBigraph bigrid2 = factory.crossingFour(0, 2);
        PureBigraph bigrid3 = factory.crossingFour(0, 3);
//
        PureBigraph bigrid = bigrid0;
//                ops(bigrid0).parallelProduct(bigrid1)
//                .parallelProduct(bigrid2)
//                .parallelProduct(bigrid3)
//                .getOuterBigraph();

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

        int n = 4; // rows
        int m = 4; // columns

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
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                int tileEntropy = world.getEntropy(x, y);
                int tileType = world.getType(x, y);
                System.out.println("(" + x + "," + y + ")@ tileEntropy: " + tileEntropy + " tileType: " + tileType);
                // Add logic to process tileEntropy and tileType if needed
                PureBigraph element = factory.createElement(tileType, x, y);
                localesBigraphs.add(element);
            }
        }

        if (localesBigraphs.size() > 1) {
            PureBigraph bigrid = localesBigraphs.get(0);
            for (int i = 1; i < localesBigraphs.size(); i++) {
                PureBigraph currentLocale = localesBigraphs.get(i);
                bigrid = ops(bigrid).parallelProduct(currentLocale).getOuterBigraph();
            }

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
