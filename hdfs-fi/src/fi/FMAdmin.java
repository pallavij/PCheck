/**
 * Copyright (c) 2012,
 * Thanh Do  <thanhdo@cs.wisc.edu>
 * Haryadi S. Gunawi  <haryadi@cs.uchicago.edu>
 * Pallavi Joshi  <pallavi@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


// this is a "command line usage" to connect to the FM


package org.fi;

import org.fi.*;

import java.io.*;
import java.net.InetSocketAddress;


public class FMAdmin {

  // DEPRECATED

  // we used to use this to communicate something to the
  // FMServer .. but guess what ... before we used
  // Hadoop RPC which is very slow, so we alleviate that
  // by just writing a file .. to a local file system as
  // the flagger
  // in the future we might want to just use XML RPC which
  // is much faster

  /*


    private static final String enableFailureOnFly = "-enable";
    private static final String disableFailureOnFly = "-disable";

    private static FMProtocol fmp = null;


    // ***********************************************
    public static void connect() {

    Configuration conf = new Configuration();
    InetSocketAddress addr =
    new InetSocketAddress(FMServer.bindAddr, FMServer.port);

    try {
    fmp = (FMProtocol)
    RPC.getProxy(FMProtocol.class, FMProtocol.versionID, addr, conf);
    } catch (IOException e) {
    Util.ERROR("cannot contact FM");
    e.printStackTrace();
    System.exit(0);
    }
    }

    // ***********************************************
    public static void parseArguments(String args[]) {
    int argsLen = (args == null) ? 0 : args.length;
    for (int i = 0; i < argsLen; i++) {
    String cmd = args[i];

    if (cmd.equals(enableFailureOnFly)) {
    connect();

    Util.FATAL(" THIS HAS BEEN DEPRECATED !!! \n");

    // fmp.enableFailure();
    System.exit(0);
    }

    else if (cmd.equals(disableFailureOnFly)) {
    connect();
    // fmp.disableFailure();

    Util.FATAL(" THIS HAS BEEN DEPRECATED !!! \n");

    System.exit(0);
    }

    else {
    Util.ERROR("Unrecognized command arg [" + cmd + "]");
    System.exit(0);
    }
    }
    }

    // ***********************************************
    public static void main(String[] args) {


    System.out.println("FMAdmin.Main: Starting ...");

    parseArguments(args);

    System.out.println("FMAdmin.Main: Stopping ...");
    }

  */
}








