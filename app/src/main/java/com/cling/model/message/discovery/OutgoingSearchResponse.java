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
import com.cling.model.message.OutgoingDatagramMessage;
import com.cling.model.message.UpnpResponse;
import com.cling.model.message.header.EXTHeader;
import com.cling.model.message.header.InterfaceMacHeader;
import com.cling.model.message.header.LocationHeader;
import com.cling.model.message.header.MaxAgeHeader;
import com.cling.model.message.header.ServerHeader;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.meta.LocalDevice;

/**
 * @author Christian Bauer
 */
public class OutgoingSearchResponse extends OutgoingDatagramMessage<UpnpResponse> {

    public OutgoingSearchResponse(IncomingDatagramMessage request,
                                  Location location,
                                  LocalDevice device) {

        super(new UpnpResponse(UpnpResponse.Status.OK), request.getSourceAddress(), request.getSourcePort());

        getHeaders().add(UpnpHeader.Type.MAX_AGE, new MaxAgeHeader(device.getIdentity().getMaxAgeSeconds()));
        getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(location.getURL()));
        getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
        getHeaders().add(UpnpHeader.Type.EXT, new EXTHeader());

        if (location.getNetworkAddress().getHardwareAddress() != null) {
            getHeaders().add(
                    UpnpHeader.Type.EXT_IFACE_MAC,
                    new InterfaceMacHeader(location.getNetworkAddress().getHardwareAddress())
            );
        }
    }

}
