package org.bigraphs.model.provider.spatial.bigrid;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bigraphs.model.provider.base.BAbstractBigraphProvider;
import org.bigraphs.model.provider.base.BLocationModelData;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.model.provider.spatial.signature.BigridSignatureProvider;

import java.util.*;
import java.util.function.Supplier;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;


/**
 * This is a concrete bigraph model (agent model) provider.
 * It provides a bigrid - bigraphical 2D grid.
 *
 * @author Dominik Grzelak
 */
public class BiGridProvider extends BAbstractBigraphProvider<DefaultDynamicSignature, PureBigraph> {
    public enum RouteDirection {
        BIDIRECTIONAL, // good when we do not want to describe all edges, e.g., for fully connected grids
        UNIDIRECTIONAL_FORWARD, // good default, the specified edges are translated as is, no other edges are created than specified
        UNIDIRECTIONAL_BACKWARD // does the opposite of UNIDIRECTIONAL_FORWARD
    }

    protected BLocationModelData lmpd;
    protected RouteDirection routeDirection = RouteDirection.BIDIRECTIONAL;
    private boolean makeWorldModelGround = false;
    // Construct maps needed for the bigraph creation later:
    // map: locale name -> root/site index of loc model bigraph
    protected BiMap<String, Integer> mLocale2Index = HashBiMap.create();

    public BiGridProvider(BLocationModelData lmpd) {
        super(BigridSignatureProvider.getInstance());
        this.lmpd = lmpd;
    }

    @Override
    public DefaultDynamicSignature getSignature() {
        return signatureProvider.getSignature();
    }

    public BLocationModelData getLocationModelData() {
        return lmpd;
    }

    public void setLocationModelData(BLocationModelData lmpd) {
        this.lmpd = lmpd;
    }

    @Override
    public PureBigraph getBigraph() throws Exception {
        // TODO: This block has to be done somewhere else
        // Problem is that LMPD is input/output hybrid!
        lmpd.getLocaleNameToCoordinates().clear(); // important when method called twice
        lmpd.getBNodeIdToExternalLocaleName().clear(); // important when method called twice
        lmpd.getLocaleNameToRootOrSiteIndex().clear();
        for (BLocationModelData.Locale each : lmpd.getLocales()) {
            // Update the coordinate set of each locale: Fill default coordinates
            // Center of each locale is default coordinate
            // Will be used by CFMAPFWorldModelProvider
            getLocationModelData().getLocaleNameToCoordinates().putIfAbsent(each.getName(), new ArrayList<>());
            getLocationModelData().getLocaleNameToCoordinates().get(each.getName()).add(each.getCenter());
        }

        Supplier<String> nameSupplier = createNameSupplier("y");
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(getSignature());
        Map<String, PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy> localeNameToBigraphMap = new LinkedHashMap<>();
        Map<String, String> localeNameToOuternameMap = new LinkedHashMap<>();


        // Make a copy
        LinkedList<BLocationModelData.Locale> locales = new LinkedList<>(lmpd.getLocales());
        LinkedList<BLocationModelData.Route> routes = new LinkedList<>(lmpd.getRoutes());
        lmpd.setNumOfLocModelSites(locales.size()); // update object with new information

        Collections.sort(locales);
        Collections.sort(routes);

//        System.out.format("Total Locales found: %d\n", locales.size());
//        System.out.format("Total Roads found: %d\n", routes.size());

        //TODO assume specific/random order?
        // Currently we build the grid from top to bottom, from left to right
        int sIx = 0;
        for (BLocationModelData.Locale each : locales) {
            assert each.getName() != null;
            if (makeWorldModelGround) {
                localeNameToBigraphMap.put(each.getName(), builder.hierarchy("Locale").top());
            } else {
                localeNameToBigraphMap.put(each.getName(), builder.hierarchy("Locale").addSite().top());
            }
            String nodeId = localeNameToBigraphMap.get(each.getName()).getLastCreatedNode().getName();
            assert nodeId.equalsIgnoreCase(each.getName());
            lmpd.getBNodeIdToExternalLocaleName().put(nodeId, each.getName());
            localeNameToOuternameMap.put(nodeId, nameSupplier.get());
            localeNameToBigraphMap.get(nodeId).top().linkToOuter(localeNameToOuternameMap.get(nodeId));

            // Store original order of locales processed here
            // In this state the locale site index == root index
            mLocale2Index.put(each.getName(), sIx);
            sIx++;

//            // Update the coordinate set of each locale: Fill default coordinates
//            // Center of each locale is default coordinate
//            // Will be used by CFMAPFWorldModelProvider
//            getLocationModelData().getLocaleNameToCoordinates().putIfAbsent(each.getName(), new ArrayList<>());
//            getLocationModelData().getLocaleNameToCoordinates().get(each.getName()).add(each.getCenter());
        }
        // store original mapping for later use
        // Note that the site indexes are not necessarily present if agents occupy the space.
        // In that case the meaning gets lost. But the index still refers to the original root index
        // of the locale model layer.
        lmpd.setLocaleNameToRootOrSiteIndex(mLocale2Index);

        // Iterate through all Roads and connect two Locales
        // For every road, check whether the starting/ending point touches a Locale -> get both locales and connect them
        // 3 Modes are considered: bidirectional / unidirectional forward & backward
        for (BLocationModelData.Route each : routes) {
            if (LOG_DEBUG) {
                System.out.println("Road: " + each.getName());
            }
            BLocationModelData.Locale connectedLocaleStart = BiGridSupport.getConnectedLocale(each.getStartingPoint(), locales);
            BLocationModelData.Locale connectedLocaleEnd = BiGridSupport.getConnectedLocale(each.getEndingPoint(), locales);
            if (connectedLocaleStart != null && connectedLocaleEnd != null) {

                String y = localeNameToOuternameMap.get(connectedLocaleStart.getName());
                String y2 = localeNameToOuternameMap.get(connectedLocaleEnd.getName());

                if (routeDirection == RouteDirection.BIDIRECTIONAL) { // from start to end and vice versa
                    localeNameToBigraphMap.get(connectedLocaleStart.getName()).addChild("Route").linkToOuter(y2).top();
                    localeNameToBigraphMap.get(connectedLocaleEnd.getName()).addChild("Route").linkToOuter(y).top();
                }
                if (routeDirection == RouteDirection.UNIDIRECTIONAL_FORWARD) { // from start to end only
                    BiGridSupport.connectToOuterName(connectedLocaleStart.getName(), localeNameToBigraphMap, y, localeNameToOuternameMap).addChild("Route").linkToOuter(y2).top();
                    BiGridSupport.connectToOuterName(connectedLocaleEnd.getName(), localeNameToBigraphMap, y2, localeNameToOuternameMap);
                }
                if (routeDirection == RouteDirection.UNIDIRECTIONAL_BACKWARD) { // from start to end only
                    BiGridSupport.connectToOuterName(connectedLocaleStart.getName(), localeNameToBigraphMap, y, localeNameToOuternameMap);
                    BiGridSupport.connectToOuterName(connectedLocaleEnd.getName(), localeNameToBigraphMap, y2, localeNameToOuternameMap).addChild("Route").linkToOuter(y).top();
                }
            }
        }

        // Finally, merge all locales under one root
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy root = builder.createRoot();
        for (PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy each : localeNameToBigraphMap.values()) {
            root.addChild(each);
        }

        // Finalize the bigraph and return a bigraph object
        return builder.createBigraph();
    }

    public <T extends BiGridProvider> T makeGround(boolean makeGround) {
        this.makeWorldModelGround = makeGround;
        return (T) this;
    }

    public <T extends BiGridProvider> T setRouteDirection(RouteDirection routeDirection) {
        this.routeDirection = routeDirection;
        return (T) this;
    }
}
