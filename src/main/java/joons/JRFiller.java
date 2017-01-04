package joons;

import java.util.ArrayList;
import java.util.List;

public class JRFiller {

    private final List<Float> vertices;
    private final List<Integer> triangleIndices;
    private final List<Float> spheres;
    private final List<Float> points;

    private final String fillType;
    public float[] p; //array of parameters
    public int np = 0; //number of parameters

    public JRFiller(String fillType, float... params) {
        vertices = new ArrayList<>();
        triangleIndices = new ArrayList<>();
        spheres = new ArrayList<>();
        points = new ArrayList<>();

        this.fillType = fillType;
        p = params;
        np = p.length;
    }

    public String getType() {
        return fillType;
    }

    public List<Float> getVertices() {
        return vertices;
    }

    private void writeTriangleIndices() {
        for (int i = 0; i < (vertices.size() / 9); i++) {
            //vertices/3 = number of 3d points
            //vertices/9 = number of triangles
            triangleIndices.add(i * 3);
            triangleIndices.add(i * 3 + 1);
            triangleIndices.add(i * 3 + 2);
        }
    }

    public float[] verticesToArray() {
        float[] v = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            v[i] = vertices.get(i);
        }
        return v;
    }

    public int[] triangleIndicesToArray() {
        writeTriangleIndices();
        int[] t = new int[triangleIndices.size()];
        for (int i = 0; i < triangleIndices.size(); i++) {
            t[i] = triangleIndices.get(i);
        }
        return t;
    }

    public void addSphere(float x, float y, float z, float r) {
        spheres.add(x);
        spheres.add(y);
        spheres.add(z);
        spheres.add(r);
    }

    public List<Float> getSpheres() {
        return spheres;
    }

    public void addPoint(float x, float y, float z) {
        points.add(x);
        points.add(y);
        points.add(z);
    }
}
