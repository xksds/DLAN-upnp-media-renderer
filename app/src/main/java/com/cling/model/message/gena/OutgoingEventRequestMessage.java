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

import com.cling.model.gena.GENASubscription;
import com.cling.model.message.StreamRequestMessage;
import com.cling.model.message.UpnpRequest;
import com.cling.model.message.header.ContentTypeHeader;
import com.cling.model.message.header.EventSequenceHeader;
import com.cling.model.message.header.NTEventHeader;
import com.cling.model.message.header.NTSHeader;
import com.cling.model.message.header.SubscriptionIdHeader;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.state.StateVariableValue;
import com.cling.model.types.NotificationSubtype;
import com.cling.model.types.UnsignedIntegerFourBytes;

import java.net.URL;
import java.util.Collection;

/**
 * @author Christian Bauer
 */
public class OutgoingEventRequestMessage extends StreamRequestMessage {

    final private Collection<StateVariableValue> stateVariableValues;

    public OutgoingEventRequestMessage(GENASubscription subscription,
                                       URL callbackURL,
                                       UnsignedIntegerFourBytes sequence,
                                       Collection<StateVariableValue> values) {

        super(new UpnpRequest(UpnpRequest.Method.NOTIFY, callbackURL));

        getHeaders().add(UpnpHeader.Type.CONTENT_TYPE, new ContentTypeHeader());
        getHeaders().add(UpnpHeader.Type.NT, new NTEventHeader());
        getHeaders().add(UpnpHeader.Type.NTS, new NTSHeader(NotificationSubtype.PROPCHANGE));
        getHeaders().add(UpnpHeader.Type.SID, new SubscriptionIdHeader(subscription.getSubscriptionId()));

        // Important! Pass by value so that we can safely increment it afterwards and before this is send!
        getHeaders().add(UpnpHeader.Type.SEQ, new EventSequenceHeader(sequence.getValue()));

        this.stateVariableValues = values;
    }

    public OutgoingEventRequestMessage(GENASubscription subscription, URL callbackURL) {
        this(subscription, callbackURL, subscription.getCurrentSequence(), subscription.getCurrentValues().values());
    }

    public Collection<StateVariableValue> getStateVariableValues() {
        return stateVariableValues;
    }
}
