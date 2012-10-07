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



package org.fi;

import org.fi.*;

import java.io.*;
import java.net.InetSocketAddress;


public class FMDriver {



  // ***********************************************
  public static void parseArguments(String args[]) {
    int argsLen = (args == null) ? 0 : args.length;
    for (int i = 0; i < argsLen; i++) {
      String cmd = args[i];

      // this option is used when we run this normally
      // if (cmd.equals(enableFailureArg))  {
      // enableFailure = true; 
      // System.out.println("  Failure is ON ... !!!");
      // }

      // else {
      // Util.ERROR("Unrecognized command arg [" + cmd + "]");
      // System.exit(0);      
      // }
    }
  }

  // ***********************************************
  public static void main(String[] args) {


    System.out.println("FMDriver.Main: Starting ...");
    
    // parseArguments(args);

    // create new falure scheduler instance                                   
    FMServer fm = new FMServer();

    fm.start();
      

    /*
    try {
      fm.initialize();

    } catch (InterruptedException e) {

      System.out.println("FMDriver.Main: Exception ...");
      e.printStackTrace();
    }
    */


    System.out.println("FMDriver.Main: Stopping ...");
  }
}








