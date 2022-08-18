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

package com.cling.support.messagebox;

import com.cling.controlpoint.ActionCallback;
import com.cling.model.action.ActionInvocation;
import com.cling.model.meta.Service;
import com.cling.support.messagebox.model.Message;

/**
 * ATTENTION: My Samsung TV does not implement this!
 *
 * @author Christian Bauer
 */
public abstract class RemoveMessage extends ActionCallback {

    public RemoveMessage(Service service, Message message) {
        this(service, message.getId());
    }

    public RemoveMessage(Service service, int id) {
        super(new ActionInvocation(service.getAction("RemoveMessage")));
        getActionInvocation().setInput("MessageID", id);
    }

}
