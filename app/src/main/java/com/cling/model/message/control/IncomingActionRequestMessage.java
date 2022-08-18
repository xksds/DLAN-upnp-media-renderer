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

package com.cling.model.message.control;

import com.cling.model.action.ActionException;
import com.cling.model.message.StreamRequestMessage;
import com.cling.model.message.header.SoapActionHeader;
import com.cling.model.message.header.UpnpHeader;
import com.cling.model.meta.Action;
import com.cling.model.meta.LocalService;
import com.cling.model.meta.QueryStateVariableAction;
import com.cling.model.types.ErrorCode;
import com.cling.model.types.SoapActionType;

/**
 * @author Christian Bauer
 */
public class IncomingActionRequestMessage extends StreamRequestMessage implements ActionRequestMessage {

    final private Action action;
    final private String actionNamespace;

    public IncomingActionRequestMessage(StreamRequestMessage source,
                                        LocalService service) throws ActionException {
        super(source);

        SoapActionHeader soapActionHeader = getHeaders().getFirstHeader(UpnpHeader.Type.SOAPACTION, SoapActionHeader.class);
        if (soapActionHeader == null) {
            throw new ActionException(ErrorCode.INVALID_ACTION, "Missing SOAP action header");
        }

        SoapActionType actionType = soapActionHeader.getValue();

        this.action = service.getAction(actionType.getActionName());
        if (this.action == null) {
            throw new ActionException(ErrorCode.INVALID_ACTION, "Service doesn't implement action: " + actionType.getActionName());
        }

        if (!QueryStateVariableAction.ACTION_NAME.equals(actionType.getActionName())) {
            if (!service.getServiceType().implementsVersion(actionType.getServiceType())) {
                throw new ActionException(ErrorCode.INVALID_ACTION, "Service doesn't support the requested service version");
            }
        }

        this.actionNamespace = actionType.getTypeString();
    }

    public Action getAction() {
        return action;
    }

    public String getActionNamespace() {
        return actionNamespace;
    }

}
