package uk.ac.ed.inf.structural;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NoFlyZone;
import uk.ac.ed.inf.flightpath.PathfindingAlgorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StructuralTests {

    private List<NoFlyZone> noFlyZones;
    private List<LngLat> centralArea;

    @BeforeEach
    void setUp() {
        centralArea = Arrays.asList(
                new LngLat(-3.192473, 55.946233),
                new LngLat(-3.192473, 55.942617),
                new LngLat(-3.184319, 55.942617),
                new LngLat(-3.184319, 55.946233),
                new LngLat(-3.192473, 55.946233)
        );

        NoFlyZone noFlyZone = new NoFlyZone("test NoFlyZone", Arrays.asList(
                new LngLat(-3.190, 55.944),
                new LngLat(-3.189, 55.945),
                new LngLat(-3.188, 55.944),
                new LngLat(-3.189, 55.943),
                new LngLat(-3.190, 55.944)
        ));

        noFlyZones = Collections.singletonList(noFlyZone);
    }

    @Test
    void testIsPointInsidePolygon_Inside() {
        LngLat insidePoint = new LngLat(-3.188, 55.944);
        assertTrue(PathfindingAlgorithm.testIsPointInsidePolygon(insidePoint, centralArea),
                "Point should be inside the polygon");
    }

    @Test
    void testIsPointInsidePolygon_Outside() {
        LngLat outsidePoint = new LngLat(-3.195, 55.948);
        assertFalse(PathfindingAlgorithm.testIsPointInsidePolygon(outsidePoint, centralArea),
                "Point should be outside the polygon");
    }

    @Test
    void testIsPointInsidePolygon_OnBoundary() {
        LngLat boundaryPoint = new LngLat(-3.192473, 55.946233);
        assertTrue(PathfindingAlgorithm.testIsPointInsidePolygon(boundaryPoint, centralArea),
                "Point on the boundary should be considered inside");
    }

    @Test
    void testValidPathExists() {
        LngLat start = new LngLat(-3.191, 55.945);
        LngLat goal = new LngLat(-3.186874, 55.944494);

        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, noFlyZones, centralArea);

        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertTrue(path.get(0).equals(start));
        assertTrue(path.get(path.size() - 1).isCloseTo(goal, 0.00015));
    }


    @Test
    void testAvoidsNoFlyZone() {
        LngLat start = new LngLat(-3.191, 55.945);
        LngLat goal = new LngLat(-3.186874, 55.944494);

        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, noFlyZones, centralArea);

        assertNotNull(path);
        assertFalse(path.isEmpty());

        // Print path to debug
        System.out.println("Generated path:");
        if (path != null) {
            path.forEach(System.out::println);
        }

        // Ensure no points are in the NoFlyZone
        for (LngLat point : path) {
            assertFalse(PathfindingAlgorithm.testIsPointInsidePolygon(point, noFlyZones.get(0).getVertices()),
                    "Path should not enter the no-fly zone");
        }
    }


    @Test
    void testFindPath_SimplePath() {
        LngLat start = new LngLat(-3.190, 55.944);
        LngLat goal = new LngLat(-3.185, 55.944);

        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, noFlyZones, centralArea);

        assertNotNull(path, "Path should be found");
        assertFalse(path.isEmpty(), "Path should contain waypoints");
        assertTrue(path.get(path.size() - 1).isCloseTo(goal, 0.00015),
                "Final position should be close to goal");
    }

    @Test
    void testPathfindingWithNoObstacles() {
        LngLat start = new LngLat(-3.191, 55.945);
        LngLat goal = new LngLat(-3.186, 55.944);

        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, Collections.emptyList(), centralArea);

        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertTrue(path.get(path.size() - 1).isCloseTo(goal, 0.00015));
    }

    @Test
    void testPathDirectlyNextToDeliveryPoint() {
        LngLat start = new LngLat(-3.192, 55.946);
        LngLat goal = new LngLat(-3.191, 55.946); // Very close

        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, Collections.emptyList(), centralArea);

        assertNotNull(path);
    }

    @Test
    void testStartAndGoalSamePoint() {
        LngLat start = new LngLat(-3.191, 55.945);

        List<LngLat> path = PathfindingAlgorithm.findPath(start, start, noFlyZones, centralArea);

        assertNotNull(path);
        assertEquals(1, path.size(), "Path should contain only the start point.");
        assertEquals(start, path.get(0), "Start and goal should be the same.");
    }

    @Test
    void testGoalOutsideCentralArea() {
        LngLat start = new LngLat(-3.191, 55.945);
        LngLat goal = new LngLat(-3.180, 55.950); // Outside central area

        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, noFlyZones, centralArea);

        assertNull(path, "Path should not exist since the goal is outside the central area.");
    }

    @Test
    void testBlockedPathByNoFlyZones() {
        NoFlyZone largeBlock = new NoFlyZone("Blocked Area", Arrays.asList(
                new LngLat(-3.190, 55.945), new LngLat(-3.189, 55.946),
                new LngLat(-3.188, 55.945), new LngLat(-3.189, 55.944), new LngLat(-3.190, 55.945)
        ));

        List<LngLat> path = PathfindingAlgorithm.findPath(
                new LngLat(-3.191, 55.945),
                new LngLat(-3.188, 55.945),
                Collections.singletonList(largeBlock),
                centralArea
        );

        assertNull(path, "No valid path should be found as the destination is blocked.");
    }

    @Test
    void testPathfindingPerformanceForSimplePath() {
        LngLat start = new LngLat(-3.190, 55.944);
        LngLat goal = new LngLat(-3.185, 55.944);

        long startTime = System.nanoTime();
        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, noFlyZones, centralArea);
        long endTime = System.nanoTime();

        long executionTime = (endTime - startTime) / 1_000_000;

        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertTrue(executionTime < 200, "Pathfinding should complete in under 200ms, but took " + executionTime + "ms.");
    }

    @Test
    void testPathfindingPerformance() {
        LngLat start = new LngLat(-3.191, 55.945);
        LngLat goal = new LngLat(-3.186874, 55.944494);

        long startTime = System.nanoTime();
        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, noFlyZones, centralArea);
        long endTime = System.nanoTime();

        long executionTime = (endTime - startTime) / 1_000_000;

        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertTrue(executionTime < 200, "Pathfinding should complete in under 200ms, but took " + executionTime + "ms.");
    }


}
