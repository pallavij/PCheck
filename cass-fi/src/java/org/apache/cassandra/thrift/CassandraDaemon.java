/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cassandra.thrift;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.TProcessorFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.CompactionManager;

/**
 * This class supports two methods for creating a Cassandra node daemon, 
 * invoking the class's main method, and using the jsvc wrapper from 
 * commons-daemon, (for more information on using this class with the 
 * jsvc wrapper, see the 
 * <a href="http://commons.apache.org/daemon/jsvc.html">Commons Daemon</a>
 * documentation).
 */

import org.apache.cassandra.Util;
import java.util.*;

public class CassandraDaemon
{
    private static Logger logger = Logger.getLogger(CassandraDaemon.class);
    private TThreadPoolServer serverEngine;

    private void setup() throws IOException, TTransportException
    {
    		System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(1) --- " + Util.diff());
        
        // log4j
        String file = System.getProperty("storage-config") + File.separator + "log4j.properties";
        PropertyConfigurator.configure(file);
        System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(1.5) --- " + Util.diff());
        int listenPort = DatabaseDescriptor.getThriftPort(); //this part takes 1sec
        
        System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(2) --- " + Util.diff());
        
        InetAddress listenAddr = DatabaseDescriptor.getThriftAddress();
        
        System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(3) --- " + Util.diff());
        /* 
         * If ThriftAddress was left completely unconfigured, then assume
         * the same default as ListenAddress
         */
        if (listenAddr == null)
            listenAddr = FBUtilities.getLocalAddress();
        
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                logger.error("Fatal exception in thread " + t, e);
                if (e instanceof OutOfMemoryError)
                {
                    System.exit(100);
                }
            }
        });
        System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(4) --- " + Util.diff());
				
				// initialize keyspaces  //jinsu this take .5sec 
        for (String table : DatabaseDescriptor.getTables())
        {
            if (logger.isDebugEnabled())
                logger.debug("opening keyspace " + table);
            Table.open(table);
        }
        
        System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(5) --- " + Util.diff());
        
				// replay the log if necessary and check for compaction candidates
        CommitLog.recover();
        
        System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(6) --- " + Util.diff());
        
        CompactionManager.instance.checkAllColumnFamilies();
        
				// start server internals
        StorageService.instance.initServer(); //this and bottom 3 lines takes .5sec
        System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(6.5) --- " + Util.diff());
        
        // now we start listening for clients
        CassandraServer cassandraServer = new CassandraServer();
        System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(6.6) --- " + Util.diff());
        Cassandra.Processor processor = new Cassandra.Processor(cassandraServer);
				
				System.out.println(new Date(System.currentTimeMillis()) + "- CD setup(7) --- " + Util.diff());
				
        // Transport
        TServerSocket tServerSocket = new TServerSocket(new InetSocketAddress(listenAddr, listenPort));
        
        logger.info(String.format("Binding thrift service to %s:%s", listenAddr, listenPort));

        // Protocol factory
        TProtocolFactory tProtocolFactory = new TBinaryProtocol.Factory();
        
        // Transport factory
        TTransportFactory inTransportFactory, outTransportFactory;
        if (DatabaseDescriptor.isThriftFramed())
        {
            inTransportFactory = new TFramedTransport.Factory();
            outTransportFactory = new TFramedTransport.Factory();
            
        }
        else
        {
            inTransportFactory = new TTransportFactory();
            outTransportFactory = new TTransportFactory();
        }
				//logger.info("&&&&passing spot 3");
        // ThreadPool Server
        TThreadPoolServer.Options options = new TThreadPoolServer.Options();
        options.minWorkerThreads = 64;
        serverEngine = new TThreadPoolServer(new TProcessorFactory(processor),
                                             tServerSocket,
                                             inTransportFactory,
                                             outTransportFactory,
                                             tProtocolFactory,
                                             tProtocolFactory,
                                             options);
        
    }

    /** hook for JSVC */
    public void init(String[] args) throws IOException, TTransportException
    {  
        setup();
    }

    /** hook for JSVC */
    public void start()
    {
    		logger.info("- cluster optQuest : CD.start()");
        logger.info("Cassandra starting up...");
        serverEngine.serve();
    }

    /** hook for JSVC */
    public void stop()
    {
        // this doesn't entirely shut down Cassandra, just the Thrift server.
        // jsvc takes care of taking the rest down
        logger.info("Cassandra shutting down...");
        serverEngine.stop();
    }
    
    
    /** hook for JSVC */
    public void destroy()
    {
        // this is supposed to "destroy any object created in init", but
        // StorageService et al. are crash-only, so we no-op here.
    }
    
    public static void main(String[] args)
    {
     		System.out.println(new Date(System.currentTimeMillis()) + "- CD main(1) --- " + Util.diff());
        CassandraDaemon daemon = new CassandraDaemon();
        String pidFile = System.getProperty("cassandra-pidfile");
        
        System.out.println(" - pidFile : " + pidFile);
        
        try
        {   
        		//jinsu playing
        		long then = System.currentTimeMillis();
        		
            daemon.setup(); //this takes 2sec
            
            long dif = System.currentTimeMillis() - then;
            
            System.out.println(new Date(System.currentTimeMillis()) + " - CD main(2) daemon.setup() --- took " + dif + " ms ");
            if (pidFile != null)
            {
                new File(pidFile).deleteOnExit();
            }
            
            if (System.getProperty("cassandra-foreground") == null)
            {
            		//jinsu debugging
								logger.info("&&&right before system.out closes");
								System.out.flush();
								
                System.out.close();
                System.err.close();
            }
            logger.info("\n");
						logger.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            logger.info("&&&&&&&&&&&&&&    CASS     Starting &&&&&&&&&&&&&");
            logger.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            logger.info("\n");
            daemon.start();
            
        }
        catch (Throwable e)
        {
            String msg = "Exception encountered during startup.";
            logger.error(msg, e);

            // try to warn user on stdout too, if we haven't already detached
            System.out.println(msg);
            e.printStackTrace();

            System.exit(3);
        }
    }
}
