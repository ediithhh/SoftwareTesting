package uk.ac.ed.inf.validation;

import uk.ac.ed.inf.constant.SystemConstants;
import uk.ac.ed.inf.data.Order;
import uk.ac.ed.inf.data.Pizza;
import uk.ac.ed.inf.data.Restaurant;
import uk.ac.ed.inf.interfaces.OrderValidation;
import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.constant.OrderValidationCode;
import uk.ac.ed.inf.external.RestaurantService;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * Implementation of the OrderValidation interface.
 * Validates customer orders based on the various error codes.
 */
public class OrderValidationImpl implements OrderValidation {

    private final RestaurantService restaurantService;

    /**
     * Constructor to initialize the OrderValidation implementation.
     * @param restaurantService The service to fetch restaurant data.
     */
    public OrderValidationImpl(RestaurantService  restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * Validates an order based on various business rules.
     *
     * @param orderToValidate   The order to be validated.
     * @param definedRestaurants The list of available restaurants.
     * @return A validated order with an appropriate status and validation code.
     */
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
        Order validatedOrder = new Order(orderToValidate);

        // Validate credit card details
        if (!isValidCreditCardNumber(orderToValidate.getCreditCardInformation().getCreditCardNumber())) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.CARD_NUMBER_INVALID);
        }

        if (!isValidExpiryDate(orderToValidate.getCreditCardInformation().getCreditCardExpiry())) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.EXPIRY_DATE_INVALID);
        }

        if (!isValidCVV(orderToValidate.getCreditCardInformation().getCvv())) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.CVV_INVALID);
        }

        // Validate pizza order constraints
        if (orderToValidate.getPizzasInOrder().length == 0) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.EMPTY_ORDER);
        }

        if (orderToValidate.getPizzasInOrder().length > SystemConstants.MAX_PIZZAS_PER_ORDER) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
        }

        // Validate pizzas against restaurant menus
        if (!arePizzasDefined(orderToValidate.getPizzasInOrder(), definedRestaurants)) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.PIZZA_NOT_DEFINED);
        }

        if (!arePizzasFromSingleRestaurant(orderToValidate.getPizzasInOrder(), definedRestaurants)) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
        }

        // Validate restaurant is opened
        if (!isRestaurantOpen(orderToValidate, definedRestaurants)) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.RESTAURANT_CLOSED);
        }

        // Validate pizza pricing
        if (!isPizzaPriceValid(orderToValidate.getPizzasInOrder(), definedRestaurants)) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.PRICE_FOR_PIZZA_INVALID);
        }

        // Validate total price
        if (!isTotalPrice(orderToValidate)) {
            return setInvalidOrder(validatedOrder, OrderValidationCode.TOTAL_INCORRECT);
        }

        validatedOrder.setOrderStatus(OrderStatus.VALID);
        validatedOrder.setOrderValidationCode(OrderValidationCode.NO_ERROR);
        return validatedOrder;
    }

    /**
     * Marks an order as invalid with the appropriate validation code.
     *
     * @param order The order to be marked as invalid.
     * @param code The reason for invalidation.
     * @return The updated order.
     */
    private Order setInvalidOrder(Order order, OrderValidationCode code) {
        order.setOrderStatus(OrderStatus.INVALID);
        order.setOrderValidationCode(code);
        return order;
    }

    /**
     * Checks if the credit card number is a valid 16-digit numeric string.
     *
     * @param cardNumber The credit card number.
     * @return true if valid, false otherwise.
     */
    private boolean isValidCreditCardNumber(String cardNumber) {
        return cardNumber.matches("\\d{16}");
    }

    /**
     * Checks if the credit card expiry date is valid and not expired.
     *
     * @param expiryDate The expiry date in MM/yy format.
     * @return true if valid, false otherwise.
     */
    private boolean isValidExpiryDate(String expiryDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiry = YearMonth.parse(expiryDate, formatter);
            return expiry.isAfter(YearMonth.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Checks if the CVV is a valid 3-digit number.
     *
     * @param cvv The CVV code.
     * @return true if valid, false otherwise.
     */
    private boolean isValidCVV(String cvv) {
        return cvv.matches("\\d{3}");
    }

    /**
     * Checks that all pizzas in an order are from the same restaurant.
     *
     * @param pizzas The pizzas in the order.
     * @param restaurants The list of available restaurants.
     * @return true if all pizzas come from a single restaurant, false otherwise.
     */
    private boolean arePizzasFromSingleRestaurant(Pizza[] pizzas, Restaurant[] restaurants) {
        if (pizzas.length == 0) return false;

        // Find the restaurant that serves the first pizza
        Restaurant firstPizzaRestaurant = findRestaurantForPizza(pizzas[0], restaurants);
        if (firstPizzaRestaurant == null) {
            return false;
        }

        // Check if all pizzas belong to the same restaurant
        for (Pizza pizza : pizzas) {
            boolean foundInSameRestaurant = Arrays.stream(firstPizzaRestaurant.menu())
                    .anyMatch(menuPizza -> menuPizza.name().equals(pizza.name()));

            if (!foundInSameRestaurant) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the restaurant that serves a given pizza.
     *
     * @param pizza The pizza to find.
     * @param restaurants The list of available restaurants.
     * @return The restaurant serving the pizza, or null if not found.
     */
    private Restaurant findRestaurantForPizza(Pizza pizza, Restaurant[] restaurants) {
        for (Restaurant restaurant : restaurants) {
            boolean isPizzaFound = Arrays.stream(restaurant.menu())
                    .anyMatch(menuPizza -> menuPizza.name().equals(pizza.name()));

            if (isPizzaFound) {
                return restaurant; // Return the first matching restaurant
            }
        }
        return null;
    }

    /**
     * Checks if all pizzas in the order are defined in at least one restaurant.
     *
     * @param pizzas The pizzas to validate.
     * @param restaurants The list of available restaurants.
     * @return true if all pizzas are found in some restaurant, false otherwise.
     */
    private boolean arePizzasDefined(Pizza[] pizzas, Restaurant[] restaurants) {
        for (Pizza pizza : pizzas) {
            boolean foundInAnyRestaurant = false;
            for (Restaurant restaurant : restaurants) {
                boolean foundInMenu = Arrays.stream(restaurant.menu())
                        .anyMatch(menuPizza -> menuPizza.name().equals(pizza.name()));

                if (foundInMenu) {
                    foundInAnyRestaurant = true;
                    break;
                }
            }
            if (!foundInAnyRestaurant) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the total order price is correct.
     *
     * @param order The order to validate.
     * @return true if the total price is correct, false otherwise.
     */
    private boolean isTotalPrice(Order order) {
        int calculatedPrice =
                Arrays.stream(order.getPizzasInOrder()).mapToInt(Pizza::priceInPence).sum() + SystemConstants.ORDER_CHARGE_IN_PENCE;
        return calculatedPrice == order.getPriceTotalInPence();
    }

    /**
     * Checks if the restaurant for the order is open on the order date.
     *
     * @param order The order to validate.
     * @param restaurants The list of available restaurants.
     * @return true if the restaurant is open, false otherwise.
     */
    private boolean isRestaurantOpen(Order order, Restaurant[] restaurants) {
        if (order.getPizzasInOrder().length == 0) {
            return false;
        }

        Restaurant restaurant = findRestaurantForPizza(order.getPizzasInOrder()[0], restaurants);
        if (restaurant == null) {
            return false;
        }

        String orderDay = order.getOrderDate().getDayOfWeek().name();

        boolean isOpen = Arrays.stream(restaurant.openingDays())
                .map(String::valueOf)  // Ensure it is a String
                .map(String::trim)  // Remove extra spaces
                .anyMatch(day -> day.equalsIgnoreCase(orderDay.trim()));
        return isOpen;
    }

    /**
     * Checks that the listed pizza prices match those in the restaurant menu.
     *
     * @param pizzas The pizzas in the order.
     * @param restaurants The list of available restaurants.
     * @return true if all pizza prices match the menu, false otherwise.
     */
    private boolean isPizzaPriceValid(Pizza[] pizzas, Restaurant[] restaurants) {
        for (Pizza pizza : pizzas) {
            Restaurant restaurant = findRestaurantForPizza(pizza, restaurants);

            if (restaurant == null) {
                return false;
            }

            // Check if the pizza price matches the price in the restaurant menu
            boolean priceIsValid = Arrays.stream(restaurant.menu())
                    .anyMatch(menuPizza -> menuPizza.name().equals(pizza.name()) &&
                            menuPizza.priceInPence() == pizza.priceInPence());

            if (!priceIsValid) {
                return false;
            }
        }
        return true;
    }

}