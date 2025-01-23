package uk.ac.ed.inf.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NoFlyZone;
import uk.ac.ed.inf.external.NoFlyZoneService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class NoFlyZoneServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NoFlyZoneService noFlyZoneService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        noFlyZoneService = new NoFlyZoneService(restTemplate);
    }

    @Test
    void testFetchNoFlyZones_ValidResponse() {

        NoFlyZone[] mockZones = {
                new NoFlyZone("mock1", Arrays.asList(
                        new LngLat(-3.190, 55.944), // Bottom-left corner
                        new LngLat(-3.190, 55.946), // Top-left corner
                        new LngLat(-3.188, 55.946), // Top-right corner
                        new LngLat(-3.188, 55.944), // Bottom-right corner
                        new LngLat(-3.190, 55.944)  // Closing the polygon
                ))
        };

        when(restTemplate.getForObject(anyString(), eq(NoFlyZone[].class))).thenReturn(mockZones);

        List<NoFlyZone> result = noFlyZoneService.fetchNoFlyZones();

        // Assertions
        assertNotNull(result);
        System.out.println(result);
        assertEquals(1, result.size());
        assertEquals("mock1", result.get(0).getName());

        // Verify mock interaction
        verify(restTemplate, times(1)).getForObject(anyString(), eq(NoFlyZone[].class));
    }

    @Test
    void testFetchNoFlyZones_EmptyResponse() {
        // Mock API returning an empty array
        when(restTemplate.getForObject(anyString(), eq(NoFlyZone[].class))).thenReturn(new NoFlyZone[]{});


        // Call the service method
        List<NoFlyZone> result = noFlyZoneService.fetchNoFlyZones();

        // Assertions
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    @Test
    void testFetchNoFlyZones_ApiFailure() {
        // Mock API throwing an exception
        when(restTemplate.getForObject(anyString(), eq(NoFlyZone[].class))).thenThrow(new RuntimeException("API Failure"));

        // Call the service method and expect an exception
        assertThrows(RuntimeException.class, () -> noFlyZoneService.fetchNoFlyZones());

    }
}