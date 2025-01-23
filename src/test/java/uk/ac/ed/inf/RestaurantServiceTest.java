package uk.ac.ed.inf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.Restaurant;
import uk.ac.ed.inf.data.Pizza;
import uk.ac.ed.inf.external.RestaurantService;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RestaurantServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restaurantService = new RestaurantService();
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
        }

        assertNotNull(result);
        assertNotNull(mockRestaurants.length);
    }

}
