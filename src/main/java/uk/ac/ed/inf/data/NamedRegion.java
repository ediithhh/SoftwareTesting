package uk.ac.ed.inf.data;

import java.util.List;

public class NamedRegion {
    private String name;
    private List<LngLat> vertices;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LngLat> getVertices() {
        return vertices;
    }

    public void setVertices(List<LngLat> vertices) {
        this.vertices = vertices;
    }
}
