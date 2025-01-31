package org.bigraphs.model.provider.spatial.bigrid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiGridConfig {

    // Directions
    public enum Direction {
        NORTH, EAST, SOUTH, WEST;
    }

    // Tile Types
    public interface Types {

    }
    public static final int TILE_BLANK = 0;
    public static final int TILE_LOCALE_NESW = 1;
    public static final int TILE_LOCALE_N = 2;
    public static final int TILE_LOCALE_E = 3;
    public static final int TILE_LOCALE_S = 4;
    public static final int TILE_LOCALE_W = 5;
    public static final int TILE_LOCALE_NE = 6;
    public static final int TILE_LOCALE_SE = 7;
    public static final int TILE_LOCALE_SW = 8;
    public static final int TILE_LOCALE_NW = 9;

    // Tile Interfaces, or "faces" (i.e., "edges" of the tiles)
    public interface Faces {

    }
    public static final int ROUTE_BLANK = 0;
    public static final int ROUTE_NESW = 1;
    public static final int ROUTE_SINGLE_N = 2;
    public static final int ROUTE_SINGLE_E = 3;
    public static final int ROUTE_SINGLE_S = 4;
    public static final int ROUTE_SINGLE_W = 5;
    public static final int ROUTE_L_N = 6;
    public static final int ROUTE_L_E = 7;
    public static final int ROUTE_L_S = 8;
    public static final int ROUTE_L_W = 9;

    // Map to store tile rules
    // for all tile types along with the tile interfaces
    // on the directions (North, East, South, West)
    public static final Map<Integer, List<Integer>> tileRules = new HashMap<>();

    static {
        // Initialize the map [North, East, South, West]
        // Blocking Tile
        tileRules.put(TILE_BLANK, Arrays.asList(ROUTE_BLANK, ROUTE_BLANK, ROUTE_BLANK, ROUTE_BLANK));
        // 4-Way Tile
        tileRules.put(TILE_LOCALE_NESW, Arrays.asList(ROUTE_NESW, ROUTE_NESW, ROUTE_NESW, ROUTE_NESW));
        // Single-Route Tiles
        tileRules.put(TILE_LOCALE_N, Arrays.asList(ROUTE_NESW, ROUTE_SINGLE_N, ROUTE_BLANK, ROUTE_SINGLE_N));
        tileRules.put(TILE_LOCALE_E, Arrays.asList(ROUTE_SINGLE_E, ROUTE_NESW, ROUTE_SINGLE_E, ROUTE_BLANK));
        tileRules.put(TILE_LOCALE_S, Arrays.asList(ROUTE_BLANK, ROUTE_SINGLE_S, ROUTE_NESW, ROUTE_SINGLE_S));
        tileRules.put(TILE_LOCALE_W, Arrays.asList(ROUTE_SINGLE_W, ROUTE_BLANK, ROUTE_SINGLE_W, ROUTE_NESW));
        // L-Tiles v1
//        tileRules.put(TILE_LOCALE_NE, Arrays.asList(ROUTE_NESW, ROUTE_NESW, ROUTE_SINGLE_E, ROUTE_SINGLE_N));
//        tileRules.put(TILE_LOCALE_SE, Arrays.asList(ROUTE_SINGLE_E, ROUTE_NESW, ROUTE_NESW, ROUTE_SINGLE_S));
//        tileRules.put(TILE_LOCALE_SW, Arrays.asList(ROUTE_SINGLE_W, ROUTE_SINGLE_S, ROUTE_NESW, ROUTE_NESW));
//        tileRules.put(TILE_LOCALE_NW, Arrays.asList(ROUTE_NESW, ROUTE_SINGLE_N, ROUTE_SINGLE_W, ROUTE_NESW));
    }

    // Map to store tile weights
    public static final Map<Integer, Integer> tileWeights = new HashMap<>();

    static {
        // Initialize the map
        tileWeights.put(TILE_LOCALE_NESW, 16);
        tileWeights.put(TILE_BLANK, 4);
        tileWeights.put(TILE_LOCALE_N, 5);
        tileWeights.put(TILE_LOCALE_E, 5);
        tileWeights.put(TILE_LOCALE_S, 5);
        tileWeights.put(TILE_LOCALE_W, 5);
        tileWeights.put(TILE_LOCALE_NE, 5);
        tileWeights.put(TILE_LOCALE_SE, 5);
        tileWeights.put(TILE_LOCALE_SW, 5);
        tileWeights.put(TILE_LOCALE_NW, 5);
    }
}