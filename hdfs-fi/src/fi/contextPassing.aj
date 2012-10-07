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
import java.net.*;
import java.nio.channels.*;
import java.nio.*;
import java.util.*;

import java.net.InetSocketAddress;
import java.lang.Thread;
import java.lang.StackTraceElement;


import org.aspectj.lang.Signature; 
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.SourceLocation;


// import org.apache.hadoop.net.SocketOutputStream;


// *********************************************************************
// remember lowest precedence is executed first
// *********************************************************************
aspect precedenceAspect {
  declare precedence
    :
     fiHooks, contextWrapper, contextPassing, contextOrigin;
}

// context debug
aspect CD {
  public static boolean debug = false;
}

// *********************************************************************
// this aspect should define the context origin. One example is when
// a File is created with new File (String).  That string must be
// put in the context, and then hooked into the newly created object
// *********************************************************************
aspect contextOrigin {

  Object around(SocketChannel sc, SocketAddress sa)
    : (call (boolean SocketChannel.connect(SocketAddress)) &&
       target(sc) && args(sa) && !within(org.fi.*)) {
    if (sa instanceof InetSocketAddress) {
      InetSocketAddress isa = (InetSocketAddress)sa;
      String ctx = Util.getNetIOContextFromPort(isa.getPort());
      sc.context = new Context(ctx);

      if (CD.debug)
        System.out.format("# Context creation SC [%d]   \n", sc.context.getPort());

    }

    Object tmp = proceed(sc, sa);

    // check if sc is successfully connected or not
    Socket s = sc.socket();
    if (s.getLocalPort() != 0) {
      if (CD.debug) {
        System.out.format("# Adding socket history (a) ... [%d] %s \n",
                          s.getLocalPort(), Util.getNodeId());
      }
      Util.addSocketHistory(s);
    }


    return tmp;
  }


  
  SocketChannel around(SocketAddress sa):
	(call (static SocketChannel SocketChannel.open(SocketAddress)) && args(sa) && !within(org.fi.*)){

      System.out.println("In SocketChannel.open's instrumentation");
     
      Object res = proceed(sa);
      SocketChannel sc = (SocketChannel)res;	
 
      if (sa instanceof InetSocketAddress) {
         InetSocketAddress isa = (InetSocketAddress)sa;
         String ctx = Util.getNetIOContextFromPort(isa.getPort());
         sc.context = new Context(ctx);

         if (CD.debug)
            System.out.format("# Context creation SC [%d]   \n", sc.context.getPort());

      }
    

      // check if sc is successfully connected or not
      Socket s = sc.socket();
      if (s.getLocalPort() != 0) {
          if (CD.debug) {
               System.out.format("# Adding socket history (a) ... [%d] %s \n",
                          s.getLocalPort(), Util.getNodeId());
          }
          Util.addSocketHistory(s);
      }

      return sc;
	
  } 
	 


  // this is where we get the local port,
  // sc.finishConnect() in SocketIOWithTimeout.java
  after(SocketChannel sc) returning
    : (call (boolean SocketChannel.finishConnect()) &&
       target(sc) && !within(org.fi.*) ) {

    // everytime the call is successful, we'll get the local port
    // now let's add this local port to our history
    Socket s = sc.socket();

    if (CD.debug) {
      System.out.format("# Adding socket history (b) ... [%d] %s \n",
                        s.getLocalPort(), Util.getNodeId());
    }

    Util.addSocketHistory(s);
  }


  // *****************************************************************
  // creating context for output stream from channel
  // for any creation of new OutputStream+(...Socket...)
  // then we want to create context on behalf of this context.
  // If I don't understand the port Id ("Unknown-port") this means
  // this port is a created port on the fly ... let's check the socketHistory
  // MAKE sure build.xml involves the correct files from core/net/*.java
  
  Object around(Socket s, long to)
    : (call (OutputStream+.new(Socket,long)) && args(s, to)
       && !within(org.fi.*)
       ) {
    Object tmp = proceed(s, to);
    OutputStream os = (OutputStream)tmp;
    String ctx = Util.getNetIOContextFromPort(s.getPort());
    if (ctx != null) {
      os.context = new Context(ctx);

      if (CD.debug) {
        System.out.format
          ("# OutStream Context creation from network .. [%s] \n", ctx);
      }

    }
    return tmp;
  }



  Object around(Socket s, long to)
    : (call (InputStream+.new(Socket,long)) && args(s, to) && !within(org.fi.*) ) {
    Object tmp = proceed(s, to);
    ClassWC cwc = (ClassWC) tmp;
    String ctx = Util.getNetIOContextFromPort(s.getPort());
    if (ctx != null) {
      cwc.setContext(new Context(ctx));

      if (CD.debug) {
        System.out.format
          ("# InStream Context creation from network .. [%s] \n", ctx);
      }
    }
    else {
      Util.WARNING(" unknown port: " + s.getPort());
    }
    return tmp;
  }


  // *****************************************
  // socket.accept, pallavi2
  // if context originates from socket.accept, it means
  // we're a listener, so we need to get the remote port
  // *****************************************

  Object around()
    : (call (SocketChannel ServerSocketChannel.accept()) && !within(org.fi.*)) {
    Object tmp = proceed();
    SocketChannel sc = (SocketChannel) tmp;
    Socket s = sc.socket();
    int senderPort = s.getPort();
    String ctx = Util.getNetIOContextFromPort(senderPort);
    sc.setContext(new Context(ctx));
    //Util.debugContext(thisJoinPoint, sc.getContext(),
    //                  "Context creation sc.accept");
    System.out.format("# sc.accept() localPort=%d remotePort=%d \n",
                      s.getLocalPort(), s.getPort());
    return tmp;

  }


}



// *********************************************************************
// this aspect is saying that if we instantiate an object from another
// object that already has context, then that context should be cloned
// to the new object
// *********************************************************************
aspect contextPassing {

  // boolean debug = false;
  boolean debug = true;


  // *********************************************
  // this is a general way for passing context
  // *********************************************

  // find instantiations where contexts should be passed
  pointcut classWCNew()
    : call (ClassWC+.new(..,ClassWC+,..)) &&
    !within(org.fi.*);

  // pass the context (this is a generic advice)
  Object around() : classWCNew () {

    // I go on with the instantiation
    Object temp = proceed();

    // find any arg that is an instance of ClassWC
    // if found, then pass that context
    Object[] args = thisJoinPoint.getArgs();
    for (int i = 0; i < args.length; i++) {
      if (args[i] instanceof ClassWC) {
        ClassWC a = (ClassWC)(args[i]);
        passContext(thisJoinPoint, (ClassWC)(temp), a.getContext());
        break;
      }
    }
    return temp;
  }


  // *********************************************
  // common function to pass context
  // *********************************************
  ClassWC passContext(JoinPoint jp, ClassWC t, Context c) {
    if (!Util.assertCtx(jp, c)) return t;
    t.setContext(c);
    if (CD.debug) {
      System.out.println("# Context passing direct ...");
    }
    return t;
  }



}



// *********************************************************************
// this wraps BufferedOutputStream, so that we can get the buffer
// when we flush dataoutputstream and reverse engineer the buffer
// *********************************************************************
aspect contextWrapper {


  // intercept new BOS(..) with new BOSW(..)
  Object around(OutputStream os) 
    : ((!within(org.fi.*) && args(os)) &&
       (call (BufferedOutputStream.new(OutputStream)))
       ) {
    BufferedOutputStreamWrapper bosw = new BufferedOutputStreamWrapper(os);
    bosw.context = os.context; // context passing done explicitly for wrapper
    BufferedOutputStream bos = bosw;
    return (Object)bos;
  }
  
  // intercept new BOS(..) with new BOSW(..)
  Object around(OutputStream os, int sz) 
    : ((!within(org.fi.*) && args(os,sz)) &&
       (call (BufferedOutputStream.new(OutputStream,int)))
       ) {
    BufferedOutputStreamWrapper bosw = new BufferedOutputStreamWrapper(os, sz);
    bosw.context = os.context; // context passing done explicitly for wrapper
    BufferedOutputStream bos = bosw;
    return (Object)bos;
  }

  // intercept new DOS
  Object around(OutputStream os)
    : ((!within(org.fi.*) && args(os)) &&
       (call (DataOutputStream.new(OutputStream)))
       ) {
    Object obj = proceed(os);
    DataOutputStream dos = (DataOutputStream)obj;
    // context passing has been done
    if (os instanceof BufferedOutputStreamWrapper) {
      if (os.getContext() != null) {
	if (os.getContext() == dos.getContext()) {
	  Context ctx = dos.getContext();
	  ctx.setExtraContext(os);	      
	}
	else {
	  Util.FATAL("why OS's context is not equal to DOS's context");
	}
      }
    }
    return obj;
  }


}
