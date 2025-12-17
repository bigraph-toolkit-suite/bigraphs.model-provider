package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.analysis.PureLinkGraphConnectedComponents;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.InstantiationMap;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.SubHypergraphIsoSearch;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.bigrid.*;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;
import org.bigraphs.model.provider.util.ResourceLoader;
import org.bigraphs.testing.BigraphUnitTestSupport;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.Viewer;
import org.junit.jupiter.api.Disabled;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * This test provides a comparison of the runtime performance between
 * Bigraph Matching and Subhypergraph search.
 *
 * @author Dominik Grzelak
 */
@Disabled
public class BigridQueryTest implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/bigrid/";


    @Test
    void create_bigrid_nxm() throws Exception {
        int m = 3;
        int n = 3;
        BLocationModelData lmpd = BLocationModelDataFactory.createGrid(m, n, 0, 0, 1, 1f);
        String json = BLocationModelDataFactory.toJson(lmpd);
        writeToFile(json, DUMP_PATH + String.format("bigrid-%dx%d.json", m, n));


        // Create bigraph grid
        BiGridProvider provider = new BiGridProvider(lmpd)
                .setRouteDirection(BiGridProvider.RouteDirection.BIDIRECTIONAL)
                .makeGround(true);
        PureBigraph bigrid = provider.getBigraph();

        // Try Queries
//        PureBigraph query = createQuery1(bigrid.getSignature());
//        PureBigraph query = createQuery2(bigrid.getSignature());
//        PureBigraph query = createQuery2a(bigrid.getSignature());
        PureBigraph query = createQuery3(bigrid.getSignature());
//        PureBigraph query = createQuery4(bigrid.getSignature());

        // Visualize
        Viewer dataGUI = GUI(bigrid, true, false, "/graphStreamStyleHighlight.css");
        GraphicGraph dataGraph = dataGUI.getGraphicGraph();
        dataGraph.setAttribute("ui.title", "Data ");

        Viewer queryGUI = GUI(query, true, true);
        GraphicGraph queryGraph = queryGUI.getGraphicGraph();
        queryGraph.setAttribute("ui.title", "Query");

        // Let the GUI render first
        Thread.sleep(1000);

        System.out.println("Start SubHypergraphIsoSearch");

        long start = System.nanoTime();

        // Start subhypergraph search
        SubHypergraphIsoSearch search = new SubHypergraphIsoSearch(query, bigrid);
        search.embeddings();

        long end = System.nanoTime();
        long durationNs = end - start;
        double durationMs = durationNs / 1_000_000.0;
        double durationSec = durationNs / 1_000_000_000.0;
        System.out.println(search.getCandidates());
        System.out.println("#Locales: " + bigrid.getSites().size());
        System.out.printf("Time: %.3f ms (%.6f s)%n", durationMs, durationSec);
        System.out.printf("Combinations: %d%n", search.getEmbeddingSet().size());
        System.out.println("End SubHypergraphIsoSearch");

        // Start bigraph matching
        System.out.println("Start Bigraph Matching");
        ParametricReactionRule<PureBigraph> rr11 = new ParametricReactionRule<>(query, query).withLabel("query");
        toPNG(rr11.getRedex(), "lhs", DUMP_PATH);
        toPNG(rr11.getReactum(), "rhs", DUMP_PATH);

        start = System.nanoTime();

        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> match = matcher.match(bigrid, rr11);
        Iterator<BigraphMatch<PureBigraph>> iterator = match.iterator();
        int cntCombinations = 0;
        while (iterator.hasNext()) {
            BigraphMatch<PureBigraph> next = iterator.next();
            cntCombinations++;
        }

        end = System.nanoTime();
        durationNs = end - start;
        durationMs = durationNs / 1_000_000.0;
        durationSec = durationNs / 1_000_000_000.0;
        System.out.printf("Time (Bigraph Matching): %.3f ms (%.6f s)%n", durationMs, durationSec);
        System.out.printf("Combinations (Bigraph Matching): %d%n", cntCombinations);
        System.out.println("End Bigraph Matching");

        Thread.sleep(1000);

        // Highlight Match
        for (SubHypergraphIsoSearch.Embedding next : search.getEmbeddingSet()) {
//            System.out.println("Next: " + next);
            for (Map.Entry<BigraphEntity.NodeEntity<?>, BigraphEntity.NodeEntity<?>> each : next.entrySet()) {
//                System.out.println("Key: " + each.getValue().getName());
                Node node = dataGraph.getNode(each.getValue().getName());
                if (node != null) {
                    node.setAttribute("ui.class", "highlight");

                }
            }
        }

        while (true)
            Thread.sleep(100);
    }

    /**
     * Matches every Locale with at least one Route
     */
    private PureBigraph createQuery1(DynamicSignature sig) throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        builder.root().child("Locale", "x")
                .down().child("Route", "y").site();
        return builder.create();
    }

    /**
     * Matches "Corners" of the bigrid
     */
    private PureBigraph createQuery2(DynamicSignature sig) throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        builder.root().child("Locale", "y").down().site();
        builder.root().child("Route", "y");
        builder.root().child("Route", "y");
        builder.root().child("Route", "y");
        return builder.create();
    }

    /**
     * Matches "triples"
     */
    private PureBigraph createQuery2a(DynamicSignature sig) throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        builder.root().child("Locale", "y").down().site().top()
                .child("Locale", "y2").down().child("Route", "y").site().top()
                .child("Locale", "y3").down().child("Route", "y").site().top()
        ;
        return builder.create();
    }

    private PureBigraph createQuery3(DynamicSignature sig) throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        builder.root().child("Locale", "y").down().site();
        return builder.create();
    }

    private PureBigraph createQuery4(DynamicSignature sig) throws Exception {
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        builder.root().child("Route", "y");
        builder.root().child("Route", "y");
        builder.root().child("Route", "y");
        builder.root().child("Route", "y");
        return builder.create();
    }

    public static PureBigraph loadLocationModel(String resourceFilename, String resourceFilenameSig) throws IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        // (2) Load Signature From Filesystem
        String xmiModel = new File(ResourceLoader.getResourceURL(resourceFilename + ".xmi").getFile()).getAbsolutePath();
        String xmiModelMM = new File(ResourceLoader.getResourceURL(resourceFilename + ".ecore").getFile()).getAbsolutePath();
        String xmiSig = new File(ResourceLoader.getResourceURL(resourceFilenameSig + ".xmi").getFile()).getAbsolutePath();
        String xmiSigMM = new File(ResourceLoader.getResourceURL(resourceFilenameSig + ".ecore").getFile()).getAbsolutePath();

        List<EObject> eObjectsSig = BigraphFileModelManagement.Load.signatureInstanceModel(
                xmiSigMM,
                xmiSig
        );
        DynamicSignature sigLoaded = createOrGetSignature(eObjectsSig.get(0));

        EPackage ePackage = createOrGetBigraphMetaModel(sigLoaded);
        EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
        List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(xmiModel);
        PureBigraph result = BigraphUtil.toBigraph(ePackage, eObjects.get(0), sigLoaded);

        Placings<DynamicSignature>.Merge m = purePlacings(sigLoaded).merge(result.getRoots().size());
        result = ops(m).nesting(result).getOuterBigraph();
        return result;
    }
}
