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

package com.cling.model.message.gena;

import com.cling.model.gena.RemoteGENASubscription;
import com.cling.model.message.StreamRequestMessage;
import com.cling.model.message.UpnpHeaders;
import com.cling.model.message.UpnpRequest;
import com.cling.model.message.header.SubscriptionIdHeader;
import com.cling.model.message.header.TimeoutHeader;
import com.cling.model.message.header.UpnpHeader;

/**
 * @author Christian Bauer
 */
public class OutgoingRenewalRequestMessage extends StreamRequestMessage {

    public OutgoingRenewalRequestMessage(RemoteGENASubscription subscription,
                                         UpnpHeaders extraHeaders) {

        super(UpnpRequest.Method.SUBSCRIBE, subscription.getEventSubscriptionURL());

        getHeaders().add(
                UpnpHeader.Type.SID,
                new SubscriptionIdHeader(subscription.getSubscriptionId())
        );

        getHeaders().add(
                UpnpHeader.Type.TIMEOUT,
                new TimeoutHeader(subscription.getRequestedDurationSeconds())
        );

        if (extraHeaders != null)
            getHeaders().putAll(extraHeaders);
    }

}