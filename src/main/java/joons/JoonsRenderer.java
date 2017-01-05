package joons;

import java.util.List;

import org.sunflow.SunflowAPI;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

import processing.core.*;
import processing.opengl.PGraphicsOpenGL;
import static joons.JRStatics.*;

public class JoonsRenderer {

    public JRRecorder recorder;
    private SunflowAPI api;
    private boolean renderIsAGo;
    private boolean rendering = false;
    private boolean rendered = false;
    private final PApplet app;

    public JoonsRenderer(PApplet parent) {
        PGraphicsOpenGL pg = (PGraphicsOpenGL) parent.g;
        FOV = pg.cameraFOV; //default value from Processing
        ASPECT = pg.cameraAspect; //default value from Processing
        recorder = new JRRecorder(parent);
        this.app = parent;
    }

    public boolean isRendering() {
        return rendering;
    }

    public boolean isRendered() {
        return rendered;
    }

    //rendering command interface
    public void beginRecord() {
        if (rendering) {
            app.beginRecord(recorder);
        }
    }

    public void endRecord() {
        if (rendering) {
            app.endRecord();
            rendering = false;
            rendered = renderSunflow();
        }
    }

    public void render() {
        rendering = true;
        rendered = false;
    }

    //image settings interface
    public void setSizeMultiplier(double multiplier) {
        SIZE_MULTIPLIER = multiplier;
    }

    public void setSampler(String sampler) {
        SAMPLER = sampler;
    }

    public void setAA(int aaMin, int aaMax) {
        AA_MIN = aaMin;
        AA_MAX = aaMax;
    }

    public void setAA(int aaMin, int aaMax, int aaSamples) {
        setAA(aaMin, aaMax);
        AA_SAMPLES = aaSamples;
    }

    public void setCaustics(int emitInMillions) {
        setCaustics(emitInMillions, 50 + 10 * emitInMillions - 5, 0.5f); //rule of thumb
    }

    public void setCaustics(int emitInMillions, int gather) {
        setCaustics(emitInMillions, gather, 0.5f); //rule of thumb
    }

    public void setCaustics(int emitInMillions, int gather, float radius) {
        CAUSTICS_EMIT = emitInMillions * 1000000; // just to make life easier.
        CAUSTICS_GATHER = gather;
        CAUSTICS_RADIUS = radius;
    }

    public void setTraceDepths(int diff, int refl, int refr) {
        TRACE_DEPTH_DIFF = diff;
        TRACE_DEPTH_REFL = refl;
        TRACE_DEPTH_REFR = refr;
    }

    public void setDOF(float focalDistance, float lensRadius) {
        FOCAL_DISTANCE = focalDistance;
        LENS_RADIUS = lensRadius; //larger the R, say 5, greater the DOF effect.
    }

    //background interface
    public void background(float gray) {
        BG_R = gray / 255f;
        BG_G = gray / 255f;
        BG_B = gray / 255f;
        if (!rendering) {
            app.background(gray);
        }
    }

    public void background(float r, float g, float b) {
        BG_R = r / 255f;
        BG_G = g / 255f;
        BG_B = b / 255f;
        if (!rendering) {
            app.background(r, g, b);
        }
    }

    public void background(String type) {
        background(type, null);
    }

    public void background(String type, float... params) {

        //gi, instant
        if (type == null ? GI_INSTANT == null : type.equals(GI_INSTANT)) {
            if (params == null) {
                GI_IS_CALLED = true;
            } else if (params.length == 4) {
                GI_IS_CALLED = true;
                GI_INSTANT_SAMPLES = (int) params[0];
                GI_INSTANT_SETS = (int) params[1];
                GI_INSTANT_C = params[2];
                GI_INSTANT_BIAS_SAMPLES = (int) params[3];
            } else {
                if (rendering) {
                    PApplet.println(GI_INSTANT_ERROR);
                }
            }
        }

        //gi, ambient occlusion
        if (type == null ? GI_AMB_OCC == null : type.equals(GI_AMB_OCC)) {
            if (params == null) {
                GI_AMB_OCC_IS_CALLED = true;
            } else if (params.length == 8) {
                GI_AMB_OCC_BRIGHT_R = params[0] / 255f;
                GI_AMB_OCC_BRIGHT_G = params[1] / 255f;
                GI_AMB_OCC_BRIGHT_B = params[2] / 255f;
                GI_AMB_OCC_DARK_R = params[3] / 255f;
                GI_AMB_OCC_DARK_G = params[4] / 255f;
                GI_AMB_OCC_DARK_B = params[5] / 255f;
                GI_AMB_OCC_MAX_DIST = params[6];
                GI_AMB_OCC_SAMPLES = (int) params[7];
            } else {
                if (rendering) {
                    PApplet.println(GI_AMB_OCC_ERROR);
                }
            }
        }

        //cornell box
        if (type == null ? CORNELL_BOX == null : type.equals(CORNELL_BOX)) {
            if (params != null) {
                switch (params.length) {
                    case 3:
                        cornellBox(params[0], params[1], params[2]);
                        break;
                    case 7:
                        cornellBox(params[0], params[1], params[2], params[3], params[4], params[5], (int) params[6]);
                        break;
                    case 22:
                        cornellBox(params[0], params[1], params[2], params[3], params[4], params[5], (int) params[6],
                                params[7], params[8], params[9], params[10], params[11], params[12],
                                params[13], params[14], params[15], params[16], params[17], params[18],
                                params[19], params[20], params[21]);
                        break;
                    default:
                        if (rendering) {
                            PApplet.println(CORNELL_BOX_ERROR);
                        }
                        break;
                }
            } else {
                if (rendering) {
                    PApplet.println(CORNELL_BOX_ERROR);
                }
            }
        }
    }

    //shade interface
    public void fill(String type) {
        if (type == null ? LIGHT == null : type.equals(LIGHT)) {
            fill(type, DEF_RADIANCE, DEF_RADIANCE, DEF_RADIANCE);
        } else {
            fill(type, DEF_RGB, DEF_RGB, DEF_RGB);
        }
    }

    public void fill(String type, float... params) {
        switch(type){
        case(CONSTANT):
        case(DIFFUSE):
        case(SHINY):
        case(PHONG):
        case(AMBIENT_OCCLUSION):
        case(LIGHT):
        case(MIRROR):
            fillers.add(new JRFiller(type, params));
            break;
        default:
            if (rendering) {
                PApplet.println(FILLER_UNKOWN_ERROR);
            }
            FILLERS_ARE_VALID = false;
            break;
        }
        //First three parameters are always used as RGB.
        //This can be used to give an idea about what the render may look like before render.
        //When showing light shaded objects in processing,
        //the r, g, b is normalized so that the highest value of them
        //is set to 255, and the rest is normalized in proportion.
        if (params.length >= 3) {
            if (null == type) {
                app.fill(params[0], params[1], params[2]);
            } else {
                switch (type) {
                    case LIGHT:
                        float r,
                         g,
                         b,
                         max;
                        r = params[0];
                        g = params[1];
                        b = params[2];
                        max = PApplet.max(new float[]{r, g, b});
                        app.fill(255 * r / max, 255 * g / max, 255 * b / max);
                        break;
                    case GLASS:
                        app.fill(params[0], params[1], params[2], DEF_GLASS_ALPHA);
                        break;
                    default:
                        app.fill(params[0], params[1], params[2]);
                        break;
                }
            }
        }
    }

    /**
     * Adds a Sunsky light to the scene with default sun, up, and east
     * directions. The Sunsky light will make no attempt at creating a simulated
     * light in your Processing render.
     *
     * @param extendSkyBeyondHorizon Choose whether to have the sky extend
     * beyond the horizon. If false, a horizon plane will exist at the scene
     * origin.
     */
    public void sunsky(boolean extendSkyBeyondHorizon) {
        sunsky(
                extendSkyBeyondHorizon,
                DEF_SUNSKY_DIR[0], DEF_SUNSKY_DIR[1], DEF_SUNSKY_DIR[2],
                DEF_SUNSKY_SAMPLES
        );
    }

    /**
     * Adds a Sunsky light to the scene with default up and east directions. The
     * Sunsky light will make no attempt at creating a simulated light in your
     * Processing render.
     *
     * @param extendSkyBeyondHorizon Choose whether to have the sky extend
     * beyond the horizon. If false, a horizon plane will exist at the scene
     * origin.
     * @param dirX X component of the directional position of the sun in the sky
     * (0.f .. 1.0f)
     * @param dirY X component of the directional position of the sun in the sky
     * (0.f .. 1.0f)
     * @param dirZ X component of the directional position of the sun in the sky
     * (0.f .. 1.0f)
     * @param samples Number of samples to use when rendering the sunsky light
     */
    public void sunsky(boolean extendSkyBeyondHorizon, float dirX, float dirY, float dirZ, int samples) {
        sunsky(
                extendSkyBeyondHorizon,
                dirX, dirY, dirZ,
                samples,
                DEF_SUNSKY_UP[0], DEF_SUNSKY_UP[1], DEF_SUNSKY_UP[2],
                DEF_SUNSKY_EAST[0], DEF_SUNSKY_EAST[1], DEF_SUNSKY_EAST[2]
        );
    }

    /**
     * The Sunsky light will make no attempt at creating a simulated light in
     * your processing render.
     *
     * @param extendSkyBeyondHorizon Choose whether to have the sky extend
     * beyond the horizon. If false, a horizon plane will exist at the scene
     * origin.
     * @param dirX X component of the directional position of the sun in the sky
     * (0.f .. 1.0f)
     * @param dirY X component of the directional position of the sun in the sky
     * (0.f .. 1.0f)
     * @param dirZ X component of the directional position of the sun in the sky
     * (0.f .. 1.0f)
     * @param samples Number of samples to use when rendering the sunsky light
     * @param upX X component of the up direction within the scene (0.f .. 1.0f)
     * @param upY Y component of the up direction within the scene (0.f .. 1.0f)
     * @param upZ Z component of the up direction within the scene (0.f .. 1.0f)
     * @param eastX X component of the east direction within the scene (0.f ..
     * 1.0f)
     * @param eastY Y component of the east direction within the scene (0.f ..
     * 1.0f)
     * @param eastZ Z component of the east direction within the scene (0.f ..
     * 1.0f)
     */
    public void sunsky(boolean extendSkyBeyondHorizon, float dirX, float dirY, float dirZ, int samples, float upX, float upY, float upZ, float eastX, float eastY, float eastZ) {
        float[] params = new float[]{extendSkyBeyondHorizon ? 1 : 0, dirX, dirY, dirZ, samples, upX, upY, upZ, eastX, eastY, eastZ};
        JRFiller filler = new JRFiller(SUNSKY, params);
        fillers.add(filler);
    }

    //cornell box implementation
    private void cornellBox(float width, float height, float depth) {
        cornellBox(width, height, depth, DEF_CORB_RADIANCE, DEF_CORB_RADIANCE, DEF_CORB_RADIANCE, DEF_SAMPLES);
    }

    private void cornellBox(float width, float height, float depth,
            float radianceR, float radianceG, float radianceB, int samples) {
        cornellBox(width, height, depth,
                radianceR, radianceG, radianceB, samples,
                DEF_CORB_COLOR_1, DEF_CORB_COLOR_2, DEF_CORB_COLOR_2, DEF_CORB_COLOR_2, DEF_CORB_COLOR_2, DEF_CORB_COLOR_1,
                DEF_CORB_COLOR_1, DEF_CORB_COLOR_1, DEF_CORB_COLOR_1, DEF_CORB_COLOR_1, DEF_CORB_COLOR_1, DEF_CORB_COLOR_1,
                DEF_CORB_COLOR_1, DEF_CORB_COLOR_1, DEF_CORB_COLOR_1); //default vaules
    }

    private void cornellBox(float width, float height, float depth,
            float radianceR, float radianceG, float radianceB, int samples,
            float leftR, float leftG, float leftB, float rightR, float rightG, float rightB,
            float backR, float backG, float backB, float topR, float topG, float topB,
            float bottomR, float bottomG, float bottomB) {
        float w = width / 2;
        float h = height / 2;
        float d = depth / 2;

        //back up current filler
        String tempFillerType = getCurrentFiller().getType();
        float[] tempParams = getCurrentFiller().p;

        //-x side
        this.fill("diffuse", leftR, leftG, leftB);
        app.beginShape(PApplet.QUADS);
        app.vertex(-w, h, -d);
        app.vertex(-w, h, d);
        app.vertex(-w, -h, d);
        app.vertex(-w, -h, -d);
        app.endShape();

        //+x side
        this.fill("diffuse", rightR, rightG, rightB);
        app.beginShape(PApplet.QUADS);
        app.vertex(w, h, -d);
        app.vertex(w, h, d);
        app.vertex(w, -h, d);
        app.vertex(w, -h, -d);
        app.endShape();

        //back
        this.fill("diffuse", backR, backG, backB);
        app.beginShape(PApplet.QUADS);
        app.vertex(w, h, -d);
        app.vertex(w, -h, -d);
        app.vertex(-w, -h, -d);
        app.vertex(-w, h, -d);
        app.endShape();

        //bottom
        this.fill("diffuse", bottomR, bottomG, bottomB);
        app.beginShape(PApplet.QUADS);
        app.vertex(w, h, -d);
        app.vertex(w, h, d);
        app.vertex(-w, h, d);
        app.vertex(-w, h, -d);
        app.endShape();

        //ceiling rim
        this.fill("diffuse", topR, topG, topB);
        app.beginShape(PApplet.QUADS);
        app.vertex(w, -h, d);
        app.vertex(w, -h, -d);
        app.vertex(w / 3f, -h, -d / 3f);
        app.vertex(w / 3f, -h, d / 3f);

        app.vertex(w, -h, -d);
        app.vertex(-w, -h, -d);
        app.vertex(-w / 3f, -h, -d / 3f);
        app.vertex(w / 3f, -h, -d / 3f);

        app.vertex(-w, -h, -d);
        app.vertex(-w, -h, d);
        app.vertex(-w / 3f, -h, d / 3f);
        app.vertex(-w / 3f, -h, -d / 3f);

        app.vertex(-w, -h, d);
        app.vertex(w, -h, d);
        app.vertex(w / 3f, -h, d / 3f);
        app.vertex(-w / 3f, -h, d / 3f);
        app.endShape();

        //ceiling light
        this.fill("light", radianceR, radianceG, radianceB, samples);
        app.beginShape(PApplet.QUADS);
        app.vertex(w / 3f, -h, d / 3f);
        app.vertex(w / 3f, -h, -d / 3f);
        app.vertex(-w / 3f, -h, -d / 3f);
        app.vertex(-w / 3f, -h, d / 3f);
        app.endShape();

        //restore previous fill
        this.fill(tempFillerType, tempParams);
    }

    //rendering inteface
    private boolean renderSunflow() {
        PApplet.println(JR_VERSION_PRINT);
        checkSettings();
        if (renderIsAGo) {
            //saves processing image to sketch folder
            app.saveFrame(UNRENDERED_FILE_NAME);

            //create & build sunflow renderer api
            createSunflowRenderer();
            if (buildSunflowRenderer()) {

                //render using the created & built api
                JRImagePanel imagePanel = new JRImagePanel();
                api.render(SunflowAPI.DEFAULT_OPTIONS, imagePanel);
                IMG_RENDERED = imagePanel.getInversedImage();
                IMG_RENDERED.save(app.sketchPath(RENDERED_INV_FILE_NAME));
                return true;
            }
        }
        return false;
    }

    private void checkSettings() {
        renderIsAGo = true;

        if (!FILLERS_ARE_VALID) {
            renderIsAGo = false;
        }

        if (!SAMPLER.equals(IPR) && !SAMPLER.equals(BUCKET)) {
            PApplet.println(IMAGE_SAMPLER_ERROR);
            renderIsAGo = false;
        }
        if (AA_MIN > 2 || AA_MIN < -2 || AA_MAX > 2 || AA_MAX < -2 || AA_MIN > AA_MAX) {
            PApplet.println(IMAGE_AA_ERROR);
            renderIsAGo = false;
        }
    }

    private void createSunflowRenderer() {
        //compiling sunflow api
        StringBuilder template = new StringBuilder();
        template.append("import org.sunflow.core.*;\n");
        template.append("import org.sunflow.core.accel.*;\n");
        template.append("import org.sunflow.core.camera.*;\n");
        template.append("import org.sunflow.core.primitive.*;\n");
        template.append("import org.sunflow.core.shader.*;\n");
        template.append("import org.sunflow.image.Color;\n");
        template.append("import org.sunflow.math.*;\n\n");
        template.append("public void build() {\n");
        template.append("}\n");
        String buildTemplate = template.toString();
        api = SunflowAPI.compile(buildTemplate);
    }

    private boolean buildSunflowRenderer() {
        //image settings
        api.parameter("resolutionX", (int) (app.width * SIZE_MULTIPLIER));
        api.parameter("resolutionY", (int) (app.height * SIZE_MULTIPLIER));
        api.parameter("sampler", SAMPLER);
        api.parameter("aa.min", AA_MIN);
        api.parameter("aa.max", AA_MAX);
        api.parameter("aa.samples", AA_SAMPLES);
        api.parameter("filter", "gaussian");
        api.options(SunflowAPI.DEFAULT_OPTIONS);

        //camera block
        //common settings
        //default setting for camera after viewModel transformation.
        api.parameter("transform", Matrix4.lookAt(new Point3(0, 0, 0),
                new Point3(0, 0, -1),
                new Vector3(0, 1, 0)));

        //compensating for the different ways Processing and Sunflow implement FOV.
        //Processing has mid-plane vertical FOV, whereas Sunflow has mid-plane horizontal FOV.
        float fovSunflow = 2 * PApplet.atan(PApplet.tan(FOV / 2f) * ASPECT) * 360 / (2 * PApplet.PI);
        api.parameter("fov", fovSunflow);
        api.parameter("aspect", ASPECT);

        //individual camera block
        if (FOCAL_DISTANCE == -1) {
            //pinhole camera
            api.camera("Camera_0", "pinhole");
            api.parameter("camera", "Camera_0");
            api.options(SunflowAPI.DEFAULT_OPTIONS);

        } else {
            //thin lens camera
            api.parameter("focus.distance", FOCAL_DISTANCE);
            api.parameter("lens.radius", LENS_RADIUS);
            api.camera("Camera_0", "thinlens");
            api.parameter("camera", "Camera_0");
            api.options(SunflowAPI.DEFAULT_OPTIONS);
        }

        //caustics block
        api.parameter("caustics.emit", CAUSTICS_EMIT);
        api.parameter("caustics", "kd");
        api.parameter("caustics.gather", CAUSTICS_GATHER);
        api.parameter("caustics.radius", CAUSTICS_RADIUS);
        api.options(SunflowAPI.DEFAULT_OPTIONS);

        //trace depth block
        api.parameter("depths.diffuse", TRACE_DEPTH_DIFF);
        api.parameter("depths.reflection", TRACE_DEPTH_REFL);
        api.parameter("depths.refraction", TRACE_DEPTH_REFR);
        api.options(SunflowAPI.DEFAULT_OPTIONS);

        //global illumination block
        //gi, ambient occlusion
        if (GI_AMB_OCC_IS_CALLED) {
            api.parameter("gi.engine", "ambocc");
            api.parameter("gi.ambocc.bright", null, new float[]{GI_AMB_OCC_BRIGHT_R, GI_AMB_OCC_BRIGHT_G, GI_AMB_OCC_BRIGHT_B});
            api.parameter("gi.ambocc.dark", null, new float[]{GI_AMB_OCC_DARK_R, GI_AMB_OCC_DARK_G, GI_AMB_OCC_DARK_B});
            api.parameter("gi.ambocc.samples", GI_AMB_OCC_SAMPLES);
            api.parameter("gi.ambocc.maxdist", GI_AMB_OCC_MAX_DIST);
            api.options(SunflowAPI.DEFAULT_OPTIONS);

        } else if (!GI_AMB_OCC_IS_CALLED && GI_IS_CALLED) { //gi, instant
            api.parameter("gi.engine", "igi");
            api.parameter("gi.igi.samples", GI_INSTANT_SAMPLES);
            api.parameter("gi.igi.sets", GI_INSTANT_SETS);
            api.parameter("gi.igi.c", GI_INSTANT_C);
            api.parameter("gi.igi.bias_samples", GI_INSTANT_BIAS_SAMPLES);
            api.options(SunflowAPI.DEFAULT_OPTIONS);

        } else { //no gi
            api.parameter("gi.engine", "none");
            api.options(SunflowAPI.DEFAULT_OPTIONS);
        }

        //light block
        int numLightsInScene = 0;
        for (int i = 0; i < fillers.size(); i++) {
            JRFiller temp = fillers.get(i);
            if (temp.getType() == null ? LIGHT == null : temp.getType().equals(LIGHT)) {
                if (!buildLight(temp, i)) {
                    return false;
                } else {
                    numLightsInScene++;
                }
            }
        }

        // Sunsky light block.
        for (int i = 0; i < fillers.size(); i++) {
            JRFiller filler = fillers.get(i);
            if (filler.getType() == null ? SUNSKY == null : filler.getType().equals(SUNSKY)) {
                // Now, we counted the lights in the previous loop becuase
                // the Sunsky light in Sunflow doesn't emit photons and we
                // must have at least ONE photon emitting light in the scene
                // or the scene will crash. We can also only have one Sunsky
                // in our scene, so we'll keep the same light name, which
                // the Sunflow API will just overwrite if there are subsequent
                // calls to creating a Sunsky light.
                if (numLightsInScene == 0) {
                    // We need to generate at least one light, so let's just create one
                    // that _should_ be harmless in the scene somewhere. We're going to
                    // Create a point light somewhere far away.
                    /*
               * Parameters:
               * "center": Point
               * "power": Color
                     */
                    api.parameter("center", new Point3(-500, 0, -500));
                    api.parameter("power", "sRGB linear", 0.01f, 0.01f, 0.01f);
                    api.light("defaultPointLight", "point");
                }
                /*
            * Parameter list:
            * [0] extendSkyBeyondHorizong
            * [1] dirX
            * [2] dirY
            * [3] dirZ
            * [4] samples
            * [5] upX
            * [6] upY
            * [7] upZ
            * [8] eastX
            * [9] eastY
            * [10] eastZ
                 */
                api.parameter("ground.extendsky", filler.p[0] == 0);
                api.parameter("up", new Vector3(filler.p[5], filler.p[6], filler.p[7]));
                api.parameter("east", new Vector3(filler.p[8], filler.p[9], filler.p[10]));
                api.parameter("samples", (int) filler.p[4]);
                api.parameter("sundir", new Vector3(filler.p[1], filler.p[2], filler.p[3]));
                api.parameter("turbidity", DEF_SUNSKY_TURBIDITY);
                api.light("sunsky_" + i, SUNSKY);
            }
        }

        //shader block
        for (int i = 0; i < fillers.size(); i++) {
            JRFiller temp = fillers.get(i);
            if (temp.getType() == null ? LIGHT != null : !temp.getType().equals(LIGHT)) {
                if (!buildFiller(temp, i)) {
                    return false;
                }
            }
        }

        //instance block
        for (int i = 0; i < fillers.size(); i++) {
            JRFiller temp = fillers.get(i);
            if (temp.getType() == null ? LIGHT != null : !temp.getType().equals(LIGHT)) {
                buildInstance(temp, i); //should have the same condition as shader block
            }
        }

        //background primitive
        api.parameter("color", null, new float[]{BG_R, BG_G, BG_B});
        api.shader("bg.shader", "constant");
        api.geometry("bg", "background");
        api.parameter("shaders", "bg.shader");
        api.instance("bg.instance", "bg");

        return true;
    }

    private boolean buildLight(JRFiller temp, int i) {
        //light mesh
        if (temp.np == 3 || temp.np == 4) {
            api.parameter("radiance", null, new float[]{temp.p[0], temp.p[1], temp.p[2]});
            if (temp.np == 4) {
                api.parameter("samples", (int) temp.p[3]);
            }
            api.parameter("points", "point", "vertex", temp.verticesToArray());
            api.parameter("triangles", temp.triangleIndicesToArray());
            api.light("Shader_" + i, "triangle_mesh");
        } else {
            PApplet.println(FILLER_LIGHT_ERROR);
            return false;
        }

        //light spheres
        List<Float> spheres = temp.getSpheres();
        int noOfSpheres = (int) spheres.size() / 4;

        for (int j = 0; j < noOfSpheres; j++) {
            float x = spheres.get(j * 4);
            float y = spheres.get(j * 4 + 1);
            float z = spheres.get(j * 4 + 2);
            float r = spheres.get(j * 4 + 3);

            if (temp.np == 3 || temp.np == 4) {
                api.parameter("radiance", null, new float[]{temp.p[0], temp.p[1], temp.p[2]});
                api.parameter("center", new Point3(x, y, z));
                api.parameter("radius", r);
                if (temp.np == 4) {
                    api.parameter("samples", (int) temp.p[3]);
                }
                api.light("SphereLight_" + i + "_" + j, "sphere");
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean buildFiller(JRFiller temp, int i) {
        //constant shader
        if (temp.getType() == null ? CONSTANT == null : temp.getType().equals(CONSTANT)) {
            if (temp.np == 3) {
                api.parameter("color", SRGB_NONLINEAR, temp.p[0] / 255f, temp.p[1] / 255f, temp.p[2] / 255f);
                api.shader("Shader_" + i, "constant");
            } else {
                PApplet.println(FILLER_CONSTANT_ERROR);
                return false;
            }
        }

        //diffuse shader
        if (temp.getType() == null ? DIFFUSE == null : temp.getType().equals(DIFFUSE)) {
            if (temp.np == 3) {
                api.parameter("diffuse", SRGB_NONLINEAR, temp.p[0] / 255f, temp.p[1] / 255f, temp.p[2] / 255f);
                api.shader("Shader_" + i, "diffuse");
            } else {
                PApplet.println(FILLER_DIFFUSE_ERROR);
                return false;
            }
        }

        //shiny shader
        if (temp.getType() == null ? SHINY == null : temp.getType().equals(SHINY)) {
            if (temp.np == 3 || temp.np == 4) {
                api.parameter("diffuse", SRGB_NONLINEAR, temp.p[0] / 255f, temp.p[1] / 255f, temp.p[2] / 255f);
                if (temp.np == 4) {
                    api.parameter("shiny", temp.p[3]);
                }
                api.shader("Shader_" + i, "shiny_diffuse");
            } else {
                PApplet.println(FILLER_SHINY_ERROR);
                return false;
            }
        }

        //mirror shader
        if (temp.getType() == null ? MIRROR == null : temp.getType().equals(MIRROR)) {
            if (temp.np == 3) {
                api.parameter("color", SRGB_NONLINEAR, temp.p[0] / 255f, temp.p[1] / 255f, temp.p[2] / 255f);
                api.shader("Shader_" + i, "mirror");
            } else {
                PApplet.println(FILLER_MIRROR_ERROR);
                return false;
            }
        }

        //glass shader
        if (temp.getType() == null ? GLASS == null : temp.getType().equals(GLASS)) {
            if (temp.np == 3 || temp.np == 4 || temp.np == 8) {
                api.parameter("color", SRGB_NONLINEAR, temp.p[0] / 255f, temp.p[1] / 255f, temp.p[2] / 255f);
                if (temp.np >= 4) {
                    api.parameter("eta", temp.p[3]); // index of refraction, IOR, def 1.6f
                }
                if (temp.np == 8) {
                    api.parameter("absorption.distance", temp.p[4]); // def 5
                    api.parameter("absorption.color", SRGB_NONLINEAR, temp.p[5] / 255f, temp.p[6] / 255f, temp.p[7] / 255f);
                }
                api.shader("Shader_" + i, "glass");
            } else {
                PApplet.println(FILLER_GLASS_ERROR);
                return false;
            }
        }

        //phong shader
        if (temp.getType() == null ? PHONG == null : temp.getType().equals(PHONG)) {
            if (temp.np == 3 || temp.np == 6 || temp.np == 8) {
                api.parameter("diffuse", SRGB_NONLINEAR, new float[]{temp.p[0] / 255f, temp.p[1] / 255f, temp.p[2] / 255f});
                if (temp.np >= 6) {
                    api.parameter("specular", SRGB_NONLINEAR, new float[]{temp.p[3] / 255f, temp.p[4] / 255f, temp.p[5] / 255f});
                }
                if (temp.np == 8) {
                    api.parameter("power", temp.p[6]);
                    api.parameter("samples", (int) temp.p[7]);
                }
                api.shader("Shader_" + i, "phong");
            } else {
                PApplet.println(FILLER_PHONG_ERROR);
                return false;
            }
        }

        //ambient occlusion shader
        if (temp.getType() == null ? AMBIENT_OCCLUSION == null : temp.getType().equals(AMBIENT_OCCLUSION)) {
            if (temp.np == 3 || temp.np == 8) {
                api.parameter("bright", SRGB_NONLINEAR, new float[]{temp.p[0] / 255f, temp.p[1] / 255f, temp.p[2] / 255f});
                if (temp.np != 8) {
                    api.parameter("maxdist", DEF_AMB_OCC_MAX_DIST);
                }
                if (temp.np == 8) {
                    api.parameter("dark", SRGB_NONLINEAR, new float[]{temp.p[3] / 255f, temp.p[4] / 255f, temp.p[5] / 255f});
                    api.parameter("maxdist", temp.p[6]);
                    api.parameter("samples", (int) temp.p[7]);
                }
                api.shader("Shader_" + i, "ambient_occlusion");
            } else {
                PApplet.println(FILLER_AMB_OCC_ERROR);
                return false;
            }
        }
        return true;
    }

    private void buildInstance(JRFiller temp, int i) {
        //render the respective objects with the above defined shaders

        //generic mesh method
        api.parameter("points", "point", "vertex", temp.verticesToArray()); //np is the number of points, or vertices
        api.parameter("triangles", temp.triangleIndicesToArray()); //nt is the number of triangle faces.
        api.geometry("Object_" + i, "triangle_mesh");
        api.parameter("shaders", "Shader_" + i);
        api.instance("Object_" + i + ".instance", "Object_" + i);

        //render the respective spheres
        List<Float> spheres = temp.getSpheres();
        int noOfSpheres = spheres.size() / 4;

        for (int j = 0; j < noOfSpheres; j++) {
            float x = spheres.get(j * 4);
            float y = spheres.get(j * 4 + 1);
            float z = spheres.get(j * 4 + 2);
            float r = spheres.get(j * 4 + 3);

            Matrix4 translate = Matrix4.IDENTITY.multiply(Matrix4.translation(x, y, z));
            Matrix4 scale = Matrix4.IDENTITY.multiply(Matrix4.scale(r, r, r));

            Matrix4 m = Matrix4.IDENTITY;
            m = scale.multiply(m);
            m = translate.multiply(m);

            api.geometry("Sphere_" + i + "_" + j, "sphere");
            api.parameter("shaders", "Shader_" + i);
            api.parameter("transform", m);
            api.instance("Sphere_" + i + "_" + j + ".instance", "Sphere_" + i + "_" + j);
        }
    }

    public void displayRendered(boolean displaySwitch) {
        //the below are to reset the display before displaying the rendered image
        if (rendered && displaySwitch) {
            app.background(255);
            app.noLights();
            app.camera();
            app.perspective();
            app.image(IMG_RENDERED, 0, 0, app.width, app.height);
        }
    }
}
