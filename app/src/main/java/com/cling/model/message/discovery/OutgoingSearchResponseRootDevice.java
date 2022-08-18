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

package com.cling.model.message.discovery;

import com.cling.model.Location;
import com.cling.model.message.IncomingDatagramMessage;
import com.cling.model.message.header.RootDeviceHeader;
import com.cling.model.message.header.USNRootDeviceHeader;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.meta.LocalDevice;

/**
 * @author Christian Bauer
 */
public class OutgoingSearchResponseRootDevice extends OutgoingSearchResponse {

    public OutgoingSearchResponseRootDevice(IncomingDatagramMessage request,
                                            Location location,
                                            LocalDevice device) {
        super(request, location, device);

        getHeaders().add(UpnpHeader.Type.ST, new RootDeviceHeader());
        getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(device.getIdentity().getUdn()));
    }
}
