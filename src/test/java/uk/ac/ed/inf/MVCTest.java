package uk.ac.ed.inf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import uk.ac.ed.inf.data.Order;
import uk.ac.ed.inf.data.LngLat;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MVCTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void testIsAlive() throws Exception {
        String url = "http://localhost:" + port + "/isAlive";
        String response = restTemplate.getForObject(url, String.class);
        assertThat(response).isEqualTo("true");
    }

    @Test
    void testGetUuid() {
        String url = "http://localhost:" + port + "/uuid";
        String response = restTemplate.getForObject(url, String.class);
        assertThat(response).isEqualTo("s2277575");
    }

    private <T> ResponseEntity<T> sendPostRequest(String url, String requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForEntity(url, request, responseType);
    }

    @Test
    void testDistanceTo() {
        String url = "http://localhost:" + port + "/distanceTo";

        String requestBody = """
            {
                "position1": { "lng": -3.192473, "lat": 55.946233 },
                "position2": { "lng": -3.184319, "lat": 55.942617 }
            }
        """;

        ResponseEntity<Double> response = sendPostRequest(url, requestBody, Double.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isGreaterThan(0);
    }

    @Test
    void testDistanceTo_SemanticError() {
        String url = "http://localhost:" + port + "/distanceTo";

        String requestBody = """
            {
                "position1": { "lng": 200.0, "lat": 100.0 }, 
                "position2": { "lng": -3200.184319, "lat": 5593.942617 }
            }
        """;

        ResponseEntity<Double> response = sendPostRequest(url, requestBody, Double.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testDistanceTo_SyntaxError() {
        String url = "http://localhost:" + port + "/distanceTo";

        String requestBody = """
            {
                "position2": { "lng": -3.184319, "lat": 55.942617 }
            }
        """;

        ResponseEntity<Double> response = sendPostRequest(url, requestBody, Double.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testIsCloseTo() {
        String url = "http://localhost:" + port + "/isCloseTo";

        String requestBody = """
            {
                "position1": { "lng": -3.192473, "lat": 55.946233 },
                "position2": { "lng": -3.192473, "lat": 55.946233 }
            }
        """;

        ResponseEntity<Boolean> response = sendPostRequest(url, requestBody, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    void testIsCloseTo_Invalid() {
        String url = "http://localhost:" + port + "/isCloseTo";

        String requestBody = """
            {
                "position1": { "lng": -3.192473, "lat": 55.946233 },
                "position2": { "lng": -3.100000, "lat": 55.900000 }
            }
        """;

        ResponseEntity<Boolean> response = sendPostRequest(url, requestBody, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();
    }

    @Test
    void testIsCloseTo_SemanticError() {
        String url = "http://localhost:" + port + "/isCloseTo";

        String requestBody = """
            {
                "position1": { "lng": 200.0, "lat": 100.0 },
                "position2": { "lng": -3200.184319, "lat": 5593.942617 }
            }
        """;

        ResponseEntity<Boolean> response = sendPostRequest(url, requestBody, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testIsCloseTo_SyntaxError() {
        String url = "http://localhost:" + port + "/isCloseTo";

        String requestBody = """
            {
                "position1": { "lng": -3.192473, "lat": 55.946233 },
                "position3": { "lng": -3.192473, "lat": 55.946233 }
            }
        """;

        ResponseEntity<Boolean> response = sendPostRequest(url, requestBody, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testNextPosition() {
        String url = "http://localhost:" + port + "/nextPosition";

        String requestBody = """
            {
                "start": { "lng": -3.192473, "lat": 55.946233 },
                "angle": 90
            }
        """;

        ResponseEntity<LngLat> response = sendPostRequest(url, requestBody, LngLat.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testNextPosition_SemanticError() {
        String url = "http://localhost:" + port + "/nextPosition";

        String requestBody = """
            {
                "start": { "lng": -3.192473, "lat": 55.946233 },
                "angle": 900
            }
        """;

        ResponseEntity<LngLat> response = sendPostRequest(url, requestBody, LngLat.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testNextPosition_SyntaxError() {
        String url = "http://localhost:" + port + "/nextPosition";

        String requestBody = """
            {
                "startPosition": { "lng": -3.192473, "lat": 55.946233 },
                "angle": 90
            }
        """;

        ResponseEntity<LngLat> response = sendPostRequest(url, requestBody, LngLat.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void testIsInRegion() {
        String url = "http://localhost:" + port + "/isInRegion";

        String requestBody = """
            {
                "position": { "lng": -3.192473, "lat": 55.946233 },
                "region": {
                    "name": "Central",
                    "vertices": [
                        { "lng": -3.192, "lat": 55.946 },
                        { "lng": -3.193, "lat": 55.946 },
                        { "lng": -3.193, "lat": 55.945 },
                        { "lng": -3.192, "lat": 55.945 },
                        { "lng": -3.192, "lat": 55.946 }
                    ]
                }
            }
        """;

        ResponseEntity<Boolean> response = sendPostRequest(url, requestBody, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testIsInRegion_InvalidPolygon() {
        String url = "http://localhost:" + port + "/isInRegion";

        String requestBody = """
            {
                "position": { "lng": -3.192473, "lat": 55.946233 },
                "region": {
                    "name": "Central",
                    "vertices": [
                        { "lng": -3.192, "lat": 55.946 },
                        { "lng": -3.193, "lat": 55.946 }
                    ]
                }
            }
        """;

        ResponseEntity<Boolean> response = sendPostRequest(url, requestBody, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testIsInRegion_SemanticError() {
        String url = "http://localhost:" + port + "/isInRegion";

        String requestBody = """
            {
                "position": { "lng": -3000.192473, "lat": 5500.946233 },
                "region": {
                    "name": "Central",
                    "vertices": [
                        { "lng": -3.192, "lat": 550.946 },
                        { "lng": -300.193, "lat": 55.946 },
                        { "lng": -3.193, "lat": 55.945 },
                        { "lng": -3.192, "lat": 55.945 },
                        { "lng": -3.192, "lat": 55.946 }
                    ]
                }
            }
        """;

        ResponseEntity<Boolean> response = sendPostRequest(url, requestBody, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testIsInRegion_SyntaxError() {
        String url = "http://localhost:" + port + "/isInRegion";

        String requestBody = """
            {
                "startingPosition": { "lng": -3.192473, "lat": 55.946233 },
                "region": {
                    "name": "Central",
                    "vertices": [
                        { "lng": -3.192, "lat": 55.946 },
                        { "lng": -3.193, "lat": 55.946 },
                        { "lng": -3.193, "lat": 55.945 },
                        { "lng": -3.192, "lat": 55.945 },
                        { "lng": -3.192, "lat": 55.946 }
                    ]
                }
            }
        """;

        ResponseEntity<Boolean> response = sendPostRequest(url, requestBody, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testValidateOrder() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 3071,
                    "pizzasInOrder": [
                      {
                        "name": "R2: Meat Lover",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R2: Vegan Delight",
                        "priceInPence": 1100
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "5213394818929712",
                      "creditCardExpiry": "02/26",
                      "cvv": "679"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testValidateOrder_MissingFields() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
            {
                "orderNo": "11496050"
            }
        """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCalcDeliveryPath() {
        String url = "http://localhost:" + port + "/calcDeliveryPath";

        String requestBody = """
                {
                    "orderNo": "77DAD717",
                    "orderDate": "2025-01-16",
                    "orderStatus": "VALID",
                    "orderValidationCode": "NO_ERROR",
                    "priceTotalInPence": 2400,
                    "pizzasInOrder": [
                      {
                        "name": "R3: Super Cheese",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R3: All Shrooms",
                        "priceInPence": 900
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "5223358194283703",
                      "creditCardExpiry": "09/25",
                      "cvv": "473"
                    }
                }
                """;

        ResponseEntity<LngLat[]> response = sendPostRequest(url, requestBody, LngLat[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    void testCalcDeliveryPath_InvalidOrder() {
        String url = "http://localhost:" + port + "/calcDeliveryPath";

        String requestBody = """
            {
                "orderNo": "4A1BF4CE",
                "pizzasInOrder": [ { "name": "R3: Super Cheese", "priceInPence": 1400 } ]
            }
        """;

        ResponseEntity<LngLat[]> response = sendPostRequest(url, requestBody, LngLat[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCalcDeliveryPath_SyntaxError() {
        String url = "http://localhost:" + port + "/calcDeliveryPath";

        String requestBody = """
                {
                    "orderNo": "77DAD717",
                    "orderDate": "2025-01-16",
                    "orderStatus": "VALID",
                    "orderValidationCode": "NO_ERROR",
                    "priceTotal": 2400,
                    "pizzas": [
                      {
                        "name": "R3: Super Cheese",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R3: All Shrooms",
                        "priceInPence": 900
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "5223358194283703",
                      "creditCardExpiry": "09/25",
                      "cvv": "473"
                    }
                }
                """;

        ResponseEntity<LngLat[]> response = sendPostRequest(url, requestBody, LngLat[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCalcDeliveryPath_EmptyOrder() {
        String url = "http://localhost:" + port + "/calcDeliveryPath";

        String requestBody = """
                {
                    "orderNo": "77DAD717",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 0,
                    "pizzasInOrder": [],
                    "creditCardInformation": {
                        "creditCardNumber": "5223358194283703",
                        "creditCardExpiry": "09/25",
                        "cvv": "473"
                    }
                }
            """;

        ResponseEntity<LngLat[]> response = sendPostRequest(url, requestBody, LngLat[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void testCalcDeliveryPathAsGeoJson() {
        String url = "http://localhost:" + port + "/calcDeliveryPathAsGeoJson";

        String requestBody = """
                {
                    "orderNo": "77DAD717",
                    "orderDate": "2025-01-16",
                    "orderStatus": "VALID",
                    "orderValidationCode": "NO_ERROR",
                    "priceTotalInPence": 2400,
                    "pizzasInOrder": [
                      {
                        "name": "R3: Super Cheese",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R3: All Shrooms",
                        "priceInPence": 900
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "5223358194283703",
                      "creditCardExpiry": "09/25",
                      "cvv": "473"
                    }
                }
                """;

        ResponseEntity<String> response = sendPostRequest(url, requestBody, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_SyntaxError() {
        String url = "http://localhost:" + port + "/calcDeliveryPathAsGeoJson";

        String requestBody = """
        {
            "orderNo": "77DAD717",
            "pizzasInOrder": [
                { "name": "R3: Super Cheese", "priceInPence": 1400 }
            ]
        }
    """;

        ResponseEntity<String> response = sendPostRequest(url, requestBody, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_SemanticError() {
        String url = "http://localhost:" + port + "/calcDeliveryPathAsGeoJson";

        String requestBody = """
        {
            "orderNo": "77DAD717",
            "orderDate": "2025-01-16",
            "orderStatus": "VALID",
            "orderValidationCode": "NO_ERROR",
            "priceTotalInPence": 2400,
            "pizzasInOrder": [
                { "name": "Invalid Pizza", "priceInPence": 1400 }
            ],
            "creditCardInformation": {
                "creditCardNumber": "5223358194283703",
                "creditCardExpiry": "09/25",
                "cvv": "473"
            }
        }
    """;

        ResponseEntity<String> response = sendPostRequest(url, requestBody, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }



}
