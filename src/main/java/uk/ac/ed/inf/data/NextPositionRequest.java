package uk.ac.ed.inf.data;

import uk.ac.ed.inf.data.LngLat;

public class NextPositionRequest {
    private LngLat start;
    private double angle;

    public LngLat getStart() {
        return start;
    }

    public void setStart(LngLat start) {
        this.start = start;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
