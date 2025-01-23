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
import uk.ac.ed.inf.data.Restaurant;
import uk.ac.ed.inf.data.Pizza;
import uk.ac.ed.inf.external.RestaurantService;

import java.time.DayOfWeek;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RestaurantServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restaurantService = new RestaurantService(restTemplate);
    }

    @Test
    void testFetchRestaurants() {

        Pizza[] mockMenu = {
                new Pizza("R2: Meat Lover", 1400),
        };
        DayOfWeek[] openingDays = { DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY };
        LngLat mockLocation = new LngLat(-3.20254147052765, 55.9432847375794);

        Restaurant[] mockRestaurants = {
                new Restaurant("Mock Restaurant", mockLocation, openingDays, mockMenu)
        };

        when(restTemplate.getForObject(anyString(), eq(Restaurant[].class))).thenReturn(mockRestaurants);

        Restaurant[] result = restaurantService.fetchRestaurants();

        for (Restaurant restaurant : result) {
            assertNotNull(restaurant.name());
            assertFalse(restaurant.name().isEmpty());
            assertNotNull(restaurant.openingDays());
            assertNotNull(restaurant.location());
            assertNotNull(restaurant.menu());
            assertTrue(restaurant.menu().length >= 0);
            assertEquals("Mock Restaurant", result[0].name());
        }

        assertEquals(1, result.length);
        assertNotNull(result);
        assertNotNull(mockRestaurants.length);
    }

    @Test
    void testFetchRestaurantsApiFailure() {
        when(restTemplate.getForObject(anyString(), eq(Restaurant[].class)))
                .thenThrow(new RuntimeException("API Failure"));

        assertThrows(RuntimeException.class, () -> restaurantService.fetchRestaurants());
    }

    @Test
    void testFetchRestaurantsEmptyResponse() {
        when(restTemplate.getForObject(anyString(), eq(Restaurant[].class)))
                .thenReturn(new Restaurant[]{});

        Restaurant[] result = restaurantService.fetchRestaurants();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testFetchRestaurantsIncorrectFormat() {
        when(restTemplate.getForObject(anyString(), eq(Restaurant[].class)))
                .thenReturn(null);

        assertThrows(NullPointerException.class, () -> restaurantService.fetchRestaurants());
    }

    @Test
    void testFetchLargeMenu() {
        Pizza[] hugeMenu = new Pizza[200];
        Arrays.fill(hugeMenu, new Pizza("Super Pizza", 1200));

        Restaurant restaurant = new Restaurant("Big Restaurant",
                new LngLat(-3.2025, 55.9432),
                new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY},
                hugeMenu);

        when(restTemplate.getForObject(anyString(), eq(Restaurant[].class)))
                .thenReturn(new Restaurant[]{restaurant});

        Restaurant[] result = restaurantService.fetchRestaurants();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(200, result[0].menu().length);
    }


}
