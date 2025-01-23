package uk.ac.ed.inf.external;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.data.LngLat;

import java.util.List;
import java.util.Map;

/**
 * Service class responsible for fetching the central area from the API.
 */
@Service
public class CentralAreaService {

    private final RestTemplate restTemplate;

    public CentralAreaService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetches the central area coordinates from the external API.
     *
     * @return A list of {@link LngLat} objects representing the vertices of the central area.
     * @throws RuntimeException if the request fails or the response structure is incorrect.
     */
    public List<LngLat> fetchCentralArea() {
        String url = "https://ilp-rest-2024.azurewebsites.net/centralArea";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("vertices")) {
            throw new RuntimeException("Failed to fetch central area");
        }

        List<Map<String, Double>> verticesData =
                (List<Map<String, Double>>) response.get("vertices");
        return verticesData.stream()
                .map(v -> new LngLat(v.get("lng"), v.get("lat")))
                .toList();

    }

}
