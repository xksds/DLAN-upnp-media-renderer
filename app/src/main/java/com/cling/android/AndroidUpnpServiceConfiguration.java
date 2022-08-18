/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.cling.android;

import com.cling.DefaultUpnpServiceConfiguration;
import com.cling.binding.xml.DeviceDescriptorBinder;
import com.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import com.cling.binding.xml.ServiceDescriptorBinder;
import com.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import com.cling.model.Namespace;
import com.cling.transport.impl.AsyncServletStreamServerConfigurationImpl;
import com.cling.transport.impl.AsyncServletStreamServerImpl;
import com.cling.transport.impl.RecoveringGENAEventProcessorImpl;
import com.cling.transport.impl.RecoveringSOAPActionProcessorImpl;
import com.cling.transport.impl.jetty.JettyServletContainer;
import com.cling.transport.spi.GENAEventProcessor;
import com.cling.transport.spi.NetworkAddressFactory;
import com.cling.transport.spi.SOAPActionProcessor;
import com.cling.transport.spi.StreamClient;
import com.cling.transport.spi.StreamServer;

/**
 * Configuration settings for deployment on Android.
 * <p>
 * This configuration utilizes the Jetty transport implementation
 * found in {@link com.cling.transport.impl.jetty} for TCP/HTTP networking, as
 * client and server. The servlet context path for UPnP is set to <code>/upnp</code>.
 * </p>
 * <p>
 * The kxml2 implementation of <code>org.xmlpull</code> is available Android, therefore
 * this configuration uses {@link RecoveringUDA10DeviceDescriptorBinderImpl},
 * {@link RecoveringSOAPActionProcessorImpl}, and {@link RecoveringGENAEventProcessorImpl}.
 * </p>
 * <p>
 * This configuration utilizes {@link UDA10ServiceDescriptorBinderSAXImpl}, the system property
 * <code>org.xml.sax.driver</code> is set to  <code>org.xmlpull.v1.sax2.Driver</code>.
 * </p>
 * <p>
 * To preserve battery, the {@link com.cling.registry.Registry} will only
 * be maintained every 3 seconds.
 * </p>
 *
 * @author Christian Bauer
 */
public class AndroidUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    public AndroidUpnpServiceConfiguration() {
        this(0); // Ephemeral port
    }

    public AndroidUpnpServiceConfiguration(int streamListenPort) {
        super(streamListenPort, false);

        // This should be the default on Android 2.1 but it's not set by default
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
    }

    @Override
    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort) {
        return new AndroidNetworkAddressFactory(streamListenPort);
    }

    @Override
    protected Namespace createNamespace() {
        // For the Jetty server, this is the servlet context path
        return new Namespace("/upnp");
    }

    @Override
    public StreamClient createStreamClient() {
       return null;
    }

    @Override
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        // Use Jetty, start/stop a new shared instance of JettyServletContainer
        return new AsyncServletStreamServerImpl(
                new AsyncServletStreamServerConfigurationImpl(
                        JettyServletContainer.INSTANCE,
                        networkAddressFactory.getStreamListenPort()
                )
        );
    }

    @Override
    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return new RecoveringUDA10DeviceDescriptorBinderImpl();
    }

    @Override
    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return new UDA10ServiceDescriptorBinderSAXImpl();
    }

    @Override
    protected SOAPActionProcessor createSOAPActionProcessor() {
        return new RecoveringSOAPActionProcessorImpl();
    }

    @Override
    protected GENAEventProcessor createGENAEventProcessor() {
        return new RecoveringGENAEventProcessorImpl();
    }

    @Override
    public int getRegistryMaintenanceIntervalMillis() {
        return 3000; // Preserve battery on Android, only run every 3 seconds
    }

}
