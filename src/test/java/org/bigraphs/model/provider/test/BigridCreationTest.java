package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.bigrid.BLocationModelDataFactory;
import org.bigraphs.model.provider.spatial.bigrid.BiGridProvider;
import org.graphstream.ui.view.Viewer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BigridCreationTest implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/bigrid/";

    @BeforeMethod
    public void setUp() {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");
    }

    @Test
    void create_bigrid_nxm() throws Exception {
        int m = 3;
        int n = 3;
        BLocationModelData lmpd = BLocationModelDataFactory.createGrid(m,n, 0,0, 1, 1f);
        String json = BLocationModelDataFactory.toJson(lmpd);
        writeToFile(json, DUMP_PATH + String.format("bigrid-%dx%d.json", m, n));

        // Create bigraph grid
        BiGridProvider provider = new BiGridProvider(lmpd)
                .setRouteDirection(BiGridProvider.RouteDirection.UNIDIRECTIONAL_FORWARD);
        PureBigraph bigrid = provider.getBigraph();
        eb(bigrid, String.format("bigrid-%dx%d", m, n), DUMP_PATH);
        print(bigrid);
        printMetaModel(bigrid);
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigrid.getSignature(), System.out);
        BigraphFileModelManagement.Store.exportAsMetaModel(bigrid.getSignature(), System.out);
        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(bigrid)
                .renderSites(false)
                .renderRoots(false);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(10000);
    }
}
