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

package org.eclipse.jetty.websocket.common.io;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.websocket.SendResult;

public class JavaxWebsocketFuture implements Future<SendResult>
{
    private final FrameBytes bytes;

    public JavaxWebsocketFuture(FrameBytes bytes)
    {
        this.bytes = bytes;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return this.bytes.cancel(mayInterruptIfRunning);
    }

    @Override
    public SendResult get() throws InterruptedException, ExecutionException
    {
        try
        {
            bytes.get();
            return new SendResult();
        }
        catch (ExecutionException e)
        {
            return new SendResult(e.getCause());
        }
    }

    @Override
    public SendResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        try
        {
            bytes.get(timeout,unit);
            return new SendResult();
        }
        catch (ExecutionException e)
        {
            return new SendResult(e.getCause());
        }
    }

    @Override
    public boolean isCancelled()
    {
        return this.bytes.isCancelled();
    }

    @Override
    public boolean isDone()
    {
        return this.bytes.isDone();
    }
}
