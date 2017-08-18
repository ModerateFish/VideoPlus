package com.thornbirds.videoeditor.Drawer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by yangli on 2017/8/17.
 */
public class BeautyDrawer extends BaseEglDrawer {

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private final static float squareCoords[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
    };
    private final static float textureVertices[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };
    private int texture;

    public BeautyDrawer(int texture) {
        this.texture = texture;
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram(); // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram); // creates OpenGL ES program executables
    }

    @Override
    public void drawSelf(float[] matrix, int width, int height) {
        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgram, "uMvpProject"), 1, false, matrix, 0);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "texelWidthOffset"), 1.0f / width);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "texelHeightOffset"), 1.0f / height);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "distanceNormalizationFactor"), 10.0f);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
    }

    protected final String vertexShaderCode = "uniform mat4 uMvpProject;\n" +
            "attribute vec4 vPosition;\n" +
            "attribute vec2 inputTextureCoordinate;\n" +
            "uniform float texelWidthOffset;\n" +
            "uniform float texelHeightOffset;\n" +
            "const int GAUSSIAN_SAMPLES = 9;\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n" +
            "void main() {\n" +
            "    gl_Position = uMvpProject * vPosition;\n" +
            "    textureCoordinate = inputTextureCoordinate;\n" +
            "    int multiplier = 0;\n" +
            "    vec2 blurStep;\n" +
            "    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
            "    for (int i = 0; i < GAUSSIAN_SAMPLES; i++) {\n" +
            "        multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));\n" +
            "        blurStep = float(multiplier) * singleStepOffset;\n" +
            "        blurCoordinates[i] = inputTextureCoordinate.xy + blurStep;\n" +
            "    }\n" +
            "}";

    protected final String fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES inputImageTexture\n;" +
            "uniform mediump float distanceNormalizationFactor\n;" +
            "const lowp int GAUSSIAN_SAMPLES = 9\n;" +
            "varying highp vec2 textureCoordinate\n;" +
            "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES]\n;" +
            "void main() {\n" +
            "    lowp vec4 centralColor\n;" +
            "    lowp float gaussianWeightTotal\n;" +
            "    lowp vec4 sum\n;" +
            "    lowp vec4 sampleColor\n;" +
            "    lowp float distanceFromCentralColor\n;" +
            "    lowp float gaussianWeight\n;" +
            "    centralColor = texture2D(inputImageTexture, blurCoordinates[4])\n;" +
            "    gaussianWeightTotal = 0.18\n;" +
            "    sum = centralColor * 0.18\n;" +

            "    sampleColor = texture2D(inputImageTexture, blurCoordinates[0])\n;" +
            "    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0)\n;" +
            "    gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor)\n;" +
            "    gaussianWeightTotal += gaussianWeight\n;" +
            "    sum += sampleColor * gaussianWeight\n;" +

            "    sampleColor = texture2D(inputImageTexture, blurCoordinates[1])\n;" +
            "    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0)\n;" +
            "    gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor)\n;" +
            "    gaussianWeightTotal += gaussianWeight\n;" +
            "    sum += sampleColor * gaussianWeight\n;" +

            "    sampleColor = texture2D(inputImageTexture, blurCoordinates[2])\n;" +
            "    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0)\n;" +
            "    gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor)\n;" +
            "    gaussianWeightTotal += gaussianWeight\n;" +
            "    sum += sampleColor * gaussianWeight\n;" +

            "    sampleColor = texture2D(inputImageTexture, blurCoordinates[3])\n;" +
            "    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0)\n;" +
            "    gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor)\n;" +
            "    gaussianWeightTotal += gaussianWeight\n;" +
            "    sum += sampleColor * gaussianWeight\n;" +

            "    sampleColor = texture2D(inputImageTexture, blurCoordinates[5])\n;" +
            "    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0)\n;" +
            "    gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor)\n;" +
            "    gaussianWeightTotal += gaussianWeight\n;" +
            "    sum += sampleColor * gaussianWeight\n;" +

            "    sampleColor = texture2D(inputImageTexture, blurCoordinates[6])\n;" +
            "    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0)\n;" +
            "    gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor)\n;" +
            "    gaussianWeightTotal += gaussianWeight\n;" +
            "    sum += sampleColor * gaussianWeight\n;" +

            "    sampleColor = texture2D(inputImageTexture, blurCoordinates[7])\n;" +
            "    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0)\n;" +
            "    gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor)\n;" +
            "    gaussianWeightTotal += gaussianWeight\n;" +
            "    sum += sampleColor * gaussianWeight\n;" +

            "    sampleColor = texture2D(inputImageTexture, blurCoordinates[8])\n;" +
            "    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0)\n;" +
            "    gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor)\n;" +
            "    gaussianWeightTotal += gaussianWeight\n;" +
            "    sum += sampleColor * gaussianWeight\n;" +

            "    gl_FragColor = sum / gaussianWeightTotal\n;" +
            "}";
}
