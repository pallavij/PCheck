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

import java.lang.Thread;
import java.lang.StackTraceElement;

import org.aspectj.lang.Signature; // include this for Signature, etc!
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.SourceLocation;


// *********************************************************************
// remember lowest precedence is executed first
// *********************************************************************
aspect precedenceAspect {
  declare precedence
    :

  // OLD
  // contextOrigin, contextPassing, contextPassingNonDirect,
  // profilingHooks, fiHooks, frogHooks;


  bcastHooks, contextWrapper, contextPassingNonDirect, contextPassing, contextOrigin;

}

// context debug
aspect CD {
  public static boolean debug = true;
}

// *********************************************************************
// this aspect should define the context origin. One example is when
// a File is created with new File (String).  That string must be
// put in the context, and then hooked into the newly created object
// *********************************************************************
// >>> JASON: marked the aspect as privileged
privileged aspect contextOrigin {

  // context from new File(..)
  File around() : call(File.new(..)) && !within(org.fi.*) {
    File f = proceed();
    f.setContext(new Context(f.getAbsolutePath()));
    if (CD.debug) System.out.format("# Context creation [%s]\n", f.context);
    return f;
  }
 

  // context from new RandomAccessFile(name)
  RandomAccessFile around(String s, String m) : !within(org.fi.*) && call (RandomAccessFile.new(String,String)) && args(s,m) {
    RandomAccessFile r = proceed(s,m);
    File f = new File(s); // help from File to get the absolute path
    r.context = new Context(f.getAbsolutePath());
    if (CD.debug) System.out.format("# Context creation RAF [%s]\n" + r.context);
    return r;
  }



  // *****************************************************************

  // for channel stuffs, we need to do this because connect is not
  // always blocking operation. and SocketChannel.getPort()
  // always be a *successful* connection to remote.
  // Thus, if we want to find out if sc.X() fails, at this point
  // we don't know what's the remote address is.
  
 Object around(SocketChannel sc, SocketAddress sa) : (call (boolean SocketChannel.connect(SocketAddress)) && target(sc) && args(sa) && !within(org.fi.*)) {

    Object tmp = proceed(sc, sa);

    // check if sc is successfully connected or not
    Socket s = sc.socket();
    int localPort = s.getLocalPort();
    if (localPort != 0) {
      if (CD.debug) {
        System.out.format("# Adding socket history (a) ... [%d] %s \n",
                          s.getLocalPort(), Util.getCassServerId());
      }
      Util.addCassSocketHistory(s);
    }


    return tmp;
  }



  // this is where we get the local port,
  // sc.finishConnect() in SocketIOWithTimeout.java
  after(SocketChannel sc) returning : (call (boolean SocketChannel.finishConnect()) && target(sc) && !within(org.fi.*) ) {

    // everytime the call is successful, we'll get the local port
    // now let's add this local port to our history
    Socket s = sc.socket();

    if (CD.debug) {
      System.out.format("# Adding socket history (b) ... [%d] %s \n",
                        s.getLocalPort(), Util.getCassServerId());
    }

    Util.addCassSocketHistory(s);
  }

  //PALLAVI : associate a port to a server id
  SocketChannel around(SocketAddress sa) : (call (SocketChannel SocketChannel.open(SocketAddress)) && args(sa) && !within(org.fi.*)) {
    SocketChannel sc = proceed(sa);

    // check if sc is successfully connected or not
    Socket s = sc.socket();
    int localPort = s.getLocalPort();
    if (localPort != 0) {
      if (CD.debug) {
        System.out.format("# Adding socket history (a) ... [%d] %s \n",
                          s.getLocalPort(), Util.getCassServerId());
      }
      Util.addCassSocketHistory(s);
    }
    return sc;
  }

  
  // *****************************************************************

  // creating context for output stream from channel
  // for any creation of new OutputStream+(...Socket...)
  // then we want to create context on behalf of this context.
  // If I don't understand the port Id ("Unknown-port") this means
  // this port is a created port on the fly ... let's check the socketHistory
  // MAKE sure build.xml involves the correct files from core/net/*.java
  Object around(Socket s, long to) : (call (OutputStream+.new(Socket,long)) && args(s, to) && !within(org.fi.*)) {
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

  Object around(Socket s, long to) : (call (InputStream+.new(Socket,long)) && args(s, to) && !within(org.fi.*) ) {
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

  //PALLAVI: keep the target socket port when reading into a ByteBuffer into 
  //the ByteBuffer
  int around(SocketChannel sc, ByteBuffer b) throws IOException: 
  (call (int SocketChannel.read(ByteBuffer) throws IOException) && 
   target(sc) && args(b) && !within(org.fi.*)) {
	Socket soc = sc.socket();
        int targetPort = soc.getPort();
        b.setContext(new Context(targetPort));	

        int tmp = proceed(sc, b);
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
  // XXX these are somehow hack ways to pass context
  // in my small test files the general way below works fine
  // but not when I try to run this in HDFS
  // I think for FOS, FIS, RAF,
  // it does not work when
  // 1) we use (..) as constructor arguments .. so we must
  //    specify the exact arg types, it works.
  // 2) we must convert the classes to ClassWC ourselves
  // 3) cannot use after(), must use around()
  // e.g. FIS fis = new FIS (f)  or RAF raf = new RAF (f, ..)
  // *********************************************
  Object around(File f)
    : !within(org.fi.*) && call (FileInputStream.new(File)) && args(f)  {
    Context ctx =
      (f.getContext() != null) ? f.getContext() :
      new Context(f.getAbsolutePath());
    return passContext(thisJoinPoint, (ClassWC)(proceed(f)), ctx);
  }

  Object around(File f)
    : !within(org.fi.*) && call (FileOutputStream.new(File)) && args(f) {
    Context ctx =
      (f.getContext() != null) ? f.getContext() :
      new Context(f.getAbsolutePath());
    return passContext(thisJoinPoint, (ClassWC)(proceed(f)), ctx);
  }

  Object around(File f, String s)
    : !within(org.fi.*) && call (RandomAccessFile.new(File,String)) && args(f,s) {
    Context ctx =
      (f.getContext() != null) ? f.getContext() :
      new Context(f.getAbsolutePath());
    return passContext(thisJoinPoint, (ClassWC)(proceed(f,s)), ctx);
  }


  // *********************************************
  // These are other ways (besides instantiations) where context
  // is created.
  // *********************************************
  Object around(ClassWC f)
    : !within(org.fi.*) &&
    call (FileChannel RandomAccessFile.getChannel()) && target(f) {
    return passContext(thisJoinPoint, (ClassWC)(proceed(f)), f.getContext());
  }


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

  //PALLAVI: transfer ByteBuffer's context when it is duplicated
  ByteBuffer around(ByteBuffer bs): 
  (call (ByteBuffer ByteBuffer.duplicate()) && 
   target(bs) && !within(org.fi.*)) {
	ByteBuffer bt = proceed(bs);
        Context bsCtx = bs.getContext();
        if(bsCtx != null){
		bt.setContext(bsCtx);
        }
        return bt;
  }


}



// *********************************************************************
// this is context passing for inextensible classes (hence non direct)
// perthis is undesired because we're adding something local (e.g.
// map(fd,context) to something global (the map), and we always map.put
// all the time, so the memory will increase all the time
// *********************************************************************
aspect contextPassingNonDirect perthis(blockedContext(ClassWC)) {

  public Object ctxKey;
  public Context ctxValue;

  // these are the pointcuts for context that is not passable
  // you define the "final" class that is returned
  pointcut blockedContext(ClassWC cwc)
    : !within(org.fi.*) && call (FileDescriptor ClassWC+.*(..)) && target(cwc);


  //
  Object around(ClassWC cwc) : blockedContext(cwc) {
    Object obj = proceed(cwc);
    ctxKey = obj;
    ctxValue = cwc.getContext();
    if (ctxValue == null)
      Util.FATAL(thisJoinPoint, "non-direct context passing is null");
    if (CD.debug) {
      System.out.println("# Context saved for non-direct ...");
    }
    return obj;
  }



  // this is what we should do under the control flow,
  // so this is another pointcut
  pointcut timeToPassContext(FileDescriptor tempArg)
    : !within(org.fi.*) && call (ClassWC+.new(..,FileDescriptor,..)) && args(tempArg);

  // time to pass the context, get the context from the map
  Object around(Object tempArg) : timeToPassContext(tempArg) {
    if (ctxValue == null)
      Util.FATAL(thisJoinPoint, "non-direct context is null");
    if (ctxKey != tempArg)
      Util.FATAL(thisJoinPoint, "try to pass non-direct context to multiple classes");

    Object temp = proceed(tempArg);
    ((ClassWC)temp).setContext(ctxValue);
    if (CD.debug) {
      System.out.println("# Context passing non-direct ...");
    }
    return temp;
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
