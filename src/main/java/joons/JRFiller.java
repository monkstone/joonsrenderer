package joons;

import org.sunflow.util.FloatArray;
import org.sunflow.util.IntArray;

public class JRFiller {

    private final FloatArray vertices;
    private final IntArray triangleIndices;
    private final FloatArray spheres;
    private final FloatArray points;

    private final String fillType;
    public float[] p; //array of parameters
    public int np = 0; //number of parameters

    public JRFiller(String fillType, float... params) {
        vertices = new FloatArray();
        triangleIndices = new IntArray();
        spheres = new FloatArray();
        points = new FloatArray();

        this.fillType = fillType;
        p = params;
        np = p.length;
    }

    public String getType() {
        return fillType;
    }

    public FloatArray getVertices() {
        return vertices;
    }

    private void writeTriangleIndices() {
        for (int i = 0; i < (vertices.getSize() / 9); i++) {
            //vertices/3 = number of 3d points
            //vertices/9 = number of triangles
            triangleIndices.add(i * 3);
            triangleIndices.add(i * 3 + 1);
            triangleIndices.add(i * 3 + 2);
        }
    }

    public float[] verticesToArray() {
        return vertices.trim();
    }

    public int[] triangleIndicesToArray() {
        writeTriangleIndices();
        return triangleIndices.trim();
    }

    public void addSphere(float x, float y, float z, float r) {
        spheres.add(x);
        spheres.add(y);
        spheres.add(z);
        spheres.add(r);
    }

    public FloatArray getSpheres() {
        return spheres;
    }

    public void addPoint(float x, float y, float z) {
        points.add(x);
        points.add(y);
        points.add(z);
    }
}
