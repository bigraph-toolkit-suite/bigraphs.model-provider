package org.bigraphs.model.provider.spatial.bigrid;

import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicControl;

import java.util.*;

import static org.bigraphs.model.provider.spatial.bigrid.BiGridConnectivityChecker.getConnectedLocales;
import static org.bigraphs.model.provider.spatial.bigrid.BiGridConnectivityChecker.getLocales;

/**
 * Utility class to check connectivity properties of bigrid structures.
 * A bigrid is considered fully connected if all Locale nodes can reach each other
 * through Route nodes connected via outer names.
 * <p>
 * Implemented as depth-first search.
 *
 * @see BiGridConnectivityChecker
 */
public class BiGridConnectivityCheckerDFS {

    public static boolean isFullyConnected(PureBigraph bigrid) {
        List<BigraphEntity.NodeEntity<DynamicControl>> locales = getLocales(bigrid);
        if (locales.isEmpty()) {
            return true; // An empty bigrid is trivially connected.
        }

        Set<BigraphEntity.NodeEntity<DynamicControl>> visited = new HashSet<>();
//        dfsTraverse(bigrid, locales.get(0), visited); // Start DFS from any Locale node
        dfsIterative(buildAdjacencyMap(bigrid), locales.get(0), visited); // Start DFS from any Locale node

        // Find unvisited locales
        List<BigraphEntity.NodeEntity<DynamicControl>> unvisitedLocales = new ArrayList<>();
        for (BigraphEntity.NodeEntity<DynamicControl> locale : locales) {
            if (!visited.contains(locale)) {
                unvisitedLocales.add(locale);
            }
        }

        // Print and return connectivity result
        if (!unvisitedLocales.isEmpty()) {
//            System.out.println("Bigrid is NOT fully connected.");
            System.out.println("Unreachable Locale nodes (" + unvisitedLocales.size() + "):");
            for (BigraphEntity.NodeEntity<DynamicControl> locale : unvisitedLocales) {
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
                                    BigraphEntity.NodeEntity<DynamicControl> locale,
                                    Set<BigraphEntity.NodeEntity<DynamicControl>> visited) {
        if (visited.contains(locale)) {
            return;
        }
        visited.add(locale);

        for (BigraphEntity.NodeEntity<DynamicControl> neighbor : getConnectedLocales(bigrid, locale)) {
            dfsTraverse(bigrid, neighbor, visited);
        }
    }

    private static void dfsIterative(Map<BigraphEntity.NodeEntity<DynamicControl>, List<BigraphEntity.NodeEntity<DynamicControl>>> adjacencyMap,
                                     BigraphEntity.NodeEntity<DynamicControl> start,
                                     Set<BigraphEntity.NodeEntity<DynamicControl>> visited) {
        Stack<BigraphEntity.NodeEntity<DynamicControl>> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            BigraphEntity.NodeEntity<DynamicControl> node = stack.pop();
            if (!visited.contains(node)) {
                visited.add(node);
                stack.addAll(adjacencyMap.getOrDefault(node, Collections.emptyList()));
            }
        }
    }

    private static Map<BigraphEntity.NodeEntity<DynamicControl>, List<BigraphEntity.NodeEntity<DynamicControl>>> buildAdjacencyMap(PureBigraph bigrid) {
        Map<BigraphEntity.NodeEntity<DynamicControl>, List<BigraphEntity.NodeEntity<DynamicControl>>> adjacencyMap = new HashMap<>();
        List<BigraphEntity.NodeEntity<DynamicControl>> locales = getLocales(bigrid);

        for (BigraphEntity.NodeEntity<DynamicControl> locale : locales) {
            adjacencyMap.put(locale, getConnectedLocales(bigrid, locale));
        }

        return adjacencyMap;
    }
}
