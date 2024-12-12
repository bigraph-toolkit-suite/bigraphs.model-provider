package org.bigraphs.model.provider.test;

import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ParametricReactionRule;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.simulation.matching.MatchIterable;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphParametricMatch;
import org.bigraphs.framework.simulation.matching.pure.PureReactiveSystem;
import org.bigraphs.framework.visualization.SwingGraphStreamer;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.spatial.bigrid.BiGridSupport;
import org.bigraphs.model.provider.spatial.bigrid.BiGridProvider;
import org.bigraphs.model.provider.demo.mapf.v1.rule.CFMAPF_ReactionRuleProvider;
import org.bigraphs.model.provider.demo.mapf.v1.CFMAPF_WorldModelProvider;
import org.bigraphs.framework.core.analysis.*;
import org.graphstream.ui.view.Viewer;
import org.junit.jupiter.api.Disabled;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.bigraphs.model.provider.spatial.bigrid.BiGridSupport.formatCoordinate;
import static org.bigraphs.model.provider.spatial.bigrid.BiGridSupport.parseParamControl;

public class DemoBigridModelProviderTest implements BigraphUnitTestSupport {
    static final String DUMP_PATH = "src/test/resources/dump/bigrid/";

    @BeforeMethod
    public void setUp() {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");
    }

    @Disabled
    @Test
    public void create_bare_bigrid() throws Exception {
        BLocationModelData lmpd = create_3_3_bigrid();
        // Create bigraph grid
        BiGridProvider provider = new BiGridProvider(lmpd)
                .setRouteDirection(BiGridProvider.RouteDirection.BIDIRECTIONAL);
        PureBigraph bigrid = provider.getBigraph();
        eb(bigrid, "bigrid-3x3", DUMP_PATH);

        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(bigrid)
                .renderSites(true)
                .renderRoots(true);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(10000);
    }

    @Test
    public void test_two_locale_bigrid() throws Exception {
        BLocationModelData lmpd = create_test_home_bigrid();
        String agentID = "N_1";
        lmpd = bigrid_add_agent_at(lmpd, agentID, new Point2D.Float(0.5f, 0.0f), new float[]{0.15f, 0.15f, 0.15f});
        // Create bigraph grid
        CFMAPF_WorldModelProvider provider = new CFMAPF_WorldModelProvider(lmpd);
        PureBigraph bigrid = provider.getBigraph();
        eb(bigrid, "bigrid-1-2", DUMP_PATH);

        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(bigrid)
                .renderSites(true)
                .renderRoots(true);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(10000);
    }


    @Test
    public void test_format_coordinates() {
        float[] testValues = {-1.34f, 0.34f, 5.43f, -0.12f, 2.01f};
        for (float value : testValues) {
            String c1 = formatCoordinate(value);
            System.out.println(c1);
            String ctrlLbl = "C_" + c1 + "__" + c1;
            Point2D.Float aFloat = parseParamControl(ctrlLbl);
            System.out.println(aFloat);
        }
    }

    @Test
    @Disabled
    public void test_bigrid_one_agent() throws Exception {
        BLocationModelData lmpd = create_3_3_bigrid();
        lmpd = bigrid_add_agent_C00(lmpd);
        lmpd = bigrid_add_target_C28(lmpd);
        boolean makeGround = false;
        CFMAPF_WorldModelProvider provider = new CFMAPF_WorldModelProvider(lmpd) // a refinement of a bare bigrid with coordinates
                .setRouteDirection(BiGridProvider.RouteDirection.BIDIRECTIONAL)
                .makeGround(makeGround);
        PureBigraph bigrid = provider.getBigraph();
        eb(bigrid, "bigrid-3x3-coords", DUMP_PATH);

        int c_xy = BiGridSupport.Search.findLocaleSiteIndexByCoordinate("C_0_00__0_00", bigrid);
        System.out.println(c_xy);
        if (makeGround) assert c_xy == -1;
        else assert c_xy == 0;
        int c_xy2 = BiGridSupport.Search.findLocaleSiteIndexByCoordinate("C_2_00__7_00", bigrid);
        System.out.println(c_xy2);
        if (makeGround) assert c_xy2 == -1;
        else assert c_xy2 == 7;

        Set<BigraphEntity.NodeEntity<DefaultDynamicControl>> coordNodes = bigrid.getNodes().stream()
                .filter(x -> x.getControl().getNamedType().stringValue().equals("CO")).collect(Collectors.toSet());
        coordNodes.forEach(node -> {
            System.out.println(node.getAttributes());
        });

        PureBigraph locationModel = provider.getLocationModel();
        eb(locationModel, "bigrid-3x3-loc", DUMP_PATH);
        PureBigraph navigationParameter = provider.getNavigationParameter();
        eb(navigationParameter, "bigrid-3x3-nav", DUMP_PATH);
        PureBigraph agentParameter = provider.getAgentParameter();
        eb(agentParameter, "bigrid-3x3-agn", DUMP_PATH);


        SwingGraphStreamer graphStreamer = new SwingGraphStreamer(bigrid)
                .renderSites(true)
                .renderRoots(false);
        graphStreamer.prepareSystemEnvironment();
        Viewer graphViewer = graphStreamer.getGraphViewer();
        while (true)
            Thread.sleep(1000);
    }

    @Test
    @Disabled
    public void simulate_one_agent() throws Exception {
        BLocationModelData lmpd = create_3_3_bigrid();
        lmpd = bigrid_add_agent_C00(lmpd);
        lmpd = bigrid_add_target_C28(lmpd);
        boolean makeGround = true;
        CFMAPF_WorldModelProvider provider = new CFMAPF_WorldModelProvider(lmpd)
                .setRouteDirection(BiGridProvider.RouteDirection.BIDIRECTIONAL)
                .makeGround(makeGround);
        PureBigraph bigrid = provider.getBigraph();
        eb(bigrid, "bigrid-3x3-one-agent", DUMP_PATH);

        String rrLbl = "rr0";
        CFMAPF_ReactionRuleProvider rrProvider = new CFMAPF_ReactionRuleProvider(lmpd, rrLbl, bigrid.getSignature());
        ParametricReactionRule<PureBigraph> rr = rrProvider.getRule();
        eb(rr.getRedex(), rrLbl + "_LHS", DUMP_PATH);
        eb(rr.getReactum(), rrLbl + "_RHS", DUMP_PATH);

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(bigrid);
        reactiveSystem.addReactionRule(rr);
        AbstractBigraphMatcher<PureBigraph> matcher = AbstractBigraphMatcher.create(PureBigraph.class);
        MatchIterable<BigraphMatch<PureBigraph>> matches = matcher.match(bigrid, rr);
        Iterator<BigraphMatch<PureBigraph>> iterator = matches.iterator();
        System.out.println(iterator.hasNext());
        int cnt = 0;
        while (iterator.hasNext()) {
            PureBigraphParametricMatch match = (PureBigraphParametricMatch) iterator.next();
            System.out.println(match);
            PureBigraph newState = reactiveSystem.buildParametricReaction(reactiveSystem.getAgent(), match, rr);
            eb(newState, "newState-" + cnt, DUMP_PATH);
            cnt++;
        }
    }

    @Test
    @Disabled
    public void test_decomposition_bigrid() throws Exception {
        BLocationModelData lmpd = create_3_3_bigrid();
//        lmpd = bigrid_add_agent_C00(lmpd);
//        lmpd = bigrid_add_target_C28(lmpd);
        boolean makeGround = true;
        CFMAPF_WorldModelProvider provider = new CFMAPF_WorldModelProvider(lmpd)
                .setRouteDirection(BiGridProvider.RouteDirection.BIDIRECTIONAL)
                .makeGround(makeGround);
        PureBigraph bigrid = provider.getBigraph();
        eb(bigrid, "bigrid-3x3", DUMP_PATH + "decomp/");


        PureBigraphDecomposerImpl decomposer = BigraphDecomposer.create(BigraphDecomposer.DEFAULT_DECOMPOSITION_STRATEGY);
        decomposer.decompose(bigrid);
        System.out.println("Count: " + decomposer.getUnionFindDataStructure().getCount());
        System.out.println("Partitions: " + decomposer.getPartitions());

        List<PureBigraph> components = decomposer.getConnectedComponents();
        AtomicInteger cnt = new AtomicInteger(0);
        components.forEach(c -> {
            try {
                // eb(c, "component_" + cnt.getAndIncrement(), DUMP_PATH + "decomp/");
                // may throw an exception because of getParent() != null in BF-API core
                BigraphFileModelManagement.Store.exportAsInstanceModel(
                        c, new FileOutputStream(DUMP_PATH + "decomp/" + "component_" + cnt.getAndIncrement() + ".xmi")
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println(components);

    }

    public static BLocationModelData bigrid_add_agent_C00(BLocationModelData lmpd) {
        BLocationModelData.Agent a1 = BLocationModelData.Agent.builder()
                .name("N_1") // cf: CFMAPF_AgentSignatureProvider
                .center(new Point2D.Float(0, 0))
                .depth(0.15f).width(0.15f).height(0.15f) // meter // BoundingBox
                .build();

        lmpd.getAgents().add(a1);

        return lmpd;
    }

    public static BLocationModelData bigrid_add_target_C28(BLocationModelData lmpd) {
        BLocationModelData.Agent a1 = lmpd.getAgents().stream().filter(a -> a.getName().equals("N_1")).findFirst().get();
        BLocationModelData.Target t1 = BLocationModelData.Target.builder()
                .name(lmpd.getTargetPrefixLabel(a1))
                .agentRef(a1)
                .center(new Point2D.Float(2, 8))
                .build();

        lmpd.getTargets().add(t1);

        return lmpd;
    }

    public static BLocationModelData bigrid_add_two_agents(BLocationModelData lmpd) {
        bigrid_add_agent_C00(lmpd);

        BLocationModelData.Agent a2 = BLocationModelData.Agent.builder()
                .name("N_2") // cf: CFMAPF_AgentSignatureProvider
                .center(new Point2D.Float(2, 8))
                .depth(0.15f).width(0.15f).height(0.15f) // meter // BoundingBox
                .build();

        lmpd.getAgents().add(a2);

        return lmpd;
    }

    public static BLocationModelData create_3_3_bigrid() {
        // 3x3 grid horizontally and vertically connected
        BLocationModelData lmpd = new BLocationModelData();
        // First row
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v0").center(new Point2D.Float(0, 0)).build());
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v1").center(new Point2D.Float(0, 1)).build());
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v2").center(new Point2D.Float(0, 2)).build());
        // Second row
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v3").center(new Point2D.Float(1, 3)).build());
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v4").center(new Point2D.Float(1, 4)).build());
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v5").center(new Point2D.Float(1, 5)).build());
        // Third row
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v6").center(new Point2D.Float(2, 6)).build());
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v7").center(new Point2D.Float(2, 7)).build());
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v8").center(new Point2D.Float(2, 8)).build());

        // Because of bidirectionality we only need to define one direction from locale to locale
        // Horizontal ones first
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l1").startingPoint(new Point2D.Float(0, 0)).endingPoint(new Point2D.Float(0, 1)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l2").startingPoint(new Point2D.Float(0, 1)).endingPoint(new Point2D.Float(0, 2)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l3").startingPoint(new Point2D.Float(1, 3)).endingPoint(new Point2D.Float(1, 4)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l4").startingPoint(new Point2D.Float(1, 4)).endingPoint(new Point2D.Float(1, 5)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l5").startingPoint(new Point2D.Float(2, 6)).endingPoint(new Point2D.Float(2, 7)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l6").startingPoint(new Point2D.Float(2, 7)).endingPoint(new Point2D.Float(2, 8)).build());
        // Vertical ones next, first to second row,
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l7").startingPoint(new Point2D.Float(0, 0)).endingPoint(new Point2D.Float(1, 3)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l8").startingPoint(new Point2D.Float(0, 1)).endingPoint(new Point2D.Float(1, 4)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l9").startingPoint(new Point2D.Float(0, 2)).endingPoint(new Point2D.Float(1, 5)).build());
        // second row to third row
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l10").startingPoint(new Point2D.Float(1, 3)).endingPoint(new Point2D.Float(2, 6)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l11").startingPoint(new Point2D.Float(1, 4)).endingPoint(new Point2D.Float(2, 7)).build());
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l12").startingPoint(new Point2D.Float(1, 5)).endingPoint(new Point2D.Float(2, 8)).build());

        return lmpd;
    }

    /**
     * 2 Locales connected: (0.5, 0.0) <-> (0.72, 1.02)
     */
    public static BLocationModelData create_test_home_bigrid() {
        // 3x3 grid horizontally and vertically connected
        BLocationModelData lmpd = new BLocationModelData();

        Point2D.Float l1 = new Point2D.Float(0.50f, 0.0f);
        Point2D.Float l2 = new Point2D.Float(0.72f, 1.02f);
        // First row
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v0").center(l1).build());
        lmpd.getLocales().add(BLocationModelData.Locale.builder().name("v1").center(l2).build());

        // Because of bidirectionality we only need to define one direction from locale to locale
        // Horizontal ones first
        lmpd.getRoutes().add(BLocationModelData.Route.builder().name("l1").startingPoint(l1).endingPoint(l2).build());

        return lmpd;
    }

    public static BLocationModelData bigrid_add_agent_at(BLocationModelData lmpd, String agentID, Point2D.Float point, float[] boundingBox) {
        BLocationModelData.Agent a1 = BLocationModelData.Agent.builder()
                .name(agentID) // cf: CFMAPF_AgentSignatureProvider
                .center(point)
                .depth(boundingBox[0]).width(boundingBox[1]).height(boundingBox[2]) // meter // BoundingBox
                .build();

        lmpd.getAgents().add(a1);

        return lmpd;
    }
}
