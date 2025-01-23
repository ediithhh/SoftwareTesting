package uk.ac.ed.inf.modelbased;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.constant.OrderValidationCode;
import uk.ac.ed.inf.constant.SystemConstants;
import uk.ac.ed.inf.data.*;
import uk.ac.ed.inf.external.RestaurantService;
import uk.ac.ed.inf.validation.OrderValidationImpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class OrderValidationImplTestModelBased {

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private OrderValidationImpl orderValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidOrder() {
        Order order = createValidOrder();
        when(restaurantService.fetchRestaurants()).thenReturn(new Restaurant[]{createMockRestaurant()});

        Order result = orderValidator.validateOrder(order, restaurantService.fetchRestaurants());

        assertEquals(OrderStatus.VALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.NO_ERROR, result.getOrderValidationCode());
    }

    @Test
    void testInvalidCardNumber() {
        Order order = createValidOrder();
        order.getCreditCardInformation().setCreditCardNumber("123");

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, result.getOrderValidationCode());
    }

    @Test
    void testInvalidExpiryDate() {
        Order order = createValidOrder();
        order.getCreditCardInformation().setCreditCardExpiry("01/20");

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, result.getOrderValidationCode());
    }

    @Test
    void testInvalidCVV() {
        Order order = createValidOrder();
        order.getCreditCardInformation().setCvv("1234567");

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.CVV_INVALID, result.getOrderValidationCode());
    }

    @Test
    void testEmptyOrder() {
        Order order = createValidOrder();
        order.setPizzasInOrder(new Pizza[0]); // No pizzas in order

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.EMPTY_ORDER, result.getOrderValidationCode());
    }

    @Test
    void testTooManyPizzas() {
        Order order = createValidOrder();
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("Pizza 1", 1400), new Pizza("Pizza 2", 1400),
                new Pizza("Pizza 3", 1400), new Pizza("Pizza 4", 1400),
                new Pizza("Pizza 5", 1400)
        });

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED, result.getOrderValidationCode());
    }

    @Test
    void testPizzaNotOnMenu() {
        Order order = createValidOrder();
        order.setPizzasInOrder(new Pizza[]{new Pizza("Nonexistent Pizza", 1100)});

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.PIZZA_NOT_DEFINED, result.getOrderValidationCode());
    }

    @Test
    void testRestaurantWithNoMenu() {
        Order order = createValidOrder();

        // Restaurant exists but has no pizzas on the menu
        Restaurant restaurant = new Restaurant("Mock Restaurant",
                new LngLat(-3.2025, 55.9432),
                new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY},
                new Pizza[]{} // Empty menu
        );

        Order result = orderValidator.validateOrder(order, new Restaurant[]{restaurant});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.PIZZA_NOT_DEFINED, result.getOrderValidationCode());
    }


    @Test
    void testPizzasFromMultipleRestaurants() {
        Order order = createValidOrder();
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("R2: Meat Lover", 1400),
                new Pizza("R3: Super Cheese", 1100)
        });

        Restaurant restaurant1 = new Restaurant("Restaurant1",
                new LngLat(-3.2025, 55.9432),
                new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY},
                new Pizza[]{new Pizza("R2: Meat Lover", 1400)}
        );

        Restaurant restaurant2 = new Restaurant("Restaurant2",
                new LngLat(-3.2000, 55.9420),
                new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY},
                new Pizza[]{new Pizza("R3: Super Cheese", 1100)}
        );

        Order result = orderValidator.validateOrder(order, new Restaurant[]{restaurant1, restaurant2});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS, result.getOrderValidationCode());
    }

    @Test
    void testRestaurantClosed() {
        Order order = createValidOrder();
        order.setOrderDate(LocalDate.of(2025,1,17));

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.RESTAURANT_CLOSED, result.getOrderValidationCode());
    }

    @Test
    void testIncorrectPizzaPrice() {
        Order order = createValidOrder();
        order.setPizzasInOrder(new Pizza[]{new Pizza("R2: Meat Lover", 2000)});

        order.setPizzasInOrder(new Pizza[]{
                new Pizza("R2: Meat Lover", 1500),
                new Pizza("R2: Vegan Delight", 1100)
        });
        order.setPriceTotalInPence(2700);

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.PRICE_FOR_PIZZA_INVALID, result.getOrderValidationCode());
    }

    @Test
    void testIncorrectTotalPrice() {
        Order order = createValidOrder();
        order.setPriceTotalInPence(5000);

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.TOTAL_INCORRECT, result.getOrderValidationCode());
    }

    @Test
    void testNoOrderCharge() {
        Order order = createValidOrder();
        order.setPriceTotalInPence(2500);

        Order result = orderValidator.validateOrder(order, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.TOTAL_INCORRECT, result.getOrderValidationCode());
    }

    @Test
    void testDifferentDateFormats() {
        Order order1 = createValidOrder();
        order1.getCreditCardInformation().setCreditCardExpiry("02/26");

        Order order2 = createValidOrder();
        order2.getCreditCardInformation().setCreditCardExpiry("02-2026");

        Order result1 = orderValidator.validateOrder(order1, new Restaurant[]{createMockRestaurant()});
        Order result2 = orderValidator.validateOrder(order2, new Restaurant[]{createMockRestaurant()});

        assertEquals(OrderValidationCode.NO_ERROR, result1.getOrderValidationCode());
        assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, result2.getOrderValidationCode());
    }

    private Order createValidOrder() {
        CreditCardInformation cardInfo = new CreditCardInformation("4894971265465487", "02/26", "475");
        Pizza[] pizzas = {
                new Pizza("R2: Meat Lover", 1400),
                new Pizza("R2: Vegan Delight", 1100)
        };
        int totalPrice = 1400 + 1100 + SystemConstants.ORDER_CHARGE_IN_PENCE;
        return new Order("5F891A3F", LocalDate.of(2025, 1, 16), OrderStatus.VALID, OrderValidationCode.NO_ERROR, totalPrice, pizzas, cardInfo);
    }

    private Restaurant createMockRestaurant() {
        Pizza[] menu = { new Pizza("R2: Meat Lover", 1400), new Pizza("R2: Vegan Delight", 1100) };
        DayOfWeek[] openingDays = { DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY };
        return new Restaurant("Mock Restaurant", new LngLat(-3.2025, 55.9432), openingDays, menu);
    }


}
