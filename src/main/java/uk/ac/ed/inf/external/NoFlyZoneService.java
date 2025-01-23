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

    public NoFlyZoneService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetches the list of no-fly zones
     *
     * @return A list of {@link NoFlyZone} objects representing no fly areas.
     * If the API response is null, returns an empty list.
     */
    public List<NoFlyZone> fetchNoFlyZones() {
        String url = "https://ilp-rest-2024.azurewebsites.net/noFlyZones";
        NoFlyZone[] zonesArray = restTemplate.getForObject(url, NoFlyZone[].class);

        if (zonesArray == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(zonesArray);
    }

}
