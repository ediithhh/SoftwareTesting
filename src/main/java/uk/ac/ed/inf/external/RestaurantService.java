package uk.ac.ed.inf.external;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.data.Restaurant;

/**
 * Service class responsible for fetching restaurant data from the API.
 */
@Service
public class RestaurantService {

    private final RestTemplate restTemplate;

    public RestaurantService() {this.restTemplate = new RestTemplate();}

    /**
     * Fetches restaurant data from the external API.
     *
     * @return an array of {@link Restaurant} objects retrieved from the API, or an empty array if the request fails.
     */
    public Restaurant[] fetchRestaurants() {
        String url = "https://ilp-rest-2024.azurewebsites.net/restaurants";
        return restTemplate.getForObject(url, Restaurant[].class);
    }
}
