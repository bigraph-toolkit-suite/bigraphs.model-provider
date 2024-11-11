package org.bigraphs.model.provider.base;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Builder;
import lombok.Data;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.model.provider.BBigraphProvider;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Generic data object containing relevant concepts to model locations/areas/realms/... and routes/roads/... connecting them.
 * <p>
 * This is the data format every model parser and model provider (i.e., each implementation of {@link BBigraphProvider}) is
 * able to understand, produce and/or update this format.
 * It is immutable, thus, can be subsequently enriched by all model providers.
 *
 * @author Dominik Grzelak
 */
@Data
public class BLocationModelData {

    // Insertion order is preserved
    LinkedList<Locale> locales = new LinkedList<>();
    LinkedList<Route> routes = new LinkedList<>();
    LinkedList<NavElement> navElements = new LinkedList<>();
    LinkedList<Agent> agents = new LinkedList<>();
    LinkedList<Target> targets = new LinkedList<>();

    int numOfNavModelSites = 0;
    int numOfLocModelSites = 0;

    //TODO: THIS IS OUTPUT STUFF: make monad, add a clone method
    Map<String, List<Point2D.Float>> localeNameToCoordinates = new HashMap<>();
    Map<String, String> bNodeIdToExternalLocaleName = new HashMap<>();
    BiMap<String, Integer> localeNameToRootOrSiteIndex = HashBiMap.create();
    // F, F', F'' ... sets of coordinates
    // map: nodeID -> external locale label
    // external local name <-> site/root index original
    //TODO: THIS IS OUTPUT STUFF

    public Locale getLocaleByName(String name) {
        if (name == null) return null;
        for (Locale each : getLocales()) {
            if (each.getName().equalsIgnoreCase(name)) {
                return each;
            }
        }
        return null;
    }

    public String getTargetPrefixLabel(Agent agent) {
        return "target_" + agent.getName();
    }

    @Data
    @Builder
    public static class Route implements Comparable<Route> {
        public String name;
        public Point2D.Float startingPoint;
        public Point2D.Float endingPoint;

        @Override
        public int compareTo(Route otherRoute) {
            int thisIndex = extractIndex(this.name);
            int otherIndex = extractIndex(otherRoute.name);
            return Integer.compare(thisIndex, otherIndex);
        }

        // We assume the format is always like "l<NUMBER>"
        private int extractIndex(String name) {
            return Integer.parseInt(name.substring(1));
        }
    }

    @Data
    @Builder
    public static class Target implements Comparable<Target> {
        public String name;
        public Point2D.Float center;
        public Agent agentRef;

        @Override
        public int compareTo(Target otherTarget) {
            return this.name.compareTo(otherTarget.name);
        }
    }

    @Data
    @Builder
    public static class Locale implements Comparable<Locale> {
        public String name;

        // origin of the area
        public Point2D.Float center;

        // width of the area
        public float width; // can also be zero

        // depth of the area
        public float depth; // can also be zero

        // Compares only the numeric parts of the name identifier of a locale
        @Override
        public int compareTo(Locale otherLocale) {
            int thisIndex = extractIndex(this.name);
            int otherIndex = extractIndex(otherLocale.name);
            return Integer.compare(thisIndex, otherIndex);
        }

        // We assume the format is always like "v<NUMBER>"
        private int extractIndex(String name) {
            return Integer.parseInt(name.substring(1));
        }
    }

    @Data
    public static class NavElement {
        String name;
        String type;
        Point2D.Float center;
        float width;
        float depth;

        DefaultDynamicControl control;
    }

    @Data
    @Builder
    public static class Agent implements Comparable<Agent> {
        public String name;
        public Point2D.Float center;
        float width;
        float depth;
        float height;

        @Override
        public int compareTo(Agent otherAgent) {
            return this.name.compareTo(otherAgent.name);
        }
    }
}
