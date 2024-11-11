package org.bigraphs.model.provider.demo.mapf.v1;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.model.provider.base.BAbstractSignatureProvider;
import org.bigraphs.model.provider.BSignatureProvider;
import org.bigraphs.model.provider.base.BComposedSignatureProvider;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.model.provider.demo.mapf.v1.sig.CFMAPF_AgentSignatureProvider;
import org.bigraphs.model.provider.demo.mapf.v1.sig.CFMAPF_LocationSignatureProvider;
import org.bigraphs.model.provider.demo.mapf.v1.sig.CFMAPF_NavigationSignatureProvider;
import org.bigraphs.model.provider.spatial.bigrid.BiGridProvider;
import org.bigraphs.model.provider.spatial.bigrid.BiGridSupport;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * Demo bigraph model provider for the Crazyflie MAPF Use Case (2D only).
 * <p>
 * A world model consists of three layers: location, navigation and agents.
 * <p>
 * To build the world model, a nxn bigrid must be supplied.
 * Then, the complete view with navigation elements and agents can be created.
 * The individual layers can be also returned: location, navigation, agents.
 *
 * @author Dominik Grzelak
 */
public class CFMAPF_WorldModelProvider extends BiGridProvider {
    private boolean externalizeCoordinateLinks = false;
    private boolean makeWorldModelGround = false;

    //TODO: store also "atoms" as LinkedList (then we can use BF-API:BigraphUtil#reorder with inst. map)
    private PureBigraph locationModel;
    private PureBigraph navigationParameter;
    private PureBigraph agentParameter;

    //TODO provide also link graph image for nav-agn-product
    private PureBigraph nav_agent_image = null;


    private LinkedList<Bigraph<DefaultDynamicSignature>> agentsAllSeparated = new LinkedList<>();

    public CFMAPF_WorldModelProvider(BLocationModelData lmpd) throws Exception {
        super(lmpd);
        super.makeGround(false);

        BAbstractSignatureProvider<DefaultDynamicSignature> coordinateSigProvider = new BAbstractSignatureProvider<>() {
            @Override
            public DefaultDynamicSignature getSignature() {
                // Add coordinate controls here: center coordinates from all the locales
                DynamicSignatureBuilder sigBuilder = pureSignatureBuilder();
                CFMAPF_WorldModelProvider.this.getLocationModelData().getLocaleNameToCoordinates().values()
                        .stream()
                        .flatMap(Collection::stream)
                        .forEach(p -> {
                            sigBuilder.addControl(BiGridSupport.formatParamControl(p), 1);
                        });
                return sigBuilder.create();
            }
        };

        BComposedSignatureProvider<DefaultDynamicSignature, PureBigraph, BSignatureProvider<DefaultDynamicSignature>>
                composedSignatureProvider =
                new BComposedSignatureProvider<>(
                        CFMAPF_AgentSignatureProvider.getInstance(),
                        coordinateSigProvider,
                        CFMAPF_NavigationSignatureProvider.getInstance(),
                        CFMAPF_LocationSignatureProvider.getInstance()
                );
        this.signatureProvider = composedSignatureProvider;

        // TODO this can be optimized, we do not have to build the whole bigrid here yet. We only need getLocaleNameToCoordinates()
        // Load location model layer to get the coordinate readings for the signature
        locationModel = super.getBigraph();
    }


    /**
     * Creates a complete world model made out of three layers.
     *
     * @return the demo world model
     * @throws Exception
     */
    @Override
    public PureBigraph getBigraph() throws Exception {

        //Determine the number of parameters for the nav model (== number of locales in the loc model)
        int numOfParams = getLocationModelData().getLocales().size();
        getLocationModelData().setNumOfNavModelSites(numOfParams);

//        // Construct maps needed for the bigraph creation later:
//        // map: locale name -> root/site index of loc model bigraph
//        BiMap<String, Integer> localeNameToRootOrSiteIndex = HashBiMap.create();
        // This map allows multiple agents merged under a locale site later
        // map: locale site index -> agents
        Map<Integer, List<Bigraph<DefaultDynamicSignature>>> locale2agents = new HashMap<>();

        // The order is given by the linkedlist so that site-root mapping is consistent
        // This indexing has nothing to do with the physical "order" in space.
        // Agents are assigned to locale sites by their coordinates.
        for (BLocationModelData.Locale each : getLocationModelData().getLocales()) {
            int sIx = mLocale2Index.get(each.getName());
            locale2agents.putIfAbsent(sIx, new ArrayList<>());
        }

        // Build the navigationParameter model
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(this.getSignature());
        // This ensures the right ordering of the parameters (i.e., the individual nav model elements under one root)
        // subject to the locales parsed before. Their root/site indices are important and must match the root indices
        // of the respective nav element components (sets of controls) of this locale
        for (int i = 0; i < numOfParams; i++) {
            final String currentLocaleName = mLocale2Index.inverse().get(i);
            // Create all coordinate nodes under the root:
            final PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy root = builder.createRoot();
            getLocationModelData().getLocaleNameToCoordinates().get(currentLocaleName).forEach(p -> {
                final String coordinateLbl = BiGridSupport.formatParamControl(p);
                try {
                    builder.createInnerName(coordinateLbl);
                    // create inner name also under the same name for easier reference
                    PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy nodeTree = root
                            .addChild("CO")
                            .linkToInner(coordinateLbl);

                    BigraphEntity.NodeEntity<Control> vCoord = nodeTree.getLastCreatedNode();
                    Map<String, Object> attributes = vCoord.getAttributes();
                    attributes.put("x", String.valueOf(p.x));
                    attributes.put("y", String.valueOf(p.y));
                    vCoord.setAttributes(attributes);

                    nodeTree.down()
                            .addChild(coordinateLbl).linkToInner(coordinateLbl)
                            .top();
                } catch (LinkTypeNotExistsException | InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
                lmpd.getTargets().forEach(tgt -> {
                    String coordLblOfTarget = BiGridSupport.formatParamControl(tgt.getCenter());
                    if (coordLblOfTarget.equals(coordinateLbl)) {
                        try {
                            root.addChild("NE").linkToInner(coordinateLbl).down()
                                    .addChild("tgt").linkToInner(coordinateLbl).linkToOuter(lmpd.getTargetPrefixLabel(tgt.getAgentRef())).top();
                        } catch (InvalidConnectionException | TypeNotExistsException e) {
                            throw new RuntimeException(e);
                        }
                    }

                });
            });


            // finally, add a site under the root (which represents a "parameter set" (agents) of nav model elements)
            root.addChild("Ensemble").down().addSite()
                    .top();
        }
        // Finalize the bigraph
        navigationParameter = builder.createBigraph();

        LinkedList<BLocationModelData.Agent> agents = getLocationModelData().getAgents();
        for (BLocationModelData.Agent each : agents) {
            boolean uavUsesExistingCoordinate = false;
            Point2D.Float coordinate = each.getCenter();
            String agentPositionRef = BiGridSupport.formatParamControl(coordinate);
            String agentTargetRef = lmpd.getTargetPrefixLabel(each);

            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(getSignature());
            PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy uav = b.createRoot()
                    .addChild("Agent", agentPositionRef).linkToOuter(agentTargetRef)
                    .down().addChild("ID")
                    .down().addChild(each.getName())
                    .up().addChild("Status")
                    .top();
            if (!makeWorldModelGround) {
                uav.addSite();
            }

            b.closeAllInnerNames();
            PureBigraph agentBigraph = b.createBigraph();
//            agentsAllSeparated.add(agentBigraph);

            // create map: locale name -> agents by coordinate matching
            // check under which site the agent will be located later
            // go through the locale's coordinate set. It is enough if the agent is close to any of them
            boolean canFindSiteMappingForUAV = false;
            for (Map.Entry<String, List<Point2D.Float>> eachEntry : getLocationModelData().getLocaleNameToCoordinates().entrySet()) {
                for (Point2D.Float point : eachEntry.getValue()) {
                    if (BiGridSupport.agentIsCloseAtPoint(each, point)) {
                        int siteIndexMapping = mLocale2Index.get(eachEntry.getKey());
                        // Same put to heavy
//                        int siteIndexMapping0 = BiGridSupport.findLocaleSiteIndexByCoordinate(
//                                BiGridSupport.formatParamControl(point),
//                                intermediate);
                        locale2agents.get(siteIndexMapping).add(agentBigraph);
                        canFindSiteMappingForUAV = true;
                        break;
                    }
                }
                if (canFindSiteMappingForUAV) break;
            }
            if (!canFindSiteMappingForUAV) {
                assert !uavUsesExistingCoordinate;
                throw new RuntimeException("Please check whether the UAV has valid starting position coordinates set. They do not exist in the location/navigation model");
            }
        }

        // fill up the rest with an empty root (i.e., a barren)
        long uavCount = locale2agents.entrySet().stream().filter(x -> x.getValue().size() > 0).count();
        int diff = (int) (getLocationModelData().getNumOfNavModelSites() - uavCount);
        if (diff != 0) {
            locale2agents.forEach((k, v) -> {
                if (v.isEmpty()) {
                    if (makeWorldModelGround) {
                        locale2agents.get(k).add(purePlacings(getSignature()).barren());
                    } else {
                        locale2agents.get(k).add(purePlacings(getSignature()).identity1());
                    }
                }
            });
        }
        Linkings<DefaultDynamicSignature> linkings = pureLinkings(getSignature());
        List<Bigraph<DefaultDynamicSignature>> collectedAgents = locale2agents.entrySet().stream()
                .map(x -> x.getValue()
                        .stream().map(y -> (Bigraph<DefaultDynamicSignature>) y)
                        .reduce(linkings.identity_e(), accumulator_mergeProduct))
                .toList();

        agentParameter = (PureBigraph) collectedAgents.stream()
                .reduce(linkings.identity_e(), accumulator_parallelProduct);

        List<String> leftOuterLabels = navigationParameter.getInnerNames().stream().map(BigraphEntity.InnerName::getName).collect(Collectors.toList());
        List<String> rightOuterLabels = agentParameter.getOuterNames()
                .stream().map(BigraphEntity.Link::getName)
                .collect(Collectors.toList());
        leftOuterLabels.removeAll(rightOuterLabels);

        for (BLocationModelData.Agent each : agents) {
            String agentTargetRef = lmpd.getTargetPrefixLabel(each);
            navigationParameter = ops(navigationParameter).parallelProduct(pureLinkings(getSignature()).identity(agentTargetRef)).getOuterBigraph();
            leftOuterLabels.add(agentTargetRef);
        }

        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(getSignature());
        leftOuterLabels.forEach(b::createOuterName);
        Bigraph<DefaultDynamicSignature> intermediate = ops(agentParameter).parallelProduct(b.createBigraph()).getOuterBigraph();
        Bigraph<DefaultDynamicSignature> navigationAgentProduct;
        if (externalizeCoordinateLinks) {
            navigationAgentProduct = ops(navigationParameter).nesting(intermediate).getOuterBigraph();
        } else {
            navigationAgentProduct = ops(navigationParameter).compose(intermediate).getOuterBigraph();
        }
        PureBigraph completeModelView = ops(locationModel).nesting(navigationAgentProduct).getOuterBigraph();
        return completeModelView;
    }

    public PureBigraph getLocationModel() {
        return locationModel;
    }

    public PureBigraph getNavigationParameter() {
        return navigationParameter;
    }

    public PureBigraph getAgentParameter() {
        return agentParameter;
    }

    public CFMAPF_WorldModelProvider makeGround(boolean makeGround) {
        this.makeWorldModelGround = makeGround;
        return this;
    }

    private static BinaryOperator<Bigraph<DefaultDynamicSignature>> accumulator_mergeProduct = new BinaryOperator<Bigraph<DefaultDynamicSignature>>() {
        @Override
        public Bigraph<DefaultDynamicSignature> apply(Bigraph<DefaultDynamicSignature> partial, Bigraph<DefaultDynamicSignature> element) {
            try {
                return ops(partial).merge(element).getOuterBigraph();
            } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                return pureLinkings(partial.getSignature()).identity_e();
            }
        }
    };

    private static BinaryOperator<Bigraph<DefaultDynamicSignature>> accumulator_parallelProduct = (partial, element) -> {
        try {
            return ops(partial).parallelProduct(element).getOuterBigraph();
        } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
            return pureLinkings(partial.getSignature()).identity_e();
        }
    };
}
