import processing.core.PApplet;
import joons.JoonsRenderer;

@SuppressWarnings("serial")
public class a_maintest extends PApplet {

    JoonsRenderer jr;

    //camera declarations
    float eyeX = 0;
    float eyeY = 0;
    float eyeZ = 0;
    float centerX = 0;
    float centerY = 0;
    float centerZ = -1;
    float upX = 0;
    float upY = 1;
    float upZ = 0;
    float fov = PI / 4;
    float aspect = 4 / 3f;
    float zNear = 5;
    float zFar = 10000;

    @Override
    public void settings() {
        size(800, 600, P3D);
    }

    @Override
    public void setup() {

        jr = new JoonsRenderer(this);
        jr.setSampler("ipr"); //Rendering mode, either "ipr" or "bucket".
        jr.setSizeMultiplier(1); //Set size of the .PNG file as a multiple of the Processing sketch size.
        jr.setAA(-2, 0, 1); //Set anti-aliasing, (min, max, samples). -2 < min, max < 2, samples = 1,2,3,4..
        jr.setCaustics(1); //Set caustics. 1 ~ 100. affects quality of light scattered through glass.
        //jr.setTraceDepth(1,4,4); //Set trace depth, (diffraction, reflection, refraction). Affects glass. (1,4,4) is good.
        //jr.setDOF(170, 5); //Set depth of field of camera, (focus distance, lens radius). Larger radius => more blurry.
    }

    @Override
    public void draw() {
        jr.beginRecord(); //Make sure to include methods you want rendered.
        camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        perspective(fov, aspect, zNear, zFar);

        jr.background(0, 255, 255); //background(gray), or (r, g, b), like Processing.
        jr.background("gi_instant"); //Global illumination, normal mode.
        //jr.background("gi_ambient_occlusion"); //Global illumination, ambient occlusion mode.

        pushMatrix();
        translate(0, 0, -120);
        jr.background("cornell_box", 100, 100, 100); //cornellBox(width, height, depth);
        popMatrix();

        pushMatrix();
        translate(-40, 20, -140);
        pushMatrix();
        rotateY(-PI / 8);

        //jr.fill("light"); or
        //jr.fill("light", r, g, b); or
        //jr.fill("light", r, g, b, int samples);
        jr.fill("light", 5, 5, 5);
        sphere(13);
        translate(27, 0, 0);

        //jr.fill("mirror"); or
        //jr.fill("mirror", r, g, b);    
        jr.fill("mirror", 255, 255, 255);
        sphere(13);
        translate(27, 0, 0);

        //jr.fill("diffuse"); or
        //jr.fill("diffuse", r, g, b);
        jr.fill("diffuse", 150, 255, 255);
        sphere(13);
        translate(27, 0, 0);

        //jr.fill("shiny"); or
        //jr.fill("shiny", r, g, b);  or
        //jr.fill("shiny", r, g, b, shininess);  or
        jr.fill("shiny", 150, 255, 255, 0.1f);
        sphere(13);
        translate(27, 0, 0);
        popMatrix();
        rotateY(PI / 8);
        translate(-10, -27, 30);

        //jr.fill("ambient_occlusion"); or
        //jr.fill("ambient_occlusion", bright r, bright g, bright b); or
        //jr.fill("ambient occlusion", bright r, bright g, bright b, dark r, dark g, dark b, maximum distance, int samples);
        jr.fill("ambient_occlusion", 150, 255, 255, 0, 0, 255, 50, 16);
        sphere(13);
        translate(27, 0, 0);

        //jr.fill("phong", r, g, b);
        jr.fill("phong", 150, 255, 255);
        sphere(13);
        translate(27, 0, 0);

        //jr.fill("glass", r, g, b);
        jr.fill("glass", 255, 255, 255);
        sphere(13);
        translate(27, 0, 0);

        //jr.fill("constant", r, g, b);
        jr.fill("constant", 150, 255, 255);
        sphere(13);
        popMatrix();

        jr.endRecord(); //Make sure to end record.
        jr.displayRendered(true); //Display rendered image if rendering completed, and the argument is true.
    }

    /**
     * Press 'r' key to start rendering.
     */
    @Override
    public void keyPressed() {
        if (key == 'r' || key == 'R') {
            jr.render(); 
        }
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"a_maintest"});
    }
}
