package org.bigraphs.model.provider.spatial.bigrid;

import java.util.*;

public class World {
    private int cols;
    private int rows;
    private Tile[][] tileRows;

    // Constructor
    public World(int sizeX, int sizeY) {
        this.rows = sizeX;
        this.cols = sizeY;
        this.tileRows = new Tile[sizeX][sizeY];

        // Initialize tiles
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                tileRows[x][y] = new Tile(x, y);
            }
        }

        // Add neighbors
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                Tile tile = tileRows[x][y];
                if (x > 0) {
                    tile.addNeighbour(BiGridConfig.Direction.NORTH, tileRows[x - 1][y]);
                }
                if (y < sizeY - 1) {
                    tile.addNeighbour(BiGridConfig.Direction.EAST, tileRows[x][y + 1]);
                }
                if (x < sizeX - 1) {
                    tile.addNeighbour(BiGridConfig.Direction.SOUTH, tileRows[x + 1][y]);
                }
                if (y > 0) {
                    tile.addNeighbour(BiGridConfig.Direction.WEST, tileRows[x][y - 1]);
                }
            }
        }
    }

    // Get entropy of a tile
    public int getEntropy(int x, int y) {
        return tileRows[x][y].getEntropy();
    }

    // Get type of a tile
    public int getType(int x, int y) {
        if(tileRows[x][y].getPossibilities().isEmpty()) {
            System.out.println("x,y = " + x + ", " + y);
            assert !tileRows[x][y].getPossibilities().isEmpty();
        }
        return tileRows[x][y].getPossibilities().get(0);
    }

    // Find the lowest entropy in the world
    public int getLowestEntropy() {
        int lowestEntropy = BiGridConfig.tileRules.keySet().size();
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                int tileEntropy = tileRows[x][y].getEntropy();
                if (tileEntropy > 0 && tileEntropy < lowestEntropy) {
                    lowestEntropy = tileEntropy;
                }
            }
        }
        return lowestEntropy;
    }

    // Get all tiles with the lowest entropy
    public List<Tile> getTilesLowestEntropy() {
        int lowestEntropy = BiGridConfig.tileRules.keySet().size();
        List<Tile> tileList = new ArrayList<>();

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                int tileEntropy = tileRows[x][y].getEntropy();
                if (tileEntropy > 0) {
                    if (tileEntropy < lowestEntropy) {
                        tileList.clear();
                        lowestEntropy = tileEntropy;
                    }
                    if (tileEntropy == lowestEntropy) {
                        tileList.add(tileRows[x][y]);
                    }
                }
            }
        }
        return tileList;
    }

    // Perform Wave Function Collapse
    public int waveFunctionCollapse() {
        List<Tile> tilesLowestEntropy = getTilesLowestEntropy();

        if (tilesLowestEntropy.isEmpty()) {
            return 0;
        }

        // Choose a random tile to collapse
        Tile tileToCollapse = tilesLowestEntropy.get(new Random().nextInt(tilesLowestEntropy.size()));
        tileToCollapse.collapse();

        // Propagation using stack
//        Stack<Tile> stack = new Stack<>();
        Queue<Tile> stack = new LinkedList<>();
//        stack.push(tileToCollapse);
        stack.add(tileToCollapse);

        while (!stack.isEmpty()) {
//            Tile tile = stack.pop();
            Tile tile = stack.remove();
            List<Integer> tilePossibilities = tile.getPossibilities();
            List<BiGridConfig.Direction> directions = tile.getDirections();

            for (BiGridConfig.Direction direction : directions) {
                Tile neighbour = tile.getNeighbour(direction);
                if (neighbour.getEntropy() != 0) {
                    boolean reduced = neighbour.constrain(tilePossibilities, direction);
                    if (reduced) {
//                        stack.push(neighbour); // Propagate further if reduced
                        stack.add(neighbour); // Propagate further if reduced
                    }
                }
            }
        }

        return 1;
    }
}