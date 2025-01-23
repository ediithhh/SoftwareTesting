package uk.ac.ed.inf.data;

/**
 * defines a point using longitude @lng and latitude @lat
 * @param lng is the longitude
 * @param lat is the latitude
 */
public record LngLat(double lng, double lat) {

    public double distanceTo(LngLat point) {
        return Math.sqrt(Math.pow(this.lng - point.lng, 2) + Math.pow(this.lat - point.lat, 2));
    }

    public boolean isCloseTo(LngLat point, double tolerance) {
        return this.distanceTo(point) < tolerance;
    }

}
