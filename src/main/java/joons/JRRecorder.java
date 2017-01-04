package joons;

import java.util.ArrayList;

import static joons.JRStatics.*;
import processing.core.*;
import processing.opengl.PGraphics3D;

/*
 * The purpose of JRRecorder is to geometry used in P5 sketch, and reproduce them in sunflow.
 * Remember, JRRecorder is a secondary PGraphics3D object, and methods are echoed to it by PApplet.
 * Say, when you call PApplet.method(), PApplet will do something like:
 * public void method() { primaryPG3D.method(); secondaryPG3D.method(); }
 */
public class JRRecorder extends PGraphics3D {

    private boolean writingVertices = false;
    private ArrayList<Float> tempVertices;
    private final PApplet app;
    int kind, vertCount;

    /**
     *
     * @param parent
     */
    public JRRecorder(final PApplet parent) {
        //standard construction for a PGraphics object
        app = parent;
        setParent(app);
        setPrimary(false);
        setSize(app.width, app.height);
        init();
        
    }

    private void init() {
        initializeFillers(); //emptying out fillers
        FILLERS_ARE_VALID = true; //true unless proven false		
        tempVertices = new ArrayList<>(); //emptying out vertices
        writingVertices = false; //init beginShape()
    }

    @Override
    public void beginDraw() {
        //this method is echoed before every draw() loop.
        init();
    }

    @Override
    public void endDraw() {
        //We need this empty override.
    }

    @Override
    public void perspective(float fov, float aspect, float zNear, float zFar) {
        //zNear and zFar are unused in sunflow.
        FOV = fov;
        ASPECT = aspect;
    }

    @Override
    public void beginShape() {
        PApplet.println("Joons-Renderer: Please use beginShape(TRIANGLES) or beginShape(QUADS).");
        PApplet.println("Joons-Renderer: Your vertices will be rendered using beginShape(TRIANGLES).");
        beginShape(PConstants.TRIANGLES);
    }

    @Override
    public void beginShape(int kind) {
        this.kind = kind;
        writingVertices = true;
        vertCount = 0;
    }

    @Override
    public void vertex(float x, float y, float z) {
        if (writingVertices) {
            vertCount++;
            tempVertices.add(x);
            tempVertices.add(y);
            tempVertices.add(z);

            if (kind == PConstants.QUADS && vertCount == 4) {
                //if more than 1 quad, simply start a new one.
                endShape();
                beginShape(PConstants.QUADS);
            }
        }
    }

    @Override
    public void endShape() {
        if (kind == PConstants.QUADS && vertCount == 4) {
            //adding vertices to make two triangles from four points
            //abcd -> abc dac
            tempVertices.add(tempVertices.get(0));//ax
            tempVertices.add(tempVertices.get(1));//ay
            tempVertices.add(tempVertices.get(2));//az
            tempVertices.add(tempVertices.get(6));//cx
            tempVertices.add(tempVertices.get(7));//cy
            tempVertices.add(tempVertices.get(8));//cz
        }
        //divide by 3, because 3 numbers make 1 point.
        for (int i = 0; i < tempVertices.size() / 3; i++) { 
            float[] tCoord = applyTransform(
                    app, 
                    tempVertices.get(i * 3),
                    tempVertices.get(i * 3 + 1),
                    tempVertices.get(i * 3 + 2)
            );
            getCurrentFiller().getVertices().add(tCoord[0]);
            getCurrentFiller().getVertices().add(tCoord[1]);
            getCurrentFiller().getVertices().add(tCoord[2]);
        }
        writingVertices = false;
        vertCount = 0;
        tempVertices = new ArrayList<>(); //emptying out vertices
    }

    //implementations of 3D primitives	
    @Override
    public void box(float d) {
        box(d, d, d);
    }

    @Override
    public void box(float width, float height, float depth) {
        float w = width / 2;
        float h = height / 2;
        float d = depth / 2;

        beginShape(PConstants.QUADS);// top
        vertex(w, h, d);
        vertex(-w, h, d);
        vertex(-w, -h, d);
        vertex(w, -h, d);
        endShape();

        beginShape(PConstants.QUADS);// +x side
        vertex(w, h, d);
        vertex(w, -h, d);
        vertex(w, -h, -d);
        vertex(w, h, -d);
        endShape();

        beginShape(PConstants.QUADS);// -x side
        vertex(-w, h, -d);
        vertex(-w, -h, -d);
        vertex(-w, -h, d);
        vertex(-w, h, d);
        endShape();

        beginShape(PConstants.QUADS);// +y side
        vertex(w, h, d);
        vertex(w, h, -d);
        vertex(-w, h, -d);
        vertex(-w, h, d);
        endShape();

        beginShape(PConstants.QUADS);// -y side
        vertex(-w, -h, d);
        vertex(-w, -h, -d);
        vertex(w, -h, -d);
        vertex(w, -h, d);
        endShape();

        beginShape(PConstants.QUADS);// bottom
        vertex(-w, h, -d);
        vertex(w, h, -d);
        vertex(w, -h, -d);
        vertex(-w, -h, -d);
        endShape();
    }

    @Override
    public void sphere(float r) {
        //Sunflow seems to offer an optimized render for a perfect sphere,
        //meaning no triangle polygonal mess from Processing
        float[] sph = applyTransform(app, 0, 0, 0);
        getCurrentFiller().addSphere(sph[0], sph[1], sph[2], r);
    }

}
