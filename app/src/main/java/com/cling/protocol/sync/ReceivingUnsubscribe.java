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

package com.cling.protocol.sync;

import com.cling.UpnpService;
import com.cling.model.gena.LocalGENASubscription;
import com.cling.model.message.StreamRequestMessage;
import com.cling.model.message.StreamResponseMessage;
import com.cling.model.message.UpnpResponse;
import com.cling.model.message.gena.IncomingUnsubscribeRequestMessage;
import com.cling.model.resource.ServiceEventSubscriptionResource;
import com.cling.protocol.ReceivingSync;
import com.cling.transport.RouterException;

import java.util.logging.Logger;

/**
 * Handles reception of GENA event unsubscribe messages.
 *
 * @author Christian Bauer
 */
public class ReceivingUnsubscribe extends ReceivingSync<StreamRequestMessage, StreamResponseMessage> {

    final private static Logger log = Logger.getLogger(ReceivingUnsubscribe.class.getName());

    public ReceivingUnsubscribe(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    protected StreamResponseMessage executeSync() throws RouterException {

        ServiceEventSubscriptionResource resource =
                getUpnpService().getRegistry().getResource(
                        ServiceEventSubscriptionResource.class,
                        getInputMessage().getUri()
                );

        if (resource == null) {
            log.fine("No local resource found: " + getInputMessage());
            return null;
        }

        log.fine("Found local event subscription matching relative request URI: " + getInputMessage().getUri());

        IncomingUnsubscribeRequestMessage requestMessage =
                new IncomingUnsubscribeRequestMessage(getInputMessage(), resource.getModel());

        // Error conditions UDA 1.0 section 4.1.3
        if (requestMessage.getSubscriptionId() != null &&
                (requestMessage.hasNotificationHeader() || requestMessage.hasCallbackHeader())) {
            log.fine("Subscription ID and NT or Callback in unsubcribe request: " + getInputMessage());
            return new StreamResponseMessage(UpnpResponse.Status.BAD_REQUEST);
        }

        LocalGENASubscription subscription =
                getUpnpService().getRegistry().getLocalSubscription(requestMessage.getSubscriptionId());

        if (subscription == null) {
            log.fine("Invalid subscription ID for unsubscribe request: " + getInputMessage());
            return new StreamResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
        }

        log.fine("Unregistering subscription: " + subscription);
        if (getUpnpService().getRegistry().removeLocalSubscription(subscription)) {
            subscription.end(null); // No reason, just an unsubscribe
        } else {
            log.fine("Subscription was already removed from registry");
        }

        return new StreamResponseMessage(UpnpResponse.Status.OK);
    }
}