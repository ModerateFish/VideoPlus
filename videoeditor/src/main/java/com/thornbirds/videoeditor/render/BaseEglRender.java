package com.thornbirds.videoeditor.render;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by yangli on 2017/8/17.
 */
public abstract class BaseEglRender implements GLSurfaceView.Renderer {
    protected final String TAG = getTAG();

    protected final int genTextureOes() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    protected final int genTexture2D() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        if (texture[0] != 0) {
            GLES20.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        }
        return texture[0];
    }

    protected final int genTexture2D(@NonNull Bitmap bitmap) {
        int texture = genTexture2D();
        if (texture != 0) {
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, bitmap, 0);
        }
        return texture;
    }

    protected abstract String getTAG();

    public void onPause() {
    }

    public void onResume() {
    }
}
