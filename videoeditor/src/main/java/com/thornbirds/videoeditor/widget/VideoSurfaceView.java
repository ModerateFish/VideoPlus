package com.thornbirds.videoeditor.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.thornbirds.videoeditor.render.BaseEglRender;
import com.thornbirds.videoeditor.render.CameraRenderer;

/**
 * Created by yangli on 2017/8/16.
 */
public class VideoSurfaceView extends GLSurfaceView {
    private final static String TAG = "VideoView";

    private BaseEglRender mRender;

    public VideoSurfaceView(Context context) {
        this(context, null);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {
        Log.w(TAG, "init");
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setEGLContextClientVersion(2);
        mRender = new CameraRenderer(this);
        setRenderer(mRender);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRender.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRender.onPause();
    }
}
