package uk.ac.ed.inf.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.inf.constant.OrderStatus;
import uk.ac.ed.inf.constant.SystemConstants;
import uk.ac.ed.inf.data.*;
import uk.ac.ed.inf.flightpath.PathfindingAlgorithm;
import uk.ac.ed.inf.interfaces.OrderValidation;
import uk.ac.ed.inf.external.CentralAreaService;
import uk.ac.ed.inf.external.NoFlyZoneService;
import uk.ac.ed.inf.external.RestaurantService;
import uk.ac.ed.inf.validation.OrderValidationImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller class that handles all drone-related operations
 */
@RestController
public class DroneController {

    private static final double tolerance = 0.00015;

    /**
     * Health check endpoint to confirm that the service is running.
     * @return true if the service is running.
     */
    @GetMapping("/isAlive")
    public boolean isAlive() {
        return true;
    }

    /**
     * Returns the UUID of the drone system.
     * @return The UUID.
     */
    @GetMapping("/uuid")
    public String getUuid() {
        return "s2277575";
    }

    /**
     * Calculates the Euclidean distance between two points.
     * @param request The JSON request containing two positions.
     * @return The calculated distance or a BAD REQUEST if invalid input.
     */
    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody LngLatPairRequest request) {
        try {
            LngLat position1 = request.getPosition1();
            LngLat position2 = request.getPosition2();

            if (!isValidCoordinate(position1) || !isValidCoordinate(position2)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            double distance =
                    Math.sqrt(Math.pow(position1.lng() - position2.lng(), 2) + Math.pow(position1.lat() - position2.lat(), 2));

            return ResponseEntity.ok(distance);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Validates if a given coordinate is within the valid longitude and latitude bounds.
     * @param position The coordinate to validate.
     * @return true if the coordinate is within valid bounds, false otherwise.
     */
    private boolean isValidCoordinate(LngLat position) {
        return position.lng() >= -180 && position.lng() <= 180 && position.lat() >= -90 && position.lat() <= 90;
    }

    /**
     * Determines if two positions are close with a defined tolerance.
     * @param request The JSON request containing two positions.
     * @return true if the positions are close, false otherwise.
     */
    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody LngLatPairRequest request) {
        try {
            LngLat position1 = request.getPosition1();
            LngLat position2 = request.getPosition2();

            if (!isValidCoordinate(position1) || !isValidCoordinate(position2)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            double distance = position1.distanceTo(position2);
            boolean isCloseTo = distance < tolerance;

            return ResponseEntity.ok(isCloseTo);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Calculates the next position of a drone given a starting position and angle.
     * @param request The JSON request containing the starting position and angle.
     * @return The next position or a BAD REQUEST if input is invalid.
     */
    @PostMapping("/nextPosition")
    public ResponseEntity<LngLat> nextPosition(@RequestBody NextPositionRequest request) {
        try {
            LngLat start = request.getStart();
            double angle = request.getAngle();

            if (angle == 999) {
                return ResponseEntity.ok(start);
            }

            if (angle < 0 || angle > 360) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            double angleInRadians = Math.toRadians(angle);
            double deltaLng = 0.00015 * Math.cos(angleInRadians);
            double deltaLat = 0.00015 * Math.sin(angleInRadians);

            LngLat nextPosition = new LngLat(start.lng() + deltaLng, start.lat() + deltaLat);

            return ResponseEntity.ok(nextPosition);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Endpoint to check whether a given point is inside a specified region.
     * @param request The JSON request containing the position and region details.
     * @return ResponseEntity<Boolean> - true if the position is inside the region,
     *         false if not, and BAD_REQUEST if the input is invalid.
     */
    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody IsInRegionRequest request) {
        try {
            LngLat position = request.getPosition();
            NamedRegion region = request.getRegion();

            // Validate the region is closed
            if (!isRegionClosed(region.getVertices())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            boolean isInRegion = isPointInPolygon(position, region.getVertices());

            return ResponseEntity.ok(isInRegion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Validates whether a given list of vertices forms a closed polygon.
     * A polygon is considered closed if it has at least 3 vertices and
     * the first and last vertex are the same.
     * @param vertices The list of LngLat coordinates representing the region.
     * @return true if the polygon is closed, false otherwise.
     */
    private boolean isRegionClosed(List<LngLat> vertices) {
        if (vertices.size() < 3) {
            return false;
        }
        LngLat first = vertices.get(0);
        LngLat last = vertices.get(vertices.size() - 1);
        return first.lng() == last.lng() && first.lat() == last.lat();
    }

    /**
     * Determines if a given point is inside a polygon using the ray-casting algorithm.
     * @param point The point to check.
     * @param vertices The list of polygon vertices.
     * @return true if the point is inside the polygon, false otherwise.
     */
    private boolean isPointInPolygon(LngLat point, List<LngLat> vertices) {
        int numVertices = vertices.size();
        boolean isInside = false;

        for (int i = 0, j = numVertices - 1; i < numVertices; j = i++) {
            LngLat vertex1 = vertices.get(i);
            LngLat vertex2 = vertices.get(j);

            // Check if the point is on the edge
            if (isPointOnEdge(point, vertex1, vertex2)) {
                return true;
            }

            // Ray-casting algorithm
            if ((vertex1.lat() > point.lat()) != (vertex2.lat() > point.lat())) {
                double intersectLng = (vertex2.lng() - vertex1.lng()) * (point.lat() - vertex1.lat()) /
                        (vertex2.lat() - vertex1.lat()) + vertex1.lng();

                if (point.lng() < intersectLng) {
                    isInside = !isInside;
                }
            }
        }

        return isInside;
    }

    /**
     * Determines if a given point lies exactly on the edge of a line segment.
     * @param point The point to check.
     * @param vertex1 The start point of the segment.
     * @param vertex2 The end point of the segment.
     * @return true if the point is on the edge, false otherwise.
     */
    private boolean isPointOnEdge(LngLat point, LngLat vertex1, LngLat vertex2) {

        if (vertex1.lng() == vertex2.lng() && vertex1.lat() == vertex2.lat()) {
            return false;
        }

        // Use cross product to check if the point is collinear with the edge
        double crossProduct = (point.lat() - vertex1.lat()) * (vertex2.lng() - vertex1.lng()) -
                (point.lng() - vertex1.lng()) * (vertex2.lat() - vertex1.lat());

        if (Math.abs(crossProduct) > 1e-12) {
            return false;   // Not collinear
        }

        // Check if the point lies within the bounds of the edge
        double dotProduct = (point.lng() - vertex1.lng()) * (vertex2.lng() - vertex1.lng()) +
                (point.lat() - vertex1.lat()) * (vertex2.lat() - vertex1.lat());

        if (dotProduct < 0) {
            return false;
        }

        double squaredLength = (vertex2.lng() - vertex1.lng()) * (vertex2.lng() - vertex1.lng()) +
                (vertex2.lat() - vertex1.lat()) * (vertex2.lat() - vertex1.lat());

        return dotProduct <= squaredLength;
    }

    private final OrderValidation orderValidator;
    private final RestaurantService restaurantService;
    private final NoFlyZoneService noFlyZoneService;
    private final CentralAreaService centralAreaService;

    /**
     * Constructor to initialize dependencies.
     * @param restaurantService Service for fetching restaurant data.
     * @param noFlyZoneService Service for fetching no-fly zones.
     * @param centralAreaService Service for fetching the central area boundary.
     */
    public DroneController(RestaurantService restaurantService, NoFlyZoneService noFlyZoneService, CentralAreaService centralAreaService) {
        this.restaurantService = restaurantService;
        this.orderValidator = new OrderValidationImpl(restaurantService);
        this.noFlyZoneService = noFlyZoneService;
        this.centralAreaService = centralAreaService;
    }

    /**
     * Validates an order before processing it.
     * @param order The order to be validated.
     * @return The validated order or a BAD REQUEST if validation fails.
     */
    @PostMapping("/validateOrder")
    public ResponseEntity<Order> validateOrder(@RequestBody Order order) {

        try {
            Restaurant[] restaurants = restaurantService.fetchRestaurants();
            Order validatedOrder = orderValidator.validateOrder(order, restaurants);

            return ResponseEntity.ok(validatedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Calculates the delivery path from the restaurant to Appleton Tower while avoiding no-fly zones.
     * @param order The validated order.
     * @return The calculated path or a BAD REQUEST if the order is invalid.
     */
    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<List<LngLat>> calcDeliveryPath(@RequestBody Order order) {

        try{

            // Check for syntax errors and missing fields
            if (order == null ||
                    order.getOrderNo() == null ||
                    order.getOrderDate() == null ||
                    order.getOrderStatus() == null ||
                    order.getPizzasInOrder() == null ||
                    order.getPizzasInOrder().length == 0 ||
                    order.getCreditCardInformation() == null ||
                    order.getCreditCardInformation().getCreditCardNumber() == null ||
                    order.getCreditCardInformation().getCreditCardExpiry() == null ||
                    order.getCreditCardInformation().getCvv() == null) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Validate the order
            Restaurant[] definedRestaurants = restaurantService.fetchRestaurants();
            Order validatedOrder = orderValidator.validateOrder(order, definedRestaurants);

            if (validatedOrder.getOrderStatus() != OrderStatus.VALID) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Fetch data
            List<NoFlyZone> noFlyZones = noFlyZoneService.fetchNoFlyZones();
            List<LngLat> centralArea = centralAreaService.fetchCentralArea();

            // Find the restaurant for the order
            Restaurant restaurant = findRestaurantForOrder(order, definedRestaurants);
            if (restaurant == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Calculate path
            LngLat appletonTower = new LngLat(SystemConstants.APPLETON_LNG, SystemConstants.APPLETON_LAT);
            LngLat restaurantLocation = restaurant.location();
            List<LngLat> pathToAppleton = PathfindingAlgorithm.findPath(restaurantLocation, appletonTower, noFlyZones, centralArea);

            if (pathToAppleton == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            return ResponseEntity.ok(pathToAppleton);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    private Restaurant findRestaurantForOrder(Order order,Restaurant[] restaurants) {
        for (Restaurant restaurant : restaurants) {
            for (Pizza menuItem : restaurant.menu()) {
                if (Arrays.stream(order.getPizzasInOrder()).anyMatch(pizza -> pizza.name().equals(menuItem.name()))) {
                    return restaurant;
                }
            }
        }
        return null;
    }

    /**
     * Converts a drone delivery path into GeoJSON format for visualization.
     * @param order The validated order.
     * @return The delivery path in GeoJSON format or a BAD REQUEST if an error occurs.
     */
    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(@RequestBody Order order) {

        try {
            List<LngLat> fullPath = calcDeliveryPath(order).getBody();

            if (fullPath == null || fullPath.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Remove hovering steps (filter out duplicate points)
            List<LngLat> simplifiedPath = removeHoveringSteps(fullPath);

            // Convert to GeoJSON format
            String geoJson = convertToGeoJson(simplifiedPath);

            return ResponseEntity.ok(geoJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private List<LngLat> removeHoveringSteps (List<LngLat> path) {
        List<LngLat> simplifiedPath = new ArrayList<>();
        LngLat prev = null;

        for (LngLat  point : path) {
            if (prev == null || !point.equals(prev)) {
                simplifiedPath.add(point);
            }
            prev = point;
        }
        return simplifiedPath;
    }

    private String convertToGeoJson(List<LngLat> path) {
        StringBuilder geoJson = new StringBuilder("""
                {
                    "type": "FeatureCollection",
                    "features": [
                        {
                            "type": "Feature",
                            "geometry": {
                                "type": "LineString",
                                "coordinates": [
                """);

        for (int i =  0; i < path.size(); i++) {
            LngLat point = path.get(i);
            geoJson.append(String.format(" [%f, %f]", point.lng(), point.lat()));
            if (i < path.size() - 1) {
                geoJson.append(",");
            }
        }

        geoJson.append("""
                                    ]
                                },
                            "properties": {}
                        }
                    ]
                }
                """);

    return geoJson.toString();
    }

}
