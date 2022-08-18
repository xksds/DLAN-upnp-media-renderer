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

import static com.test.dlna.service.Config.IPV4_UPNP_MULTICAST_GROUP;
import static com.test.dlna.service.Config.UPNP_MULTICAST_PORT;

import com.cling.model.Location;
import com.cling.model.ModelUtil;
import com.cling.model.message.OutgoingDatagramMessage;
import com.cling.model.message.UpnpRequest;
import com.cling.model.message.header.HostHeader;
import com.cling.model.message.header.LocationHeader;
import com.cling.model.message.header.MaxAgeHeader;
import com.cling.model.message.header.NTSHeader;
import com.cling.model.message.header.ServerHeader;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.meta.LocalDevice;
import com.cling.model.types.NotificationSubtype;

/**
 * @author Christian Bauer
 */
public abstract class OutgoingNotificationRequest extends OutgoingDatagramMessage<UpnpRequest> {

    private NotificationSubtype type;

    protected OutgoingNotificationRequest(Location location, LocalDevice device, NotificationSubtype type) {
        super(
                new UpnpRequest(UpnpRequest.Method.NOTIFY),
                ModelUtil.getInetAddressByName(IPV4_UPNP_MULTICAST_GROUP),
                UPNP_MULTICAST_PORT
        );

        this.type = type;

        getHeaders().add(UpnpHeader.Type.MAX_AGE, new MaxAgeHeader(device.getIdentity().getMaxAgeSeconds()));
        getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(location.getURL()));

        getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
        getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
        getHeaders().add(UpnpHeader.Type.NTS, new NTSHeader(type));
    }

    public NotificationSubtype getType() {
        return type;
    }

}
