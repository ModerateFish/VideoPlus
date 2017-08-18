package com.thornbirds.videoeditor.Drawer;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Created by yangli on 2017/8/17.
 */
public abstract class BaseEglDrawer {

    protected final int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    protected final float[] transformTextureCoordinates(float[] coords, float[] matrix) {
        float[] result = new float[coords.length];
        float[] vt = new float[4];
        for (int i = 0; i < coords.length; i += 2) {
            float[] v = {coords[i], coords[i + 1], 0, 1};
            Matrix.multiplyMV(vt, 0, matrix, 0, v, 0);
            result[i] = vt[0];
            result[i + 1] = vt[1];
        }
        return result;
    }

    public abstract void drawSelf(float[] matrix, int width, int height);
}
