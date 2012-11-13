//
//  ========================================================================
//  Copyright (c) 1995-2012 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.websocket.server;

import javax.websocket.Endpoint;
import javax.websocket.ServerContainer;

import org.eclipse.jetty.websocket.client.JettyClientContainer;

public class JettyServerContainer extends JettyClientContainer implements ServerContainer
{
    @Override
    public void publishServer(Class<? extends Endpoint> endpointClazz)
    {
        // TODO Auto-generated method stub
    }
}
