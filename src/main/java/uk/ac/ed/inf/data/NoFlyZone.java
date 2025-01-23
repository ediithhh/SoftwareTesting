package uk.ac.ed.inf.data;

import java.util.List;

public class NoFlyZone {

    private String name;

    private List<LngLat> vertices;

    public NoFlyZone(String name, List<LngLat> vertices) {
        this.name = name;
        this.vertices = vertices;
    }

    public String getName() {
        return name;
    }

    public List<LngLat> getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        return "NoFlyZone{name='" + name + "', vertices=" + vertices + "}";
    }

}
