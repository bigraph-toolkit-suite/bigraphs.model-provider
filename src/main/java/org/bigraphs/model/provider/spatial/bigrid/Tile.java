package org.bigraphs.model.provider.spatial.bigrid;

import lombok.Getter;

import java.util.*;

public class Tile {

    private List<Integer> possibilities;
    @Getter
    private int entropy;
    @Getter
    private Map<BiGridConfig.Direction, Tile> neighbours;
    @Getter
    private final int x;
    @Getter
    private final int y;

    public Tile(int x, int y) {
        // Initialize possibilities and entropy
        this.possibilities = new ArrayList<>(BiGridConfig.tileRules.keySet());
        this.entropy = possibilities.size();
        this.x = x;
        this.y = y;

        // Initialize neighbours map
        this.neighbours = new HashMap<>();
    }

    // Add a neighbour tile
    public void addNeighbour(BiGridConfig.Direction direction, Tile tile) {
        neighbours.put(direction, tile);
    }

    // Get a neighbour in a specific direction
    public Tile getNeighbour(BiGridConfig.Direction direction) {
        return neighbours.get(direction);
    }

    // Get all directions for existing neighbours
    public List<BiGridConfig.Direction> getDirections() {
        return new ArrayList<>(neighbours.keySet());
    }

    // Get the current possibilities for this tile
    public List<Integer> getPossibilities() {
        return possibilities;
    }

    // Collapse the tile's possibilities to one, based on weights
    public void collapse() {
        List<Integer> weights = new ArrayList<>();
        for (int possibility : possibilities) {
            weights.add(BiGridConfig.tileWeights.get(possibility));
        }

        // Select one possibility based on weights
        int chosenIndex = getWeightedRandomIndex(weights);
        this.possibilities = Collections.singletonList(possibilities.get(chosenIndex));
        this.entropy = 0;
    }

    // Constrain possibilities based on a neighbour's possibilities and direction
    public boolean constrain(List<Integer> neighbourPossibilities, BiGridConfig.Direction direction) {
        boolean reduced = false;

        if (entropy > 0) {
            Set<Integer> connectors = new HashSet<>();
            for (int neighbourPossibility : neighbourPossibilities) {
                connectors.add(BiGridConfig.tileRules.get(neighbourPossibility).get(direction.ordinal()));
            }

            // Determine the opposite direction
            BiGridConfig.Direction opposite = getOppositeDirection(direction);

            // Check and remove incompatible possibilities
            Iterator<Integer> iterator = possibilities.iterator();
            while (iterator.hasNext()) {
                int possibility = iterator.next();
                if (!connectors.contains(BiGridConfig.tileRules.get(possibility).get(opposite.ordinal()))) {
                    iterator.remove();
                    reduced = true;
                }
            }

            // Update entropy
            this.entropy = possibilities.size();
        }

        return reduced;
    }

    // Get the opposite direction
    private BiGridConfig.Direction getOppositeDirection(BiGridConfig.Direction direction) {
        switch (direction) {
            case NORTH:
                return BiGridConfig.Direction.SOUTH;
            case EAST:
                return BiGridConfig.Direction.WEST;
            case SOUTH:
                return BiGridConfig.Direction.NORTH;
            case WEST:
                return BiGridConfig.Direction.EAST;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    // Helper method to get a weighted random index
    private int getWeightedRandomIndex(List<Integer> weights) {
        int totalWeight = weights.stream().mapToInt(Integer::intValue).sum();
        int randomValue = new Random().nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (int i = 0; i < weights.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (randomValue < cumulativeWeight) {
                return i;
            }
        }
        throw new IllegalStateException("No valid index found for weights.");
    }
}
