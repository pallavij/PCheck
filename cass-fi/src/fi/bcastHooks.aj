/**
 * Copyright (c) 2012,
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;


import org.aspectj.lang.Signature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.SourceLocation;

//PALLAVI: Cassandra related imports
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.net.Message;

//Hooks added by PALLAVI
public aspect bcastHooks {


   void around(MessagingService ms, Message m, InetAddress recvr):
	(call (public void MessagingService.sendOneWay(Message, InetAddress)) && target(ms) && args(m, recvr) && !within(org.fi.*)){

		String mtype = m.getMessageType();
		Util.MESSAGE("In sendOneWay...mtype = " + mtype);	

		if(mtype.equals("GS")){
			int rid = getNodeId(recvr);
			String ctxStr = "NetIO-Node-"+rid;
			
                        Context ctx = new Context(ctxStr);
			File f = new File("a");
			ClassWC cwc = (ClassWC) f;
			cwc.setContext(ctx);

			String mstr = m.toString();
			String verb = "";

			String separator = System.getProperty("line.separator");
			String[] mstrParts = mstr.split(separator);
			for(String p : mstrParts){
				if(p.startsWith("VERB:")){
					verb = p.substring(5);
				}
			}

			ctx.setExtraContext(verb);

			FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
                                  JoinPlc.BEFORE, JoinIot.READ,
                                  JoinExc.IO, JoinRbl.NO);
			FMClient.fiHookNetReadReq(fjp);
		}
		
		proceed(ms, m, recvr);

   }

   /****************
   before(Message m):
        (execution (public static void MessagingService.receive(Message)) && args(m) && !within(org.fi.*)){

		String mtype = m.getMessageType();

		if(mtype.equals("GS")){
			InetAddress sender = m.getFrom();
			int sid = getNodeId(sender);
			String ctxStr = "NetIO-Node-"+sid;
			
                        Context ctx = new Context(ctxStr);
			File f = new File("a");
			ClassWC cwc = (ClassWC) f;
			cwc.setContext(ctx);

			String mstr = m.toString();
			String verb = "";

			String separator = System.getProperty("line.separator");
			String[] mstrParts = mstr.split(separator);
			for(String p : mstrParts){
				if(p.startsWith("VERB:")){
					verb = p.substring(5);
				}
			}

			ctx.setExtraContext(verb);

			FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
                                  JoinPlc.BEFORE, JoinIot.READ,
                                  JoinExc.IO, JoinRbl.NO);
			FMClient.fiHookNetReadReq(fjp);
		}
   }
   **********************/
 
   private static int getNodeId(InetAddress ia){
	String host = ia.getHostAddress();
	System.out.println("host = " + host);
	
	int hlen = host.length();
	
	int nid = host.charAt(hlen-1) - '1';

	return nid;	
   }


   private static String getStack(){
        Thread t = Thread.currentThread();
        StackTraceElement[] st = t.getStackTrace();
	String stk = "";
        for(int i = st.length-1; i >= 0; i--){
                StackTraceElement se = st[i];
		stk += se.toString() + "\n"; 
        }
        return stk;
   }


}
