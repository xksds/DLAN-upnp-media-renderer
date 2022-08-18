package com.test.dlna.service.dmr;

import com.cling.model.types.UnsignedIntegerFourBytes;
import com.cling.support.lastchange.LastChange;
import com.cling.support.model.TransportState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class MediaPlayerManager extends ConcurrentHashMap<UnsignedIntegerFourBytes, DLNAPlayer> {

    final private static Logger log = Logger.getLogger(MediaPlayerManager.class.getName());

    final protected LastChange avTransportLastChange;
    final protected LastChange renderingControlLastChange;


    public MediaPlayerManager(int numberOfPlayers,
                              LastChange avTransportLastChange,
                              LastChange renderingControlLastChange) {
        super(numberOfPlayers);
        this.avTransportLastChange = avTransportLastChange;
        this.renderingControlLastChange = renderingControlLastChange;

        for (int i = 0; i < numberOfPlayers; i++) {

            DLNAPlayer player =
                    new DLNAPlayer(
                            new UnsignedIntegerFourBytes(i),
                            avTransportLastChange,
                            renderingControlLastChange
                    ) {
                        @Override
                        protected void transportStateChanged(TransportState newState) {
                            super.transportStateChanged(newState);
                            if (newState.equals(TransportState.PLAYING)) {
                                onPlay(this);
                            } else if (newState.equals(TransportState.STOPPED)) {
                                onStop(this);
                            }
                        }
                    };
            put(player.getInstanceId(), player);
        }
    }

    protected void onPlay(DLNAPlayer player) {
        log.fine("Player is playing: " + player.getInstanceId());
    }

    protected void onStop(DLNAPlayer player) {
        log.fine("Player is stopping: " + player.getInstanceId());
    }
}
