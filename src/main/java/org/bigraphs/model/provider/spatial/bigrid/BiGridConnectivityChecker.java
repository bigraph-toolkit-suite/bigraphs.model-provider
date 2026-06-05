package org.bigraphs.model.provider.spatial.bigrid;

import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.model.provider.spatial.signature.BiSpaceSignatureProvider;
import org.bigraphs.model.provider.spatial.signature.ThreeDimensionalBiSpaceSignatureProvider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to check connectivity properties of bigrid structures.
 * A bigrid is considered fully connected if all Locale nodes can reach each other
 * through Route nodes connected via outer names.
 * <p>
 * Implemented as breath-first search.
 *
 * @see BiGridConnectivityCheckerDFS
 */
public class BiGridConnectivityChecker {

    private final static Set<String> localeControlNames = Set.of(BiSpaceSignatureProvider.LOCALE_TYPE);
    private final static Set<String> routeControlNames = ThreeDimensionalBiSpaceSignatureProvider.getInstance().getSignature().getControls().stream().map(x -> x.getNamedType().stringValue()).collect(Collectors.toSet());

    /**
     * Checks if the given bigrid is fully connected.
     *
     * @param bigrid The bigrid structure to check
     * @return {@code true} if all Locale nodes are reachable from each other, {@code false} otherwise
     */
    public static boolean isFullyConnected(PureBigraph bigrid) {
        if (bigrid == null) {
            throw new IllegalArgumentException("Bigrid cannot be null");
        }

        List<BigraphEntity.NodeEntity<DynamicControl>> locales = getLocales(bigrid);
        if (locales.isEmpty()) {
            return true; // An empty bigrid is trivially connected.
        }

        // Start traversal from any Locale node
        Set<BigraphEntity.NodeEntity<DynamicControl>> visited = new HashSet<>();
        Queue<BigraphEntity.NodeEntity<DynamicControl>> queue = new LinkedList<>();
        queue.add(locales.get(0)); // Start from the first Locale node

        while (!queue.isEmpty()) {
            BigraphEntity.NodeEntity<DynamicControl> currentLocale = queue.poll();
            if (!visited.contains(currentLocale)) {
                visited.add(currentLocale);
                List<BigraphEntity.NodeEntity<DynamicControl>> connectedLocales = getConnectedLocales(bigrid, currentLocale);
                queue.addAll(connectedLocales);
            }
        }

        // Check for unvisited locales
        List<BigraphEntity.NodeEntity<DynamicControl>> unvisitedLocales = new ArrayList<>();
        for (BigraphEntity.NodeEntity<DynamicControl> locale : locales) {
            if (!visited.contains(locale)) {
                unvisitedLocales.add(locale);
            }
        }

        // If there are unvisited locales, print them and return false
        if (!unvisitedLocales.isEmpty()) {
            System.out.println("Unreachable Locale nodes (" + unvisitedLocales.size() + "):");
            for (BigraphEntity.NodeEntity<DynamicControl> locale : unvisitedLocales) {
                System.out.println("- " + locale.getName() + " (" + locale.getControl().getNamedType().stringValue() + ")");
            }
            return false;
        }

        return true;
    }

    /**
     * Get all Locale-typed nodes in the bigrid.
     */
    public static List<BigraphEntity.NodeEntity<DynamicControl>> getLocales(PureBigraph bigrid) {
        return bigrid.getNodes().stream().filter(BiGridConnectivityChecker::isLocaleEntity).collect(Collectors.toList());
//        for (BigraphEntity.NodeEntity<DynamicControl> node : bigrid.getNodes()) {
//            if (node.getControl().getNamedType().stringValue().equals(BiSpaceSignatureProvider.LOCALE_TYPE)) {
//                locales.add(node);
//            }
//        }
//        return locales;
    }

    protected static boolean isLocaleEntity(BigraphEntity<?> entity) {
        return BigraphEntityType.isNode(entity) && localeControlNames.contains(entity.getControl().getNamedType().stringValue());
    }

    protected static boolean isRouteEntity(BigraphEntity<?> entity) {
        return BigraphEntityType.isNode(entity) && routeControlNames.contains(entity.getControl().getNamedType().stringValue());

    }

    /**
     * Get all Locale nodes connected to the given Locale node via Route nodes and outer names.
     */
    public static List<BigraphEntity.NodeEntity<DynamicControl>> getConnectedLocales(
            PureBigraph bigrid, BigraphEntity.NodeEntity<DynamicControl> locale) {

        List<BigraphEntity.NodeEntity<DynamicControl>> connectedLocales = new ArrayList<>();

        // Get all Route nodes nested inside the current Locale node
        for (BigraphEntity<?> route : bigrid.getChildrenOf(locale)) {
            if (isRouteEntity(route)) {
                // Find the outer name this Route node links to
                for (BigraphEntity.Link outerName : bigrid.getIncidentLinksOf((BigraphEntity.NodeEntity<? extends Control<?, ?>>) route)) {
                    // Find all Locale nodes that also link to this outer name
                    for (BigraphEntity.NodeEntity<DynamicControl> otherLocale : getLocales(bigrid)) {
                        if (!otherLocale.equals(locale) && bigrid.getIncidentLinksOf(otherLocale).contains(outerName)) {
                            connectedLocales.add(otherLocale);
                        }
                    }
                }
            }
        }
        return connectedLocales;
    }
}
