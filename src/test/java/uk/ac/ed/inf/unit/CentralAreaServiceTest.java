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
import uk.ac.ed.inf.external.CentralAreaService;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CentralAreaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CentralAreaService centralAreaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        centralAreaService = new CentralAreaService(restTemplate);
    }

    @Test
    void testFetchCentralArea_ValidResponse() {

        Map<String, Object> mockResponse = Map.of(
                "vertices", List.of(
                        Map.of("lng", -3.190, "lat", 55.944),
                        Map.of("lng", -3.190, "lat", 55.946),
                        Map.of("lng", -3.188, "lat", 55.946),
                        Map.of("lng", -3.188, "lat", 55.944)
                )
        );

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        List<LngLat> result = centralAreaService.fetchCentralArea();

        // Assertions
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(-3.190, result.get(0).lng());
        assertEquals(55.944, result.get(0).lat());

        // Verify mock interaction
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void testFetchCentralArea_NullResponse() {
        // Mock API returning null
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        // Call the service method and expect an exception
        assertThrows(NullPointerException.class, () -> centralAreaService.fetchCentralArea());

        // Verify mock interaction
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void testFetchCentralArea_MissingVerticesKey() {
        // Mock API response without "vertices" key
        Map<String, Object> mockResponse = Map.of("invalidKey", "someValue");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // Call the service method and expect an exception
        assertThrows(NullPointerException.class, () -> centralAreaService.fetchCentralArea());

        // Verify mock interaction
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Map.class));
    }
}
