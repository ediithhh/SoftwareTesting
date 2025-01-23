package uk.ac.ed.inf.modelbased;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.Order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ed.inf.constant.OrderValidationCode.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ModelBasedTests {

    private static final Logger logger = LoggerFactory.getLogger(ModelBasedTests.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private <T> ResponseEntity<T> sendPostRequest(String url, String requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        logger.info("Sending POST request to {} with body: {}", url, requestBody);
        ResponseEntity<T> response = restTemplate.postForEntity(url, request, responseType);
        logger.info("Response from {}: Status {}, Body {}", url, response.getStatusCode(), response.getBody());
        return response;
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
    void testValidateOrder_valid() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 2600,
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
    void testValidateOrderInvalidCreditCardNumber() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 2600,
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
                      "creditCardNumber": "1234",
                      "creditCardExpiry": "02/26",
                      "cvv": "679"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                CARD_NUMBER_INVALID);
    }

    @Test
    void testValidateOrderCardExpired() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 2600,
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
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/20",
                      "cvv": "679"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                EXPIRY_DATE_INVALID);
    }

    @Test
    void testValidateOrderInvalidCVV() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 2600,
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
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/26",
                      "cvv": "6789"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                CVV_INVALID);
    }

    @Test
    void testValidateOrderTotalIncorrect() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 30000,
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
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/26",
                      "cvv": "678"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                TOTAL_INCORRECT);
    }

    @Test
    void testValidateOrderPizzaNotDefined() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 2600,
                    "pizzasInOrder": [
                      {
                        "name": "Random Pizza",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R2: Vegan Delight",
                        "priceInPence": 1100
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/26",
                      "cvv": "679"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                PIZZA_NOT_DEFINED);
    }

    @Test
    void testValidateOrderMaxPizzaCountExceeded() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 6500,
                    "pizzasInOrder": [
                      {
                        "name": "R2: Meat Lover",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R2: Vegan Delight",
                        "priceInPence": 1100
                      },
                      {
                        "name": "R2: Meat Lover",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R2: Vegan Delight",
                        "priceInPence": 1100
                      },
                      {
                        "name": "R2: Meat Lover",
                        "priceInPence": 1400
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/26",
                      "cvv": "678"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                MAX_PIZZA_COUNT_EXCEEDED);
    }

    @Test
    void testValidateOrderPizzaFromMultipleRestaurant() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 2600,
                    "pizzasInOrder": [
                      {
                        "name": "R3: Super Cheese",
                        "priceInPence": 1400
                      },
                      {
                        "name": "R2: Vegan Delight",
                        "priceInPence": 1100
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/26",
                      "cvv": "678"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                PIZZA_FROM_MULTIPLE_RESTAURANTS);
    }

    @Test
    void testValidateOrderRestaurantClosed() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-18",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 2600,
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
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/26",
                      "cvv": "678"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                RESTAURANT_CLOSED);
    }

    @Test
    void testValidateOrderPriceForPizzaInvalid() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 3200,
                    "pizzasInOrder": [
                      {
                        "name": "R2: Meat Lover",
                        "priceInPence": 2000
                      },
                      {
                        "name": "R2: Vegan Delight",
                        "priceInPence": 1100
                      }
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/26",
                      "cvv": "678"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                PRICE_FOR_PIZZA_INVALID);
    }

    @Test
    void testValidateOrderEmptyOrder() {
        String url = "http://localhost:" + port + "/validateOrder";

        String requestBody = """
                {
                    "orderNo": "11496050",
                    "orderDate": "2025-01-16",
                    "orderStatus": "UNDEFINED",
                    "orderValidationCode": "UNDEFINED",
                    "priceTotalInPence": 100,
                    "pizzasInOrder": [
                    ],
                    "creditCardInformation": {
                      "creditCardNumber": "8475829475839473",
                      "creditCardExpiry": "02/26",
                      "cvv": "678"
                    }
                }
                """;

        ResponseEntity<Order> response = sendPostRequest(url, requestBody, Order.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderValidationCode()).isEqualTo(
                EMPTY_ORDER);
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


}
