package com.test.dlna.service.dmr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.cling.model.ModelUtil;
import com.cling.model.types.UnsignedIntegerFourBytes;
import com.cling.support.avtransport.lastchange.AVTransportVariable;
import com.cling.support.lastchange.LastChange;
import com.cling.support.model.Channel;
import com.cling.support.model.MediaInfo;
import com.cling.support.model.PositionInfo;
import com.cling.support.model.StorageMedium;
import com.cling.support.model.TransportAction;
import com.cling.support.model.TransportInfo;
import com.cling.support.model.TransportState;
import com.cling.support.renderingcontrol.lastchange.ChannelMute;
import com.cling.support.renderingcontrol.lastchange.ChannelVolume;
import com.cling.support.renderingcontrol.lastchange.RenderingControlVariable;
import com.test.dlna.DLNAApplication;
import com.test.dlna.LocalPlayerActivity;
import com.test.dlna.players.RemotePlayer;
import com.test.dlna.service.Action;

import java.net.URI;
import java.util.logging.Logger;

public class DLNAPlayer {

    final private static Logger log = Logger.getLogger(DLNAPlayer.class.getName());
    private static final String TAG = DLNAPlayer.class.getName();

    final private UnsignedIntegerFourBytes instanceId;
    final private LastChange avTransportLastChange;
    final private LastChange renderingControlLastChange;

    private volatile TransportInfo currentTransportInfo = new TransportInfo();
    private PositionInfo currentPositionInfo = new PositionInfo();
    private MediaInfo currentMediaInfo = new MediaInfo();
    private double storedVolume;

    private RemotePlayer mRemotePlayer;

    public DLNAPlayer(UnsignedIntegerFourBytes instanceId,
                      LastChange avTransportLastChange,
                      LastChange renderingControlLastChange) {
        super();
        this.instanceId = instanceId;
        this.avTransportLastChange = avTransportLastChange;
        this.renderingControlLastChange = renderingControlLastChange;
    }

    public UnsignedIntegerFourBytes getInstanceId() {
        return instanceId;
    }

    public LastChange getAvTransportLastChange() {
        return avTransportLastChange;
    }

    public LastChange getRenderingControlLastChange() {
        return renderingControlLastChange;
    }

    synchronized public TransportInfo getCurrentTransportInfo() {
        return currentTransportInfo;
    }

    synchronized public PositionInfo getCurrentPositionInfo() {
        return currentPositionInfo;
    }

    synchronized public MediaInfo getCurrentMediaInfo() {
        return currentMediaInfo;
    }

    synchronized public void setURI(URI uri, String type, String name, String currentURIMetaData) {
        Log.i(TAG, "setURI " + uri);

        currentMediaInfo = new MediaInfo(uri.toString(), currentURIMetaData);
        currentPositionInfo = new PositionInfo(1, "", uri.toString());

        getAvTransportLastChange().setEventedValue(getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri));

        transportStateChanged(TransportState.STOPPED);

//        mRemotePlayer = new RemotePlayer(new GstMediaListener(), type, name);
//        mRemotePlayer.setDataSource(uri.toString());

        LocalPlayerActivity.setMediaListener(new GstMediaListener());
        Context context = DLNAApplication.getInstance().getApplicationContext();
        Intent intent = new Intent(context, LocalPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("name", name);
        intent.putExtra("type", type);
        intent.putExtra("playURI", uri.toString());
        context.startActivity(intent);
    }

    synchronized public void setMute(boolean desiredMute) {
        if (desiredMute && getVolume() > 0) {
            log.fine("Switching mute ON");
            setVolume(0);
        } else if (!desiredMute && getVolume() == 0) {
            log.fine("Switching mute OFF, restoring: " + storedVolume);
            setVolume(storedVolume);
        }
    }

    synchronized public TransportAction[] getCurrentTransportActions() {
        TransportState state = currentTransportInfo.getCurrentTransportState();
        TransportAction[] actions;

        switch (state) {
            case STOPPED:
                actions = new TransportAction[]{
                        TransportAction.Play
                };
                break;
            case PLAYING:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek
                };
                break;
            case PAUSED_PLAYBACK:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek,
                        TransportAction.Play
                };
                break;
            default:
                actions = null;
        }
        return actions;
    }

    synchronized protected void transportStateChanged(TransportState newState) {
        TransportState currentTransportState = currentTransportInfo.getCurrentTransportState();
        log.fine("Current state is: " + currentTransportState + ", changing to new state: " + newState);
        currentTransportInfo = new TransportInfo(newState);

        getAvTransportLastChange().setEventedValue(
                getInstanceId(),
                new AVTransportVariable.TransportState(newState),
                new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions())
        );
    }

    public double getVolume() {
        AudioManager audioManager = (AudioManager) DLNAApplication.getInstance().getApplicationContext().getSystemService(Service.AUDIO_SERVICE);
        double v = (double) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.i(TAG, "getVolume " + v);
        return v;
    }

    synchronized public void setVolume(double volume) {
        Log.i(TAG, "setVolume " + volume);
        storedVolume = getVolume();

        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", Action.SET_VOLUME);
        intent.putExtra("volume", volume);

        DLNAApplication.getInstance().getApplicationContext().sendBroadcast(intent);

        ChannelMute switchedMute =
                (storedVolume == 0 && volume > 0) || (storedVolume > 0 && volume == 0)
                        ? new ChannelMute(Channel.Master, storedVolume > 0 && volume == 0)
                        : null;

        getRenderingControlLastChange().setEventedValue(
                getInstanceId(),
                new RenderingControlVariable.Volume(
                        new ChannelVolume(Channel.Master, (int) (volume * 100))
                ),
                switchedMute != null
                        ? new RenderingControlVariable.Mute(switchedMute)
                        : null
        );
    }

    public void play() {
        Log.i(TAG, "play");
        sendBroadcastAction(Action.PLAY);
    }

    public void pause() {
        Log.i(TAG, "pause");
        sendBroadcastAction(Action.PAUSE);
    }

    public void stop() {
        Log.i(TAG, "stop");
        sendBroadcastAction(Action.STOP);
    }

    public void seek(int position) {
        Log.i(TAG, "seek " + position);
        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", Action.SEEK);
        intent.putExtra("position", position);
        DLNAApplication.getInstance().getApplicationContext().sendBroadcast(intent);
    }

    public void sendBroadcastAction(String action) {
        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", action);
        DLNAApplication.getInstance().getApplicationContext().sendBroadcast(intent);
    }

    public interface RemoterListener {
        void pause();

        void start();

        void stop();

        void endOfMedia();

        void positionChanged(int position);

        void durationChanged(int duration);
    }

    protected class GstMediaListener implements RemoterListener {
        public void pause() {
            transportStateChanged(TransportState.PAUSED_PLAYBACK);
        }

        public void start() {
            transportStateChanged(TransportState.PLAYING);
        }

        public void stop() {
            transportStateChanged(TransportState.STOPPED);
        }

        public void endOfMedia() {
            log.fine("End Of Media event received, stopping media player backend");
            transportStateChanged(TransportState.NO_MEDIA_PRESENT);
            //GstMediaPlayer.this.stop();
        }

        public void positionChanged(int position) {
            log.fine("Position Changed event received: " + position);
            synchronized (DLNAPlayer.this) {
                currentPositionInfo = new PositionInfo(1, currentMediaInfo.getMediaDuration(),
                        currentMediaInfo.getCurrentURI(), ModelUtil.toTimeString(position / 1000),
                        ModelUtil.toTimeString(position / 1000));
            }
        }

        public void durationChanged(int duration) {
            log.fine("Duration Changed event received: " + duration);
            synchronized (DLNAPlayer.this) {
                String newValue = ModelUtil.toTimeString(duration / 1000);
                currentMediaInfo = new MediaInfo(currentMediaInfo.getCurrentURI(), "",
                        new UnsignedIntegerFourBytes(1), newValue, StorageMedium.NETWORK);

                getAvTransportLastChange().setEventedValue(getInstanceId(),
                        new AVTransportVariable.CurrentTrackDuration(newValue),
                        new AVTransportVariable.CurrentMediaDuration(newValue));
            }
        }
    }
}

