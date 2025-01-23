package uk.ac.ed.inf.external;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.data.NoFlyZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class responsible for fetching the no-fly zones from the API.
 */
@Service
public class NoFlyZoneService {

    private final RestTemplate restTemplate;

    // ✅ Correctly inject the RestTemplate
    public NoFlyZoneService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches the list of no-fly zones.
     *
     * @return A list of {@link NoFlyZone} objects representing no-fly areas.
     * If the API response is null, throws an exception.
     */
    public List<NoFlyZone> fetchNoFlyZones() {
        String url = "https://ilp-rest-2024.azurewebsites.net/noFlyZones";
        NoFlyZone[] zonesArray = restTemplate.getForObject(url, NoFlyZone[].class);

        if (zonesArray == null) {
            throw new NullPointerException("Received null response from API");
        }
        return Arrays.asList(zonesArray);
    }
}
