package org.bigraphs.model.provider.spatial.bigrid;

import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;

import java.util.*;

import static org.bigraphs.model.provider.spatial.bigrid.BiGridConnectivityChecker.ROUTE_TYPE;

public class BiGridConnectivityCheckerDFS {

    public static boolean isFullyConnected(PureBigraph bigrid) {
        List<BigraphEntity.NodeEntity<DefaultDynamicControl>> locales = getLocales(bigrid);
        if (locales.isEmpty()) {
            return true; // An empty bigrid is trivially connected.
        }

        Set<BigraphEntity.NodeEntity<DefaultDynamicControl>> visited = new HashSet<>();
//        dfsTraverse(bigrid, locales.get(0), visited); // Start DFS from any Locale node
        dfsIterative(buildAdjacencyMap(bigrid), locales.get(0), visited); // Start DFS from any Locale node

        // Find unvisited locales
        List<BigraphEntity.NodeEntity<DefaultDynamicControl>> unvisitedLocales = new ArrayList<>();
        for (BigraphEntity.NodeEntity<DefaultDynamicControl> locale : locales) {
            if (!visited.contains(locale)) {
                unvisitedLocales.add(locale);
            }
        }

        // Print and return connectivity result
        if (!unvisitedLocales.isEmpty()) {
//            System.out.println("Bigrid is NOT fully connected.");
            System.out.println("Unreachable Locale nodes (" + unvisitedLocales.size() + "):");
            for (BigraphEntity.NodeEntity<DefaultDynamicControl> locale : unvisitedLocales) {
                System.out.println("- " + locale.getName());
            }
            return false;
        }

//        System.out.println("Bigrid is fully connected.");
        return true;
    }

    /**
     * Recursively visits connected Locale nodes using DFS.
     */
    private static void dfsTraverse(PureBigraph bigrid,
                                    BigraphEntity.NodeEntity<DefaultDynamicControl> locale,
                                    Set<BigraphEntity.NodeEntity<DefaultDynamicControl>> visited) {
        if (visited.contains(locale)) {
            return;
        }
        visited.add(locale);

        for (BigraphEntity.NodeEntity<DefaultDynamicControl> neighbor : getConnectedLocales(bigrid, locale)) {
            dfsTraverse(bigrid, neighbor, visited);
        }
    }

    private static void dfsIterative(Map<BigraphEntity.NodeEntity<DefaultDynamicControl>, List<BigraphEntity.NodeEntity<DefaultDynamicControl>>> adjacencyMap,
                                     BigraphEntity.NodeEntity<DefaultDynamicControl> start,
                                     Set<BigraphEntity.NodeEntity<DefaultDynamicControl>> visited) {
        Stack<BigraphEntity.NodeEntity<DefaultDynamicControl>> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            BigraphEntity.NodeEntity<DefaultDynamicControl> node = stack.pop();
            if (!visited.contains(node)) {
                visited.add(node);
                stack.addAll(adjacencyMap.getOrDefault(node, Collections.emptyList()));
            }
        }
    }

    private static Map<BigraphEntity.NodeEntity<DefaultDynamicControl>, List<BigraphEntity.NodeEntity<DefaultDynamicControl>>> buildAdjacencyMap(PureBigraph bigrid) {
        Map<BigraphEntity.NodeEntity<DefaultDynamicControl>, List<BigraphEntity.NodeEntity<DefaultDynamicControl>>> adjacencyMap = new HashMap<>();
        List<BigraphEntity.NodeEntity<DefaultDynamicControl>> locales = getLocales(bigrid);

        for (BigraphEntity.NodeEntity<DefaultDynamicControl> locale : locales) {
            adjacencyMap.put(locale, getConnectedLocales(bigrid, locale));
        }

        return adjacencyMap;
    }

    /**
     * Get all Locale-typed nodes in the bigrid.
     */
    private static List<BigraphEntity.NodeEntity<DefaultDynamicControl>> getLocales(PureBigraph bigrid) {
        List<BigraphEntity.NodeEntity<DefaultDynamicControl>> locales = new ArrayList<>();
        for (BigraphEntity.NodeEntity<DefaultDynamicControl> node : bigrid.getNodes()) {
            if (node.getControl().getNamedType().stringValue().equals("Locale")) {
                locales.add(node);
            }
        }
        return locales;
    }

    /**
     * Get all Locale nodes connected to the given Locale node via Route nodes and outer names.
     */
    private static List<BigraphEntity.NodeEntity<DefaultDynamicControl>> getConnectedLocales(
            PureBigraph bigrid, BigraphEntity.NodeEntity<DefaultDynamicControl> locale) {

        List<BigraphEntity.NodeEntity<DefaultDynamicControl>> connectedLocales = new ArrayList<>();

        // Get all Route nodes nested inside the current Locale node
        for (BigraphEntity<?> route : bigrid.getChildrenOf(locale)) {
            if (BigraphEntityType.isNode(route) && route.getControl().getNamedType().stringValue().equals(ROUTE_TYPE)) {
                // Find the outer name this Route node links to
                for (BigraphEntity.Link outerName : bigrid.getIncidentLinksOf((BigraphEntity.NodeEntity<? extends Control<?, ?>>) route)) {
                    // Find all Locale nodes that also link to this outer name
                    for (BigraphEntity.NodeEntity<DefaultDynamicControl> otherLocale : getLocales(bigrid)) {
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
