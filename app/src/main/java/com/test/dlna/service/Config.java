package com.test.dlna.service;

public class Config {

    public static final String LOGO = "ic_launcher.png";

    public static final int UPNP_MULTICAST_PORT = 1900;
    public static final int UPNP_STREAM_PORT = 48500;
    public static final int UPNP_LOCAL_MULTICAST_PORT = 58500;

    public static final String IPV4_UPNP_MULTICAST_GROUP = "239.255.255.250";
    public static final int MIN_ADVERTISEMENT_AGE_SECONDS = 1800;

    public static final String MANUFACTURER = android.os.Build.MANUFACTURER;
    public static final String SERVICE_NAME = "DLAN Media Player";
    public static final String DMR_NAME = "MSI MediaRenderer";
    public static final String DMR_DESC = "MSI MediaRenderer";
    public static final String DMR_MODEL_URL = "http://4thline.org/projects/cling/mediarenderer/";

}
