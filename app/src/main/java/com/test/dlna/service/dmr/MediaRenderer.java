package com.test.dlna.service.dmr;

import android.util.Log;

import com.cling.binding.LocalServiceBinder;
import com.cling.binding.annotations.AnnotationLocalServiceBinder;
import com.cling.model.DefaultServiceManager;
import com.cling.model.ServiceManager;
import com.cling.model.ValidationException;
import com.cling.model.meta.DeviceDetails;
import com.cling.model.meta.DeviceIdentity;
import com.cling.model.meta.Icon;
import com.cling.model.meta.LocalDevice;
import com.cling.model.meta.LocalService;
import com.cling.model.meta.ManufacturerDetails;
import com.cling.model.meta.ModelDetails;
import com.cling.model.types.DLNACaps;
import com.cling.model.types.DLNADoc;
import com.cling.model.types.UDADeviceType;
import com.cling.model.types.UDN;
import com.cling.model.types.UnsignedIntegerFourBytes;
import com.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import com.cling.support.lastchange.LastChange;
import com.cling.support.lastchange.LastChangeAwareServiceManager;
import com.cling.support.model.TransportState;
import com.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import com.test.dlna.DLNAApplication;
import com.test.dlna.service.Config;
import com.test.dlna.service.UpnpUtil;

import java.io.IOException;
import java.util.Map;

public class MediaRenderer {

    public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 500;

    private static final String TAG = MediaRenderer.class.getName();

    final protected LocalServiceBinder binder = new AnnotationLocalServiceBinder();

    // These are shared between all "logical" player instances of a single service
    final protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    final protected LastChange renderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    final protected Map<UnsignedIntegerFourBytes, DLNAPlayer> mediaPlayers;

    final protected ServiceManager<ConnectionManagerService> connectionManager;
    final protected LastChangeAwareServiceManager<AVTransportService> avTransport;
    final protected LastChangeAwareServiceManager<AudioRenderingControl> renderingControl;

    final protected LocalDevice device;

    public MediaRenderer(int numberOfPlayers) {

        mediaPlayers = new MediaPlayerManager(
                numberOfPlayers,
                avTransportLastChange,
                renderingControlLastChange
        ) {
            // These overrides connect the player instances to the output/display
            @Override
            protected void onPlay(DLNAPlayer player) {
//                getDisplayHandler().onPlay(player);
            }

            @Override
            protected void onStop(DLNAPlayer player) {
//                getDisplayHandler().onStop(player);
            }
        };

        LocalService connectionManagerService = binder.read(ConnectionManagerService.class);
        connectionManager =
                new DefaultServiceManager(connectionManagerService) {
                    @Override
                    protected Object createServiceInstance() throws Exception {
                        return new ConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionManager);

        LocalService<AVTransportService> avTransportService = binder.read(AVTransportService.class);
        avTransport =
                new LastChangeAwareServiceManager<AVTransportService>(
                        avTransportService,
                        new AVTransportLastChangeParser()
                ) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(avTransportLastChange, mediaPlayers);
                    }
                };
        avTransportService.setManager(avTransport);

        // The Rendering Control just passes the calls on to the backend players
        LocalService<AudioRenderingControl> renderingControlService = binder.read(AudioRenderingControl.class);
        renderingControl =
                new LastChangeAwareServiceManager<AudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected AudioRenderingControl createServiceInstance() throws Exception {
                        return new AudioRenderingControl(renderingControlLastChange, mediaPlayers);
                    }
                };
        renderingControlService.setManager(renderingControl);

        try {
            UDN udn = UpnpUtil.uniqueSystemIdentifier("msidmr");

            device = new LocalDevice(
                    new DeviceIdentity(udn),
                    new UDADeviceType("MediaRenderer", 1),
                    new DeviceDetails(
                            Config.SERVICE_NAME + " (" + android.os.Build.MODEL + ")",
                            new ManufacturerDetails(Config.MANUFACTURER),
                            new ModelDetails(Config.DMR_NAME, Config.DMR_DESC, "1", Config.DMR_MODEL_URL),
                            new DLNADoc[]{new DLNADoc("DMR", DLNADoc.Version.V1_5)},
                            new DLNACaps(new String[]{"av-playback"})
                    ),
                    new Icon[]{createDefaultDeviceIcon()},
                    new LocalService[]{
                            avTransportService,
                            renderingControlService,
                            connectionManagerService
                    }
            );
            Log.i(TAG, "getType: " + device.getType().toString());
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }

        runLastChangePushThread();
    }

    protected void runLastChangePushThread() {
        // TODO: We should only run this if we actually have event subscribers
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        avTransport.fireLastChange();
                        renderingControl.fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "runLastChangePushThread ex", ex);
                }
            }
        }.start();
    }

    public LocalDevice getDevice() {
        return device;
    }


    synchronized public Map<UnsignedIntegerFourBytes, DLNAPlayer> getMediaPlayers() {
        return mediaPlayers;
    }

    synchronized public void stopAllMediaPlayers() {
        for (DLNAPlayer mediaPlayer : mediaPlayers.values()) {
            TransportState state =
                    mediaPlayer.getCurrentTransportInfo().getCurrentTransportState();
            if (!state.equals(TransportState.NO_MEDIA_PRESENT) ||
                    state.equals(TransportState.STOPPED)) {
                Log.i(TAG, "Stopping player instance: " + mediaPlayer.getInstanceId());
                mediaPlayer.stop();
            }
        }
    }

    public ServiceManager<ConnectionManagerService> getConnectionManager() {
        return connectionManager;
    }

    public ServiceManager<AVTransportService> getAvTransport() {
        return avTransport;
    }

    public ServiceManager<AudioRenderingControl> getRenderingControl() {
        return renderingControl;
    }

    protected Icon createDefaultDeviceIcon() {
        try {
            return new Icon("image/png", 48, 48, 32,
                    "msi.png",
                    DLNAApplication.getInstance().getApplicationContext().getResources().getAssets()
                            .open(Config.LOGO));
        } catch (IOException e) {
            Log.w(TAG, "createDefaultDeviceIcon IOException");
            return null;
        }
    }

}
