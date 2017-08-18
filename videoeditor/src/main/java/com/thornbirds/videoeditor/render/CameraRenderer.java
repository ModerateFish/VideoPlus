package com.thornbirds.videoeditor.render;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.util.Log;

import com.thornbirds.videoeditor.Drawer.BaseEglDrawer;
import com.thornbirds.videoeditor.Drawer.BeautyDrawer;
import com.thornbirds.videoeditor.Drawer.CameraDrawer;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;

public class CameraRenderer extends BaseEglRender implements SurfaceTexture.OnFrameAvailableListener {

    private final CameraHelper mCameraHelper;
    private BaseEglDrawer mLeftDrawer;
    private BaseEglDrawer mRightDrawer;

    private GLSurfaceView mSurfaceView;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;

    @Override
    protected String getTAG() {
        return "CameraRenderer";
    }

    public CameraRenderer(@NonNull GLSurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mCameraHelper = new CameraHelper();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraHelper.stopPreview();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.w(TAG, "onSurfaceCreated");
        if (mTextureId == 0) {
            mTextureId = genTextureOes();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(this);
        }
        mCameraHelper.startPreview(mSurfaceTexture);
        mLeftDrawer = new CameraDrawer(mTextureId);
        mRightDrawer = new BeautyDrawer(mTextureId);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.w(TAG, "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.w(TAG, "onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        float[] matrix = new float[16];
        // mSurfaceTexture.getTransformMatrix(mtx);
        Matrix.setIdentityM(matrix, 0);
        Matrix.rotateM(matrix, 0, 180, 0, 0, 1);
        Matrix.rotateM(matrix, 0, 180, 0, 1, 0);
        Matrix.scaleM(matrix, 0, 0.5f, 0.5f, 0.5f);
        Matrix.translateM(matrix, 0, -1.0f, 0, 0);
        mLeftDrawer.drawSelf(matrix, mCameraHelper.mPreviewWidth, mCameraHelper.mPreviewHeight);
        Matrix.translateM(matrix, 0, 2.0f, 0, 0);
        mRightDrawer.drawSelf(matrix, mCameraHelper.mPreviewWidth, mCameraHelper.mPreviewHeight);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mSurfaceView.requestRender();
    }

    private static class CameraHelper {
        private static final String TAG = "CameraHelper";

        private static final int MIN_PREVIEW_PIXELS = 960 * 540;  // small screen
        private static final int MAX_PREVIEW_PIXELS = 1280 * 720; // large/HD screen

        private Camera mCamera;

        protected int mPreviewWidth = -1;
        protected int mPreviewHeight = -1;

        protected void startPreview(@NonNull SurfaceTexture surfaceTexture) {
            if (mCamera != null || surfaceTexture == null) {
                return;
            }
            try {
                mCamera = Camera.open(1);
                mCamera.setDisplayOrientation(90);
                adjustCameraPreviewSize();
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.startPreview();
            } catch (Exception e) {
                Log.e(TAG, "open camera failed, exception=" + e);
            }
        }

        protected void stopPreview() {
            if (mCamera == null) {
                return;
            }
            try {
                mCamera.setPreviewDisplay(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (IOException e) {
                Log.e(TAG, "stop camera failed, exception=" + e);
            }
        }

        private void adjustCameraPreviewSize() {
            Camera.Parameters p = mCamera.getParameters();
            List<Camera.Size> supportedSizes = p.getSupportedPreviewSizes();
            Camera.Size bestSize = supportedSizes.get(0);
            int bestPixels = -1;
            for (Camera.Size size : supportedSizes) {
                int pixels = size.width * size.height;
                if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                    continue;
                }
                if (pixels > bestPixels) {
                    bestPixels = pixels;
                    bestSize = size;
                }
            }
            p.setPreviewSize(bestSize.width, bestSize.height);
            p.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(p);

            mPreviewWidth = bestSize.height;
            mPreviewHeight = bestSize.width;
        }
    }
}