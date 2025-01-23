package uk.ac.ed.inf.flightpath;

import uk.ac.ed.inf.data.LngLat;

/**
 * Represents a node in the pathfinding algorithm.
 * Each node contains information about its position, parent, and cost values.
 */
public class Node implements Comparable<Node> {
    private LngLat position;
    private Node parent;
    private double gCost;   // Cost from the start node to this node
    private double hCost;   // Heuristic cost from this node to goal
    private double fCost;   // Total cost (gCost + hCost)

    /**
     * Constructs a new node for pathfinding.
     *
     * @param position The geographic position of this node.
     * @param parent   The parent node in the path (null for the start node).
     * @param gCost    The cost from the start node to this node.
     * @param hCost    The estimated cost from this node to the goal.
     */
    public Node(LngLat position, Node parent, double gCost, double hCost) {
        this.position = position;
        this.parent = parent;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
    }

    /**
     * Retrieves the position of this node.
     *
     * @return The position as a {@link LngLat} object.
     */
    public LngLat getPosition() {
        return position;
    }

    /**
     * Retrieves the parent node in the path.
     *
     * @return The parent {@link Node}, or null if this is the starting node.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Retrieves the cost from the start node to this node.
     *
     * @return The gCost value.
     */
    public double getGCost() {
        return gCost;
    }

    /**
     * Retrieves the heuristic estimated cost from this node to the goal.
     *
     * @return The hCost value.
     */
    public double getHCost() {
        return hCost;
    }

    /**
     * Retrieves the total cost (gCost + hCost).
     *
     * @return The fCost value.
     */
    public double getFCost() {
        return fCost;
    }

    /**
     * Compares this node with another node based on their fCost.
     * This is used for priority ordering in a priority queue for pathfinding.
     *
     * @param other The node to compare against.
     * @return A negative integer, zero, or a positive integer as this node
     *         has a lower, equal, or higher fCost than the other node.
     */
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.fCost, other.fCost);
    }

}
