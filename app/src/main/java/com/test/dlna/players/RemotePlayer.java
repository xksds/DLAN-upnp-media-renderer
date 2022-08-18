package com.test.dlna.players;

import android.util.Log;

import com.test.dlna.service.dmr.DLNAPlayer;

public class RemotePlayer {
    private final static String TAG = "RemotePlayer";
    private DLNAPlayer.RemoterListener mDLNARemoterListener;
    private String mMediaType, mDisplayTitle;

    public RemotePlayer(DLNAPlayer.RemoterListener listener, String type, String title) {
        mDLNARemoterListener = listener;
        mMediaType = type;
        mDisplayTitle = title;
    }

    public void setDataSource(String uri) {
        Log.i(TAG, "setDataSource: " + uri);
    }

    public void start() {

    }

    public void pause() {

    }

    public void stop() {

    }

    public void release() {

    }

    public boolean isPlaying() {
        return false;
    }

    public void seekTo(long pos) {

    }

    public long getDuration() {
        return 0;
    }

    public long getCurrentPosition() {
        return 0;
    }
}
