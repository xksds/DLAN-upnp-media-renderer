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

import com.cling.model.ModelUtil;
import com.cling.model.message.OutgoingDatagramMessage;
import com.cling.model.message.UpnpRequest;
import com.cling.model.message.header.HostHeader;
import com.cling.model.message.header.MANHeader;
import com.cling.model.message.header.MXHeader;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.types.NotificationSubtype;

/**
 * @author Christian Bauer
 */
public class OutgoingSearchRequest extends OutgoingDatagramMessage<UpnpRequest> {

    private UpnpHeader searchTarget;

    public OutgoingSearchRequest(UpnpHeader searchTarget, int mxSeconds) {
        super(
                new UpnpRequest(UpnpRequest.Method.MSEARCH),
                ModelUtil.getInetAddressByName(IPV4_UPNP_MULTICAST_GROUP),
                UPNP_MULTICAST_PORT
        );

        this.searchTarget = searchTarget;

        getHeaders().add(UpnpHeader.Type.MAN, new MANHeader(NotificationSubtype.DISCOVER.getHeaderString()));
        getHeaders().add(UpnpHeader.Type.MX, new MXHeader(mxSeconds));
        getHeaders().add(UpnpHeader.Type.ST, searchTarget);
        getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
    }

    public UpnpHeader getSearchTarget() {
        return searchTarget;
    }
}