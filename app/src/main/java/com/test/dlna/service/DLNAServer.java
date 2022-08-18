package com.test.dlna.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.cling.android.AndroidUpnpService;
import com.cling.android.AndroidUpnpServiceImpl;
import com.cling.model.meta.LocalDevice;
import com.cling.model.meta.RemoteDevice;
import com.cling.registry.DefaultRegistryListener;
import com.cling.registry.Registry;
import com.test.dlna.DLNAApplication;
import com.test.dlna.service.dmr.MediaRenderer;

public class DLNAServer {

    private static final String TAG = DLNAServer.class.getName();

    private AndroidUpnpService mUpnpService;
    private DeviceListRegistryListener deviceListRegistryListener;
    private boolean serviceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            mUpnpService = (AndroidUpnpService) service;

            Log.v(TAG, "Connected to UPnP Service");

//      TODO: High light. We just need start a meida renderer for display remote playback video/audio
            MediaRenderer mediaRenderer = new MediaRenderer(1);
            mUpnpService.getRegistry().addDevice(mediaRenderer.getDevice());
            mUpnpService.getRegistry().addListener(deviceListRegistryListener);
            mUpnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            mUpnpService = null;
        }
    };

    public DLNAServer() {
        deviceListRegistryListener = new DeviceListRegistryListener();
    }

    public int bindService() {
        Context context = DLNAApplication.getInstance().getApplicationContext();
        context.bindService(new Intent(context, AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
        serviceBound = true;
        return 0;
    }

    public int unbindService() {

        Log.d(TAG, "unbindService with mUpnpService " + mUpnpService + " serviceBound " + serviceBound);
        if (null != mUpnpService) {
            mUpnpService.getRegistry().removeListener(deviceListRegistryListener);
        }
        if (true == serviceBound) {
            DLNAApplication.getInstance().getApplicationContext().unbindService(serviceConnection);
            serviceBound = false;
        }
        return 0;
    }


    public class DeviceListRegistryListener extends DefaultRegistryListener {
        private final String LIS_TAG = DeviceListRegistryListener.class.getName();

        /* Discovery performance optimization for very slow Android devices! */

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            Log.i(LIS_TAG, "remoteDeviceDiscoveryStarted:" + device.toString() + device.getType().getType());
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            Log.i(LIS_TAG, "remoteDeviceDiscoveryFailed:" + device.toString() + device.getType().getType() + ex.toString());
        }

        /*
         * End of optimization, you can remove the whole block if your Android
         * handset is fast (>= 600 Mhz)
         */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            Log.i(LIS_TAG, "remoteDeviceAdded:" + device.toString() + device.getType().getType());
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            Log.i(LIS_TAG, "remoteDeviceRemoved:" + device.toString() + device.getType().getType());
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            Log.i(LIS_TAG, "localDeviceAdded:" + device.toString() + device.getType().getType());
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            Log.i(LIS_TAG, "localDeviceRemoved:" + device.toString() + device.getType().getType());

        }
    }
}
