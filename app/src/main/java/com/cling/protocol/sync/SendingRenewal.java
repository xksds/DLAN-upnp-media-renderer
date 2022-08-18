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
import com.cling.model.gena.CancelReason;
import com.cling.model.gena.RemoteGENASubscription;
import com.cling.model.message.StreamResponseMessage;
import com.cling.model.message.gena.IncomingSubscribeResponseMessage;
import com.cling.model.message.gena.OutgoingRenewalRequestMessage;
import com.cling.protocol.SendingSync;
import com.cling.transport.RouterException;

import java.util.logging.Logger;

/**
 * Renewing a GENA event subscription with a remote host.
 * <p>
 * This protocol is executed periodically by the local registry, for any established GENA
 * subscription to a remote service. If renewal failed, the subscription will be removed
 * from the registry and the
 * {@link com.cling.model.gena.RemoteGENASubscription#end(com.cling.model.gena.CancelReason, com.cling.model.message.UpnpResponse)}
 * method will be called. The <code>RENEWAL_FAILED</code> reason will be used, however,
 * the response might be <code>null</code> if no response was received from the remote host.
 * </p>
 *
 * @author Christian Bauer
 */
public class SendingRenewal extends SendingSync<OutgoingRenewalRequestMessage, IncomingSubscribeResponseMessage> {

    final private static Logger log = Logger.getLogger(SendingRenewal.class.getName());

    final protected RemoteGENASubscription subscription;

    public SendingRenewal(UpnpService upnpService, RemoteGENASubscription subscription) {
        super(
                upnpService,
                new OutgoingRenewalRequestMessage(
                        subscription,
                        upnpService.getConfiguration().getEventSubscriptionHeaders(subscription.getService())
                )
        );
        this.subscription = subscription;
    }

    protected IncomingSubscribeResponseMessage executeSync() throws RouterException {
        log.fine("Sending subscription renewal request: " + getInputMessage());

        StreamResponseMessage response;
        try {
            response = getUpnpService().getRouter().send(getInputMessage());
        } catch (RouterException ex) {
            onRenewalFailure();
            throw ex;
        }

        if (response == null) {
            onRenewalFailure();
            return null;
        }

        final IncomingSubscribeResponseMessage responseMessage = new IncomingSubscribeResponseMessage(response);

        if (response.getOperation().isFailed()) {
            log.fine("Subscription renewal failed, response was: " + response);
            getUpnpService().getRegistry().removeRemoteSubscription(subscription);
            getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                    new Runnable() {
                        public void run() {
                            subscription.end(CancelReason.RENEWAL_FAILED, responseMessage.getOperation());
                        }
                    }
            );
        } else if (!responseMessage.isValidHeaders()) {
            log.severe("Subscription renewal failed, invalid or missing (SID, Timeout) response headers");
            getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                    new Runnable() {
                        public void run() {
                            subscription.end(CancelReason.RENEWAL_FAILED, responseMessage.getOperation());
                        }
                    }
            );
        } else {
            log.fine("Subscription renewed, updating in registry, response was: " + response);
            subscription.setActualSubscriptionDurationSeconds(responseMessage.getSubscriptionDurationSeconds());
            getUpnpService().getRegistry().updateRemoteSubscription(subscription);
        }

        return responseMessage;
    }

    protected void onRenewalFailure() {
        log.fine("Subscription renewal failed, removing subscription from registry");
        getUpnpService().getRegistry().removeRemoteSubscription(subscription);
        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                new Runnable() {
                    public void run() {
                        subscription.end(CancelReason.RENEWAL_FAILED, null);
                    }
                }
        );
    }
}