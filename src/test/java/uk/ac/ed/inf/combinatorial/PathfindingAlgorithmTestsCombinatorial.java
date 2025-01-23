package uk.ac.ed.inf.combinatorial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NoFlyZone;
import uk.ac.ed.inf.flightpath.PathfindingAlgorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PathfindingAlgorithmTestsCombinatorial {

    @Mock
    private NoFlyZone mockNoFlyZone;

    private List<NoFlyZone> noFlyZones;
    private List<LngLat> centralArea;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock a no-fly zone as a simple polygon
        List<LngLat> mockPolygon = Arrays.asList(
                new LngLat(-3.19057881832123, 55.9440241257753),
                new LngLat(-3.18998873233795, 55.9428465054091),
                new LngLat(-3.1870973110199, 55.9432881172426),
                new LngLat(-3.18768203258514, 55.9444777403937),
                new LngLat(-3.19057881832123, 55.9440241257753)  // Closing the polygon
        );

        mockNoFlyZone = mock(NoFlyZone.class);
        when(mockNoFlyZone.getVertices()).thenReturn(mockPolygon);

        // Mock a central area as a simple bounding box
        centralArea = Arrays.asList(
                new LngLat(-3.192473, 55.946233),
                new LngLat(-3.192473, 55.942617),
                new LngLat(-3.184319, 55.942617),
                new LngLat(-3.184319, 55.946233),
                new LngLat(-3.192473, 55.946233)
        );

        noFlyZones = Collections.singletonList(mockNoFlyZone);
    }

    // -3.186874, 55.944494
    @ParameterizedTest
    @CsvSource({
            "-3.18385720252991, 55.9444987687571, -3.186874, 55.944494, true", // Direct path
            "-3.20254147052765, 55.9432847375794, -3.186874, 55.944494, true", // Must avoid no-fly zone
            "-3.19401741027832, 55.9439069661694, -3.186874, 55.944494, true", // Must enter central area
            "-3.192, 55.946, -3.100, 55.900, false" // No valid path
    })
    void testPathfindingAlgorithm(double startLng, double startLat, double endLng, double endLat, boolean expected) {
        LngLat start = new LngLat(startLng, startLat);
        LngLat goal = new LngLat(endLng, endLat);

        List<LngLat> path = PathfindingAlgorithm.findPath(start, goal, noFlyZones, centralArea);

        boolean hasValidPath = path != null && !path.isEmpty();
        assertEquals(expected, hasValidPath, "Pathfinding result should match expected");
    }
}
