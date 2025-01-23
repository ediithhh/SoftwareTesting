package uk.ac.ed.inf.modelbased;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NoFlyZone;
import uk.ac.ed.inf.flightpath.PathfindingAlgorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PathfindingAlgorithmTestModelBased {

    private LngLat start;
    private LngLat goal;

    private List<NoFlyZone> noFlyZones;
    private List<LngLat> centralArea;

    @BeforeEach
    void setUp() {
        start = new LngLat(-3.190, 55.944);
        goal = new LngLat(-3.185049999999997, 55.944);

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
    void testPathfindingValidPath() {
        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, List.of(), List.of());

        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(goal, path.get(path.size() - 1));
    }

    @Test
    void testPathfindingAvoidNoFlyZones() {
        LngLat start = new LngLat(-3.191, 55.945);
        LngLat goal = new LngLat(-3.186874, 55.944494);

        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, noFlyZones, centralArea);

        assertNotNull(path);
        assertFalse(path.contains(noFlyZones), "Path should avoid no-fly " +
                "zones");
    }

    @Test
    void testPathfindingFailsForDistantGoal() {
        LngLat farGoal = new LngLat(-3.100, 55.900);
        List<LngLat> path = PathfindingAlgorithm.findPath(start, farGoal, noFlyZones, centralArea);

        assertNull(path, "Path should return null if no valid paths found");
    }

}
