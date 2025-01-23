package uk.ac.ed.inf.combinatorial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.data.Order;
import uk.ac.ed.inf.data.Pizza;
import uk.ac.ed.inf.data.CreditCardInformation;
import uk.ac.ed.inf.constant.OrderValidationCode;
import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.data.Restaurant;
import uk.ac.ed.inf.external.RestaurantService;
import uk.ac.ed.inf.validation.OrderValidationImpl;
import uk.ac.ed.inf.data.LngLat;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class CombinatorialTests {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private OrderValidationImpl orderValidation;

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Correctly initialize Restaurant with required fields
        testRestaurant = new Restaurant(
                "TestRestaurant",
                new LngLat(-3.186874, 55.944494), // Dummy coordinates
                new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY},
                new Pizza[]{new Pizza("TestPizza", 800)}
        );

        // Mock restaurant service behavior
        Mockito.when(restaurantService.fetchRestaurants()).thenReturn(new Restaurant[]{testRestaurant});

        orderValidation = new OrderValidationImpl(restaurantService);
    }

    /**
     * Test different combinations of valid and invalid credit card details using OrderValidationImpl.
     */
    @ParameterizedTest
    @CsvSource({
            "1232345678, 12/25, 123, CARD_NUMBER_INVALID, false",
            "12345678, 12/25, 123, CARD_NUMBER_INVALID, false",    // Invalid card number
            "1234567812345678, 13/25, 123, EXPIRY_DATE_INVALID, false", // Invalid expiry format
            "1234567812345678, 12/25, 12, CVV_INVALID, false"   // Invalid CVV
    })
    public void testCreditCardCombinations(String cardNumber, String expiry, String cvv, OrderValidationCode expectedCode, boolean expected) {
        CreditCardInformation creditCard = new CreditCardInformation(cardNumber, expiry, cvv);
        Order order = new Order("ORD007", LocalDate.now(), 900, new Pizza[]{new Pizza("TestPizza", 800)}, creditCard);
        Order validatedOrder = orderValidation.validateOrder(order, restaurantService.fetchRestaurants());

        assertEquals(expectedCode, validatedOrder.getOrderValidationCode(), "Validation code should match expected");
        assertEquals(expected, validatedOrder.getOrderStatus() == OrderStatus.VALID, "Order validation result should match expected");
    }

    /**
     * Test different pizza count variations using OrderValidationImpl.
     */
    @ParameterizedTest
    @CsvSource({
            "1, true",
            "4, true",
            "6, false",   // Exceeds max allowed
            "0, false"    // Empty order
    })
    public void testPizzaCountCombinations(int pizzaCount, boolean expected) {
        Pizza[] pizzas = new Pizza[pizzaCount];
        for (int i = 0; i < pizzaCount; i++) {
            pizzas[i] = new Pizza("TestPizza", 800);
        }
        CreditCardInformation creditCard = new CreditCardInformation("1234567812345678", "12/25", "123");
        Order order = new Order("ORD008", LocalDate.now(), pizzaCount * 800 + 100, pizzas, creditCard);

        Restaurant[] restaurants = restaurantService.fetchRestaurants();
        Order validatedOrder = orderValidation.validateOrder(order, restaurants);

        assertEquals(expected, validatedOrder.getOrderStatus() == OrderStatus.VALID, "Pizza count validation should match expected");
    }

    /**
     * Test combinations of valid and invalid order total prices.
     */
    @ParameterizedTest
    @CsvSource({
            "1700, 2, true",   // Correct price
            "1500, 2, false",  // Incorrect price
            "900, 1, true",    // Single pizza, correct price
            "2000, 2, false"   // Overpriced order
    })
    public void testTotalPriceCombinations(int totalPrice, int pizzaCount, boolean expected) {
        Pizza[] pizzas = new Pizza[pizzaCount];
        for (int i = 0; i < pizzaCount; i++) {
            pizzas[i] = new Pizza("TestPizza", 800);
        }
        CreditCardInformation creditCard = new CreditCardInformation("1234567812345678", "12/25", "123");
        Order order = new Order("ORD005", LocalDate.now(), OrderStatus.UNDEFINED, OrderValidationCode.UNDEFINED, totalPrice, pizzas, creditCard);

        boolean isValid = (pizzaCount * 800 + 100) == totalPrice;
        assertEquals(expected, isValid, "Total price validation should match expected");
    }

    /**
     * Test different combinations of order status and validation codes.
     */
    @ParameterizedTest
    @CsvSource({
            "VALID, NO_ERROR, true",
            "INVALID, EMPTY_ORDER, false",
            "INVALID, CARD_NUMBER_INVALID, false",
            "VALID, TOTAL_INCORRECT, false"
    })
    public void testOrderStatusValidation(OrderStatus status, OrderValidationCode validationCode, boolean expected) {
        Pizza[] pizzas = { new Pizza("Margherita", 800) };
        CreditCardInformation creditCard = new CreditCardInformation("1234567812345678", "12/25", "123");
        Order order = new Order("ORD006", LocalDate.now(), status, validationCode, 800, pizzas, creditCard);

        boolean isValid = status == OrderStatus.VALID && validationCode == OrderValidationCode.NO_ERROR;
        assertEquals(expected, isValid, "Order status and validation code combination should match expected");
    }
}
