package uk.ac.ed.inf.flightpath;

import uk.ac.ed.inf.constant.SystemConstants;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NoFlyZone;

import java.util.*;

/**
 * Implements a pathfinding algorithm for drone navigation.
 */
public class PathfindingAlgorithm {
    private static final double[] COMPASS_DIRECTIONS = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    private static final int MAX_ITERATIONS = 5000;
    private static final int MAX_NODES = 10000;

    /**
     * Finds the shortest path from the start position to the goal.
     *
     * @param start       The starting position of the drone.
     * @param goal        The target destination.
     * @param noFlyZones  A list of restricted no-fly zones.
     * @param centralArea The central area the drone must remain inside once entered.
     * @return A list of {@link LngLat} positions representing the calculated path, or null if no valid path is found.
     */
    public static List<LngLat> findPath (LngLat start, LngLat goal, List<NoFlyZone> noFlyZones, List<LngLat> centralArea) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();  // Nodes to be  explored
        Map<LngLat, Node> allNodes = new HashMap<>(); // Tracks visited nodes
        Set<LngLat> closedSet = new HashSet<>();  // Prevents revising nodes

        Node startNode = new Node(start, null, 0, heuristic(start,goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        int iterations = 0;
        boolean hasEnteredCentralArea = false;

        // Iteratively process nodes until a path is found or a limit is reached
        while (!openSet.isEmpty()) {
            if (++iterations > MAX_ITERATIONS) {
                return null;  // Abort if too many iterations
            }

            Node current = openSet.poll();  // Get the node with the lowest cost

            if (current.getPosition().isCloseTo(goal, SystemConstants.DRONE_IS_CLOSE_DISTANCE)) {
                return reconstructPath(current);
            }

            closedSet.add(current.getPosition());

            // Mark when the drone enters the central area
            if (!hasEnteredCentralArea && isPointInsidePolygon(current.getPosition(), centralArea)) {
                hasEnteredCentralArea = true;
            }

            for (double angle : COMPASS_DIRECTIONS) {
                LngLat nextPos = moveInDirection(current.getPosition(), angle);

                // Check that the next position is valid
                if (closedSet.contains(nextPos) || isInNoFlyZone(nextPos,noFlyZones) ||!isInsideCentralArea(nextPos, centralArea, hasEnteredCentralArea)) {
                    continue;
                }

                double gCost = current.getGCost() + SystemConstants.DRONE_MOVE_DISTANCE;

                // Only add if it's a better path
                if (!allNodes.containsKey(nextPos) || gCost < allNodes.get(nextPos).getGCost()) {
                    Node nextNode = new Node(nextPos, current, gCost, heuristic(nextPos, goal));
                    openSet.add(nextNode);
                    allNodes.put(nextPos, nextNode);
                }
            }
            if (allNodes.size() > MAX_NODES) {
                return null;
            }
        }
        return null;  // No valid path found
    }

    /**
     * Estimates the heuristic cost between two points using the Euclidean distance.
     *
     * @param a The first point.
     * @param b The second point.
     * @return The estimated cost between the two points.
     */
    private static double heuristic(LngLat a, LngLat b) {
        return 1.5 * Math.sqrt(Math.pow(a.lng() - b.lng(), 2) + Math.pow(a.lat() - b.lat(), 2));
    }

    /**
     * Moves the drone in the given direction based on the specified angle.
     *
     * @param position The starting position.
     * @param angle    The movement direction in degrees.
     * @return The new position after the move.
     */
    private static LngLat moveInDirection(LngLat position, double angle) {
        double radians = Math.toRadians(angle);
        double newLng = position.lng() + SystemConstants.DRONE_MOVE_DISTANCE * Math.cos(radians);
        double newLat = position.lat() + SystemConstants.DRONE_MOVE_DISTANCE * Math.sin(radians);
        return new LngLat(newLng, newLat);
    }

    /**
     * Checks if a given position falls inside a no-fly zone.
     *
     * @param position  The position to check.
     * @param noFlyZones The list of no-fly zones.
     * @return true if the position is inside a no-fly zone, false otherwise.
     */
    private static boolean isInNoFlyZone (LngLat position, List<NoFlyZone> noFlyZones) {
        for (NoFlyZone zone : noFlyZones) {
            if (isPointInsidePolygon(position, zone.getVertices())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a position is inside the central area.
     *
     * @param position             The position to check.
     * @param centralArea          The central area boundaries.
     * @param hasEnteredCentralArea Whether the drone has already entered the central area.
     * @return true if the position is valid within the central area rules, false otherwise.
     */
    private static boolean isInsideCentralArea(LngLat position, List<LngLat> centralArea, boolean hasEnteredCentralArea) {
        boolean inside = isPointInsidePolygon(position, centralArea);

        if (!hasEnteredCentralArea) {
            return true;
        }
        if (hasEnteredCentralArea && !inside) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether a point is inside a given polygon using the ray-casting algorithm.
     *
     * @param point   The point to check.
     * @param polygon The polygon represented as a list of vertices.
     * @return true if the point is inside the polygon, false otherwise.
     */
    private static boolean isPointInsidePolygon(LngLat point, List<LngLat> polygon) {
        int intersections = 0;
        int numVertices = polygon.size();

        for (int i = 0, j = numVertices - 1; i < numVertices; j = i++) {
            LngLat v1 = polygon.get(i);
            LngLat v2 = polygon.get(j);

            if (isPointOnEdge(point, v1, v2)) {
                return true;
            }

            if (((v1.lat() > point.lat()) != (v2.lat() > point.lat())) &&
                    (point.lng() < (v2.lng() - v1.lng()) * (point.lat() - v1.lat()) / (v2.lat() - v1.lat()) + v1.lng())) {
                intersections++;
            }

        }
        return (intersections % 2 == 1);
    }

    /**
     * Checks whether a point lies exactly on the edge of a polygon segment.
     *
     * @param point The point to check.
     * @param v1    The first vertex of the segment.
     * @param v2    The second vertex of the segment.
     * @return true if the point is on the segment, false otherwise.
     */
    private static boolean isPointOnEdge(LngLat point, LngLat v1, LngLat v2) {
        double minX = Math.min(v1.lng(), v2.lng());
        double maxX = Math.max(v1.lng(), v2.lng());
        double minY = Math.min(v1.lat(), v2.lat());
        double maxY = Math.max(v1.lat(), v2.lat());

        return (point.lng() >= minX && point.lng() <= maxX &&
                point.lat() >= minY && point.lat() <= maxY &&
                Math.abs((v2.lng() - v1.lng()) * (point.lat() - v1.lat()) -
                        (point.lng() - v1.lng()) * (v2.lat() - v1.lat())) < 1e-9);
    }

    public static boolean testIsPointInsidePolygon(LngLat point, List<LngLat> polygon) {
        return isPointInsidePolygon(point, polygon);
    }

    /**
     * Reconstructs the shortest path from the goal node back to the start.
     *
     * @param node The final node in the path.
     * @return A list of positions representing the reconstructed path.
     */
    private static List<LngLat> reconstructPath(Node node) {
        List<LngLat> path = new ArrayList<>();
        while (node != null) {
            path.add(node.getPosition());
            node = node.getParent();
        }
        Collections.reverse(path);
        return path;
    }

}