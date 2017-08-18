package com.thornbirds.videoplus;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.thornbirds.videoeditor.render.CameraRenderer;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private GLSurfaceView mSurfaceView;
    private CameraRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (GLSurfaceView) findViewById(R.id.video_view);
        mRenderer = new CameraRenderer(mSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }
}
