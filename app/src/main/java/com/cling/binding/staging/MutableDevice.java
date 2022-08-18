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

package com.cling.binding.staging;

import com.cling.model.ValidationException;
import com.cling.model.meta.Device;
import com.cling.model.meta.DeviceDetails;
import com.cling.model.meta.Icon;
import com.cling.model.meta.ManufacturerDetails;
import com.cling.model.meta.ModelDetails;
import com.cling.model.meta.Service;
import com.cling.model.meta.UDAVersion;
import com.cling.model.types.DLNACaps;
import com.cling.model.types.DLNADoc;
import com.cling.model.types.DeviceType;
import com.cling.model.types.UDN;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MutableDevice {

    public UDN udn;
    public MutableUDAVersion udaVersion = new MutableUDAVersion();
    public URL baseURL;
    public String deviceType;
    public String friendlyName;
    public String manufacturer;
    public URI manufacturerURI;
    public String modelName;
    public String modelDescription;
    public String modelNumber;
    public URI modelURI;
    public String serialNumber;
    public String upc;
    public URI presentationURI;
    public List<DLNADoc> dlnaDocs = new ArrayList();
    public DLNACaps dlnaCaps;
    public List<MutableIcon> icons = new ArrayList();
    public List<MutableService> services = new ArrayList();
    public List<MutableDevice> embeddedDevices = new ArrayList();
    public MutableDevice parentDevice;

    public Device build(Device prototype) throws ValidationException {
        // Note how all embedded devices inherit the version and baseURL of the root!
        return build(prototype, createDeviceVersion(), baseURL);
    }

    public Device build(Device prototype, UDAVersion deviceVersion, URL baseURL) throws ValidationException {

        List<Device> embeddedDevicesList = new ArrayList();
        for (MutableDevice embeddedDevice : embeddedDevices) {
            embeddedDevicesList.add(embeddedDevice.build(prototype, deviceVersion, baseURL));
        }
        return prototype.newInstance(
                udn,
                deviceVersion,
                createDeviceType(),
                createDeviceDetails(baseURL),
                createIcons(),
                createServices(prototype),
                embeddedDevicesList
        );
    }

    public UDAVersion createDeviceVersion() {
        return new UDAVersion(udaVersion.major, udaVersion.minor);
    }

    public DeviceType createDeviceType() {
        return DeviceType.valueOf(deviceType);
    }

    public DeviceDetails createDeviceDetails(URL baseURL) {
        return new DeviceDetails(
                baseURL,
                friendlyName,
                new ManufacturerDetails(manufacturer, manufacturerURI),
                new ModelDetails(modelName, modelDescription, modelNumber, modelURI),
                serialNumber, upc, presentationURI, dlnaDocs.toArray(new DLNADoc[dlnaDocs.size()]), dlnaCaps
        );
    }

    public Icon[] createIcons() {
        Icon[] iconArray = new Icon[icons.size()];
        int i = 0;
        for (MutableIcon icon : icons) {
            iconArray[i++] = icon.build();
        }
        return iconArray;
    }

    public Service[] createServices(Device prototype) throws ValidationException {
        Service[] services = prototype.newServiceArray(this.services.size());
        int i = 0;
        for (MutableService service : this.services) {
            services[i++] = service.build(prototype);
        }
        return services;
    }

}
