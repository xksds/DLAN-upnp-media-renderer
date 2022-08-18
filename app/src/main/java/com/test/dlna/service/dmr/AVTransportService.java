package com.test.dlna.service.dmr;

import android.util.Log;

import com.cling.model.types.ErrorCode;
import com.cling.model.types.UnsignedIntegerFourBytes;
import com.cling.support.avtransport.AVTransportErrorCode;
import com.cling.support.avtransport.AVTransportException;
import com.cling.support.avtransport.AbstractAVTransportService;
import com.cling.support.lastchange.LastChange;
import com.cling.support.model.DeviceCapabilities;
import com.cling.support.model.MediaInfo;
import com.cling.support.model.PlayMode;
import com.cling.support.model.PositionInfo;
import com.cling.support.model.SeekMode;
import com.cling.support.model.StorageMedium;
import com.cling.support.model.TransportAction;
import com.cling.support.model.TransportInfo;
import com.cling.support.model.TransportSettings;

import org.seamless.http.HttpFetch;
import org.seamless.util.URIUtil;

import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

public class AVTransportService extends AbstractAVTransportService {

    final private static Logger log = Logger.getLogger(AVTransportService.class.getName());

    private static final String TAG = "GstAVTransportService";

    final private Map<UnsignedIntegerFourBytes, DLNAPlayer> players;

    protected AVTransportService(LastChange lastChange, Map<UnsignedIntegerFourBytes, DLNAPlayer> players) {
        super(lastChange);
        this.players = players;
    }

    protected Map<UnsignedIntegerFourBytes, DLNAPlayer> getPlayers() {
        return players;
    }

    protected DLNAPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        DLNAPlayer player = getPlayers().get(instanceId);
        if (player == null) {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                  String currentURI,
                                  String currentURIMetaData) throws AVTransportException {
        Log.d(TAG, currentURI + "---" + currentURIMetaData);
        URI uri;
        try {
            uri = new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            );
        }

        if (currentURI.startsWith("http:")) {
            try {
                HttpFetch.validate(URIUtil.toURL(uri));
            } catch (Exception ex) {
                throw new AVTransportException(
                        AVTransportErrorCode.RESOURCE_NOT_FOUND, ex.getMessage()
                );
            }
        } else if (!currentURI.startsWith("file:")) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "Only HTTP and file: resource identifiers are supported"
            );
        }

        // TODO: Check mime type of resource against supported types
        // TODO: DIDL fragment parsing and handling of currentURIMetaData
        String type = "image";
        if (currentURIMetaData.contains("object.item.videoItem")) {
            type = "video";
        } else if (currentURIMetaData.contains("object.item.imageItem")) {
            type = "image";
        } else if (currentURIMetaData.contains("object.item.audioItem")) {
            type = "audio";
        }
        String name = currentURIMetaData.substring(currentURIMetaData.indexOf("<dc:title>") + 10,
                currentURIMetaData.indexOf("</dc:title>"));
        Log.d(TAG, name);

        getInstance(instanceId).setURI(uri, type, name, currentURIMetaData);
    }

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentMediaInfo();
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentTransportInfo();
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentPositionInfo();
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId);
        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId);
        return new TransportSettings(PlayMode.NORMAL);
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).stop();
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
        getInstance(instanceId).play();
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).pause();
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Record");
    }

    private int getRealTime(String paramString) {
        int i = paramString.indexOf(":");
        int j = 0;
        if (i > 0) {
            String[] arrayOfString = paramString.split(":");
            j = Integer.parseInt(arrayOfString[2]) + 60
                    * Integer.parseInt(arrayOfString[1]) + 3600
                    * Integer.parseInt(arrayOfString[0]);
        }
        return j;
    }
    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
        final DLNAPlayer player = getInstance(instanceId);
        SeekMode seekMode;
        try {
            seekMode = SeekMode.valueOrExceptionOf(unit);

            if (!seekMode.equals(SeekMode.REL_TIME)) {
                throw new IllegalArgumentException();
            }

            int pos = (int) (getRealTime(target) * 1000);
            Log.i(TAG, "### " + unit + " target: " + target + "  pos: " + pos);
            getInstance(instanceId).seek(pos);
        } catch (IllegalArgumentException ex) {
            throw new AVTransportException(
                    AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + unit
            );
        }
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        log.info("### TODO: Not implemented: Next");
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        log.info("### TODO: Not implemented: Previous");
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                      String nextURI,
                                      String nextURIMetaData) throws AVTransportException {
        log.info("### TODO: Not implemented: SetNextAVTransportURI");
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
        log.info("### TODO: Not implemented: SetPlayMode");
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
        log.info("### TODO: Not implemented: SetRecordQualityMode");
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return getInstance(instanceId).getCurrentTransportActions();
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[getPlayers().size()];
        int i = 0;
        for (UnsignedIntegerFourBytes id : getPlayers().keySet()) {
            ids[i] = id;
            i++;
        }
        return ids;
    }
}
