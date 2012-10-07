package org.apache.cassandra.net;
/*
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 */


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.cassandra.utils.FBUtilities;
import org.apache.log4j.Logger;

import org.apache.cassandra.config.DatabaseDescriptor;


//jinsu
import org.apache.cassandra.Util;
import java.util.*;
import java.io.*;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.net.MessagingService;

public class OutboundTcpConnection extends Thread
{
    private static final Logger logger = Logger.getLogger(OutboundTcpConnection.class);

    private static final ByteBuffer CLOSE_SENTINEL = ByteBuffer.allocate(0);
    private static final int OPEN_RETRY_DELAY = 100; // ms between retries

    private final OutboundTcpConnectionPool pool;
    private final InetAddress endpoint;
    private final BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<ByteBuffer>();
    private DataOutputStream output;
    private Socket socket;

    public OutboundTcpConnection(final OutboundTcpConnectionPool pool, final InetAddress remoteEp)
    {
        super("WRITE-" + remoteEp);
        this.pool = pool;
        this.endpoint = remoteEp;
    }

    public void write(ByteBuffer buffer)
    {
        try
        {
            queue.put(buffer);
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
    }

    public void closeSocket()
    {
        queue.clear();
        write(CLOSE_SENTINEL);
    }

    public void run()
    {
        while (true)
        {
            ByteBuffer bb = take();
            if (bb == CLOSE_SENTINEL)
            {
                disconnect();
                continue;
            }
            if (socket != null || connect())
                writeConnected(bb);
        }
    }

    private void writeConnected(ByteBuffer bb)
    {
        try
        {
            Util.debug(new Date(System.currentTimeMillis()) + "- outgoing wC(): " + socket.toString());	
            output.write(bb.array(), 0, bb.limit());
            if (queue.peek() == null)
            {
                output.flush();
            }
        }
        catch (IOException e)
        {
            logger.info("error writing to " + endpoint);
	    			logger.info(" - OTC(exception in wC(1)): " + e); 
	    
		


            disconnect();
	     
        }
    }

    private void disconnect()
    {
        if (socket != null)
        {
            try
            {	
                socket.close();
             
             //*******************************************************
             //REMOVE THIS LATER
             //JINSU: MAJOR HACK FOR STALE CONNECTION BUG
             //MESSAGINGSERVICE and GOSSIPER have cached connection information using connectionPoolManager and liveEndpoints_.
             //SO THERE COMES THE PROBLEM IF NODES RESTART TOO FAST AND 
             //THE CLUSTER DOESN''T SEE NODES DYING(we don't see node# is dead messsage.).
             //OK SO THE LOGIC IS THAT SINCE WE THIS SOCKET IS DEAD WE JUST CREATE A NEW CONNECTION POOL FOR THE ENDPOINT.
             //IT'S INEFFICIENT BUT IT WILL TAKE CARE OF THE STALE SOCKET PROBLEM.
             //
             //related hack : see MessagingService constructor
             //******************************************************
             MessagingService.instance.convict(endpoint);
             Gossiper.instance.convict(endpoint);
            }
            catch (IOException e)
            {
                if (logger.isDebugEnabled())
                    logger.debug("exception closing connection to " + endpoint, e);
            }
            output = null;
            socket = null;
        }
    }

    private ByteBuffer take()
    {
        ByteBuffer bb;
        try
        {
            bb = queue.take();
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
        return bb;
    }

    private boolean connect()
    {
        if (logger.isDebugEnabled())
            logger.debug("attempting to connect to " + endpoint);
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + DatabaseDescriptor.getRpcTimeout())
        {
            try
            {
                // zero means 'bind on any available port.'
                socket = new Socket(endpoint, DatabaseDescriptor.getStoragePort(), FBUtilities.getLocalAddress(), 0);
                socket.setTcpNoDelay(true);
                output = new DataOutputStream(socket.getOutputStream());
                return true;
            }
            catch (IOException e)
            {
                socket = null;
                Util.debug("- OTC in connect(1) : socket creation error(to : " + endpoint + " => " + e.toString());
                
                //JINSU added...
                if(e.toString().contains("Connection refused")) {
                	break;
                }
                if (logger.isTraceEnabled())
                    logger.trace("unable to connect to " + endpoint, e);
                try
                {
                    Thread.sleep(OPEN_RETRY_DELAY);
                }
                catch (InterruptedException e1)
                {
                    throw new AssertionError(e1);
                }
            }
        }
        return false;
    }
}
