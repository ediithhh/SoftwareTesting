package uk.ac.ed.inf.data;

import uk.ac.ed.inf.data.LngLat;
import uk.ac.ed.inf.data.NamedRegion;

public class IsInRegionRequest {

    private LngLat position;
    private NamedRegion region;

    public LngLat getPosition() {
        return position;
    }

    public void setPosition(LngLat position) {
        this.position = position;
    }

    public NamedRegion getRegion() {
        return region;
    }

    public void setRegion(NamedRegion region) {
        this.region = region;
    }
}
