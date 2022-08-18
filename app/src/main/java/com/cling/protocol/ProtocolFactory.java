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

package com.cling.protocol;

import com.cling.UpnpService;
import com.cling.model.action.ActionInvocation;
import com.cling.model.gena.LocalGENASubscription;
import com.cling.model.gena.RemoteGENASubscription;
import com.cling.model.message.IncomingDatagramMessage;
import com.cling.model.message.StreamRequestMessage;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.meta.LocalDevice;
import com.cling.protocol.async.SendingNotificationAlive;
import com.cling.protocol.async.SendingNotificationByebye;
import com.cling.protocol.async.SendingSearch;
import com.cling.protocol.sync.SendingAction;
import com.cling.protocol.sync.SendingEvent;
import com.cling.protocol.sync.SendingRenewal;
import com.cling.protocol.sync.SendingSubscribe;
import com.cling.protocol.sync.SendingUnsubscribe;

import java.net.URL;

/**
 * Factory for UPnP protocols, the core implementation of the UPnP specification.
 * <p>
 * This factory creates an executable protocol either based on the received UPnP messsage, or
 * on local device/search/service metadata). A protocol is an aspect of the UPnP specification,
 * you can override individual protocols to customize the behavior of the UPnP stack.
 * </p>
 * <p>
 * An implementation has to be thread-safe.
 * </p>
 *
 * @author Christian Bauer
 */
public interface ProtocolFactory {

    public UpnpService getUpnpService();

    /**
     * Creates a {@link com.cling.protocol.async.ReceivingNotification},
     * {@link com.cling.protocol.async.ReceivingSearch},
     * or {@link com.cling.protocol.async.ReceivingSearchResponse} protocol.
     *
     * @param message The incoming message, either {@link com.cling.model.message.UpnpRequest} or
     *                {@link com.cling.model.message.UpnpResponse}.
     * @return The appropriate protocol that handles the messages or <code>null</code> if the message should be dropped.
     * @throws ProtocolCreationException If no protocol could be found for the message.
     */
    public ReceivingAsync createReceivingAsync(IncomingDatagramMessage message) throws ProtocolCreationException;

    /**
     * Creates a {@link com.cling.protocol.sync.ReceivingRetrieval},
     * {@link com.cling.protocol.sync.ReceivingAction},
     * {@link com.cling.protocol.sync.ReceivingSubscribe},
     * {@link com.cling.protocol.sync.ReceivingUnsubscribe}, or
     * {@link com.cling.protocol.sync.ReceivingEvent} protocol.
     *
     * @param requestMessage The incoming message, examime {@link com.cling.model.message.UpnpRequest.Method}
     *                       to determine the protocol.
     * @return The appropriate protocol that handles the messages.
     * @throws ProtocolCreationException If no protocol could be found for the message.
     */
    public ReceivingSync createReceivingSync(StreamRequestMessage requestMessage) throws ProtocolCreationException;

    /**
     * Called by the {@link com.cling.registry.Registry}, creates a protocol for announcing local devices.
     */
    public SendingNotificationAlive createSendingNotificationAlive(LocalDevice localDevice);

    /**
     * Called by the {@link com.cling.registry.Registry}, creates a protocol for announcing local devices.
     */
    public SendingNotificationByebye createSendingNotificationByebye(LocalDevice localDevice);

    /**
     * Called by the {@link com.cling.controlpoint.ControlPoint}, creates a protocol for a multicast search.
     */
    public SendingSearch createSendingSearch(UpnpHeader searchTarget, int mxSeconds);

    /**
     * Called by the {@link com.cling.controlpoint.ControlPoint}, creates a protocol for executing an action.
     */
    public SendingAction createSendingAction(ActionInvocation actionInvocation, URL controlURL);

    /**
     * Called by the {@link com.cling.controlpoint.ControlPoint}, creates a protocol for GENA subscription.
     */
    public SendingSubscribe createSendingSubscribe(RemoteGENASubscription subscription) throws ProtocolCreationException;

    /**
     * Called by the {@link com.cling.controlpoint.ControlPoint}, creates a protocol for GENA renewal.
     */
    public SendingRenewal createSendingRenewal(RemoteGENASubscription subscription);

    /**
     * Called by the {@link com.cling.controlpoint.ControlPoint}, creates a protocol for GENA unsubscription.
     */
    public SendingUnsubscribe createSendingUnsubscribe(RemoteGENASubscription subscription);

    /**
     * Called by the {@link com.cling.model.gena.GENASubscription}, creates a protocol for sending GENA events.
     */
    public SendingEvent createSendingEvent(LocalGENASubscription subscription);
}
