package com.thornbirds.videoeditor;

/**
 * Created by yangli on 2017/8/15.
 *
 * @module 视频录制器
 */
public class VideoRecorder {

    static {
        System.loadLibrary("video_recorder-lib");
    }

    protected native String stringFromJNI();
}
