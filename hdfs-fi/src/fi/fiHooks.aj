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
import org.fi.FMServer.*;
import org.fi.FMJoinPoint.*;


import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.nio.channels.*;
import java.nio.*;

import org.aspectj.lang.Signature; // include this for Signature, etc!
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.SourceLocation;



public aspect fiHooks {

  Object around(ClassWC c) throws IOException
    : ((!within(org.fi.*) && target(c)) &&
       (// buffered writes:
	call (* OutputStream+.write*(..)             throws IOException) ||
	call (* DataOutputStream+.write*(..)        throws IOException) ||
	// non-buffered writes:
	call (* Channel+.write*(..)            throws IOException)  
       )
       ) {

    Context ctx = c.getContext();

    if(c instanceof SocketChannel){
	SocketChannel sc = (SocketChannel)c;
	Socket s = sc.socket();
	ctx = new Context(Util.getNetIOContextFromPort(s.getPort()));	
    }

    if((ctx != null) && (new File(FMLogic.EXP_RUN_FLAG)).exists()){
	String ctxStr = ctx.getTargetIO();
	if((ctxStr != null) && Util.isNetIO(ctxStr)){
		//System.out.println("ctxStr = " + ctxStr);
		String rIdStr = getNodeIdStr(ctxStr);
        	Context newCtx = new Context(rIdStr);
		
                File f = new File("a");
		ClassWC newCwc = (ClassWC) f;
		newCwc.setContext(newCtx);

	        FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, newCwc, null,
			  JoinPlc.BEFORE, JoinIot.WRITE,
			  JoinExc.IO, JoinRbl.NO);

	        FMClient.fiHookNetReadReq(fjp);
	}
    }

    return proceed(c);
  }


  String getNodeIdStr(String ctxStr){
	String[] ctxStrParts = ctxStr.split(":");

	String rid = ctxStrParts[0];
	if(rid.startsWith("NetIO-")){
		rid = rid.substring((new String("NetIO-")).length());
		if(rid.startsWith("Client")){
			rid = "0";
		}
		else if(rid.startsWith("NameNode")){
			rid = "1";
		}
		else if(rid.startsWith("DataNode")){
			String dnodeStr = new String("DataNode-");
			int dnodeLen = dnodeStr.length();
			rid = (new Integer((Integer.parseInt(rid.substring(dnodeLen, dnodeLen + 1))) + 1)).toString();
		}
		else{
			rid = "-1";
		}
	}

	return "NetIO-Node-"+rid;
  }


  Object around (ClassWC c) throws IOException
    : ((!within(org.fi.*) && args(c)) &&
       (call (* ByteArrayOutputStream+.writeTo(OutputStream) throws IOException) ||
        call (* OutputStream+.writeTo(OutputStream+) throws IOException)
        )
       ) {
    Context ctx = c.getContext();
    if((ctx != null) && (new File(FMLogic.EXP_RUN_FLAG)).exists()){
	String ctxStr = ctx.getTargetIO();
	if((ctxStr != null) && Util.isNetIO(ctxStr)){
		//System.out.println("ctxStr = " + ctxStr);
		String rIdStr = getNodeIdStr(ctxStr);
		Context newCtx = new Context(rIdStr);
		
                File f = new File("a");
		ClassWC newCwc = (ClassWC) f;
		newCwc.setContext(newCtx);

	        FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, newCwc, null,
			  JoinPlc.BEFORE, JoinIot.WRITE,
			  JoinExc.IO, JoinRbl.NO);
		
                //System.out.println("In fiHooks...2...ctxStr = " + ctxStr);
                //System.out.println("In fiHooks...2...rIdStr = " + rIdStr);

	        FMClient.fiHookNetReadReq(fjp);
	}
    }

    return proceed(c);
  }


}
