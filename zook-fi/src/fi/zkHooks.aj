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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import org.aspectj.lang.Signature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.SourceLocation;

//zookeeper specific import
import org.apache.zookeeper.server.quorum.QuorumCnxManager.Message;
import org.apache.zookeeper.server.quorum.FastLeaderElection.Notification;
import org.apache.zookeeper.server.quorum.FastLeaderElection;
import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.Vote;


//Hooks added by PALLAVI
public aspect zkHooks {

  Object around(SocketChannel sc, ByteBuffer b) throws IOException: 
  (call (* SocketChannel.write(ByteBuffer) throws IOException) && 
   target(sc) && args(b) && !within(org.fi.*)) {

    ByteBuffer nb = b.duplicate();    
    int c = nb.capacity();

    if (c >= 32){
            int len = nb.getInt();
	    int state = nb.getInt();
	    long leader = nb.getLong();
	    long zxid = nb.getLong();
	    long epoch = nb.getLong();
	    byte[] st = new byte[nb.remaining()];
            nb.get(st);

            String stack = null;
            try{
		stack = new String(st, "ASCII");
            }
            catch(UnsupportedEncodingException e){
            	stack = new String(st);   
            }
	    //taking the top-most element of the stack off
	    int fstNewlnIdx = stack.indexOf('\n');
            stack = stack.substring(fstNewlnIdx+1);


	    if(stack.contains("LeaderElection")){
		    //System.out.println("In zkHooks.aj....leader = " + leader + " zxid = " + zxid + " epoch = " + epoch);
		    //System.out.println("In zkHooks.aj....stack = " + stack);

		    Socket soc = sc.socket();
		    InetAddress targetAddr = soc.getInetAddress();
		    int targetPort = soc.getPort();
                    
	            int tServerId = Util.getZkNodeFromPort(targetPort);

		    String ctxStr = "NetIO-Node-"+tServerId+":"+targetPort;
	            if(tServerId == 0)
			ctxStr = "NetIO-Node-Unknown:"+targetPort;

		    //System.out.println("In zkHooks.aj...ctxStr is " + ctxStr);	

                    Context ctx = new Context(ctxStr);

		    File f = new File("a");
		    ClassWC cwc = (ClassWC) f;
		    cwc.setContext(ctx);
		    VoteContext vc = new VoteContext(leader, zxid, epoch, stack); 
		    ctx.setExtraContext(vc); 
		    FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
						  JoinPlc.BEFORE, JoinIot.WRITE,
						  JoinExc.IO, JoinRbl.NO);
		    

		    Object obj = FMClient.fiHookIox(fjp);
		    if (obj != null) return obj;
		    obj = proceed(sc, b);
		    //fjp.setAfter(obj);
		    //return FMClient.fiHookIox(fjp);
                    return obj;
            }
	    else{
		    Object tmp = proceed(sc, b);
		    return tmp;
	    }
    }
    else{
	    Object tmp = proceed(sc, b);
	    return tmp;
    }

  }

  void around(BlockingQueue q, Message m): 
  (call (void BlockingQueue.put(Object)) && 
         target(q) && args(m) && !within(org.fi.*)) {
        if(m != null){
		ByteBuffer bf = m.getBuffer();
                long senderId = m.getSid();
	        
                ByteBuffer nb = bf.duplicate();    
	        int c = nb.capacity();

                if(c >= 28){
		    int state = nb.getInt();
		    long leader = nb.getLong();
		    long zxid = nb.getLong();
		    long epoch = nb.getLong();
		    byte[] st = new byte[nb.remaining()];
		    nb.get(st);
		   
                    String stack = null; 
		    try{
			stack = new String(st, "ASCII");
                    }
                    catch(UnsupportedEncodingException e){
			stack = new String(st);
                    }
		    //taking the top-most element of the stack off
		    int fstNewlnIdx = stack.indexOf('\n');
		    stack = stack.substring(fstNewlnIdx+1);

		    //System.out.println("In zkHooks.aj...reading of a message...stack is " + stack);
		    
		    if(stack.contains("LeaderElection")){
			    long tServerId = senderId;
			    //PALLAVI: cannot capture the port when the message was sent by the same server
			    int targetPort = 0;
			    Context bfCtx = bf.getContext();
                            if(bfCtx != null){
				targetPort = bfCtx.getPort();
			    }			   
 
		            String ctxStr = "NetIO-Node-"+tServerId+":"+targetPort;
			    Context ctx = new Context(ctxStr);

			    File f = new File("a");
			    ClassWC cwc = (ClassWC) f;
			    cwc.setContext(ctx);
			    VoteContext vc = new VoteContext(leader, zxid, epoch, stack); 
			    ctx.setExtraContext(new Pair<VoteContext, BlockingQueue>(vc, q)); 
			    FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
							  JoinPlc.BEFORE, JoinIot.READ,
							  JoinExc.IO, JoinRbl.NO);
			   
                            FMClient.fiHookNetRead(fjp); 
			    //PALLAVI: which hook do we call here? We cannot throw IOException here
			    //FMClient.fiHookNox(fjp);
			    proceed(q, m);
			    //fjp.setAfter(null);
			    //FMClient.fiHookNox(fjp);
                    }
                    else{
			    proceed(q, m);
                    }
		
                }
                else{
			proceed(q, m);
                }
	}
        else{
		proceed(q, m);
        }
  } 

 
  void around(BlockingQueue q, Notification n): 
  (call (void BlockingQueue.put(Object)) && 
         target(q) && args(n) && !within(org.fi.*)) {
	hookForLocalPut(q, n, thisJoinPoint, false);
	proceed(q, n);		
   }


  boolean around(BlockingQueue q, Notification n): 
  (call (boolean BlockingQueue.offer(Object)) && 
         target(q) && args(n) && !within(org.fi.*)) {
	hookForLocalPut(q, n, thisJoinPoint, true);
	boolean obj = proceed(q, n);		
	return obj;
  }

  void hookForLocalPut(BlockingQueue q, Notification n, JoinPoint jp, boolean noWait){
        if(n != null){
		File f = new File("a");
	    	ClassWC cwc = (ClassWC) f;
		Context ctx = new Context("NetIO-Node-" + n.sid);
	        
		VoteContext vc = new VoteContext(n.leader, n.zxid, n.epoch, null); 
                ctx.setExtraContext(new Pair<VoteContext, BlockingQueue>(vc, q));
	    	
		cwc.setContext(ctx);
	
	    	FMJoinPoint fjp = new FMJoinPoint(jp, cwc, null,
					  JoinPlc.BEFORE, JoinIot.READ,
					  JoinExc.IO, JoinRbl.NO);
		FMClient.fiHookLocalRead(fjp, noWait);
	}
  }


  Object around(BlockingQueue q, long t, TimeUnit u) throws InterruptedException: 
  (call (* BlockingQueue.poll(long, TimeUnit) throws InterruptedException) && 
   	target(q) && args(t, u) && !within(org.fi.*)) {

	    boolean isPollInFLE = ((thisJoinPoint.getSourceLocation()).getFileName()).contains("FastLeaderElection");
	    boolean isPollInSendTh = isInStack("WorkerSender"); 

	    if(!isPollInFLE || isPollInSendTh){
		Object res = proceed(q, t, u);
		return res;
	    }

	    File f = new File("a");
	    ClassWC cwc = (ClassWC) f;
	    Context ctx = new Context("");
            ctx.setExtraContext(q);
	    cwc.setContext(ctx);
	    FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
					  JoinPlc.BEFORE, JoinIot.NONE,
					  JoinExc.NONE, JoinRbl.NO);
	    long newT = FMClient.fiHookPoll(fjp);
            Object res = proceed(q, newT, u);		
  	    
            /****************
	    if(newT == 0){
		if(res != null)
			Util.MESSAGE("Read something using poll that was paused.");
		else
			Util.MESSAGE("Did NOT read anything using poll that was paused.");
	    }
            ***************/
            return res;
  }


  private static boolean isInStack(String s){
	Thread t = Thread.currentThread();
	StackTraceElement[] st = t.getStackTrace();
	for(int i = st.length-1; i >= 0; i--){
		StackTraceElement se = st[i];
		if((se.getClassName()).contains(s))
			return true;
	}
	return false;
  }


  void around(BlockingQueue q):
  (call (void BlockingQueue.clear()) && target(q) && !within(org.fi.*)){
	    boolean isPollInFLE = ((thisJoinPoint.getSourceLocation()).getFileName()).contains("FastLeaderElection");
	    boolean isPollInSendTh = isInStack("WorkerSender"); 

	    if(!isPollInFLE || isPollInSendTh){
		proceed(q);
	    }
	    
	    File f = new File("a");
	    ClassWC cwc = (ClassWC) f;
	    Context ctx = new Context("");
            ctx.setExtraContext(q);
	    cwc.setContext(ctx);

	    proceed(q);
	    FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
					  JoinPlc.BEFORE, JoinIot.NONE,
					  JoinExc.NONE, JoinRbl.NO);
	    FMClient.fiHookClearQueue(fjp);
 }

 Object around():
 (call (LinkedBlockingQueue.new()) && !within(org.fi.*)){
 	    Object obj = proceed();
	    File f = new File("a");
	    ClassWC cwc = (ClassWC) f;
	    FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, obj,
                                      JoinPlc.BEFORE, JoinIot.NONE,
                                      JoinExc.NONE, JoinRbl.NO);
            FMClient.fiHookCreateQueue(fjp);
	    return obj;
 }

 Object around():
 (call (ArrayBlockingQueue.new(*)) && !within(org.fi.*)){
 	    Object obj = proceed();
	    File f = new File("a");
	    ClassWC cwc = (ClassWC) f;
	    FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, obj,
                                      JoinPlc.BEFORE, JoinIot.NONE,
                                      JoinExc.NONE, JoinRbl.NO);
            FMClient.fiHookCreateQueue(fjp);
	    return obj;
 }


 /*******************************
 void around(Vote v):
 (call (void QuorumPeer.setCurrentVote(Vote)) && args(v) && !within(org.fi.*)){
	proceed(v);
	File f = new File("a");
	ClassWC cwc = (ClassWC) f;
	Context ctx = new Context("");
	VoteContext vc = new VoteContext(v.id, v.zxid, 0, null); 
        ctx.setExtraContext(vc);
	cwc.setContext(ctx);
	
	FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
			      JoinPlc.AFTER, JoinIot.NONE,
			      JoinExc.NONE, JoinRbl.NO);
	FMClient.fiHookStateChange(fjp);
	return;
 }
 *******************************/

 
void around(long l, long z):
 (call (void FastLeaderElection.updateProposal(long, long)) && args(l, z) && !within(org.fi.*)){
	//proceed(l, z);
	File f = new File("a");
	ClassWC cwc = (ClassWC) f;
	Context ctx = new Context("");
	VoteContext vc = new VoteContext(l, z, 0, null); 
        ctx.setExtraContext(vc);
	cwc.setContext(ctx);
	
	FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
			      JoinPlc.BEFORE, JoinIot.NONE,
			      JoinExc.NONE, JoinRbl.NO);
	FMClient.fiHookStateChange(fjp);
	proceed(l, z);
	return;
 }

void around(long l):
(call (void Thread.sleep(long)) && args(l) && withincode(void org.apache.zookeeper.server.quorum.Leader.lead())){
        File f = new File("a");
	ClassWC cwc = (ClassWC) f;
	Context ctx = new Context("");
	cwc.setContext(ctx);
	
        FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
			      JoinPlc.BEFORE, JoinIot.NONE,
			      JoinExc.NONE, JoinRbl.NO);
	System.out.println("In zkHooks...going to call fiHookSleep");
	FMClient.fiHookSleep(fjp);
	//proceed(100);
	proceed(20);
}


after():
(call (void leaderIsNowLeading()) && !within(org.fi.*)){
        FMClient.fiHookLeaderElected();
}

before():
(execution (* lookForLeader()) && !within(org.fi.*)){
	FMClient.fiHookStartElection();
} 
 
 /***********
  void around(Socket s, int to) throws SocketException: 
  (call (void Socket.setSoTimeout(int) throws SocketException) && 
   	target(s) && args(to) && !within(org.fi.*)) {
        System.out.println("In zkHooks.aj...in setSoTimeout");
        if(to > 0){
	        FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, s, null,
					  JoinPlc.BEFORE, JoinIot.NONE,
					  JoinExc.IO, JoinRbl.NO);
		System.out.println("In zkHooks.aj...going to call fiHookTimeout in setSoTimeout");
		FMClient.fiHookTimeout(fjp);	
		proceed(s, to);
        }
	else{
		proceed(s, to);
	}
  }

  void around(Object obj, long to) throws InterruptedException:
  (call (void Object.wait(long) throws InterruptedException) && 
   	target(obj) && args(to) && !within(org.fi.*)) {
        System.out.println("In zkHooks.aj...in wait");
	if(to > 0){
	        FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, null, null,
					  JoinPlc.BEFORE, JoinIot.NONE,
					  JoinExc.NONE, JoinRbl.NO);
		System.out.println("In zkHooks.aj...going to call fiHookTimeout in wait");
		FMClient.fiHookTimeout(fjp);	
		proceed(obj, to);

	}
	else{
		proceed(obj, to);
	}
  }

  void around(Object obj, long to, int nanos) throws InterruptedException:
  (call (void Object.wait(long, int) throws InterruptedException) && 
   	target(obj) && args(to) && args(nanos) && !within(org.fi.*)) {
        System.out.println("In zkHooks.aj...in wait with nanos");
	if((to > 0) || (nanos > 0)){
	        FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, null, null,
					  JoinPlc.BEFORE, JoinIot.NONE,
					  JoinExc.NONE, JoinRbl.NO);
		System.out.println("In zkHooks.aj...going to call fiHookTimeout in wait with nanos");
		FMClient.fiHookTimeout(fjp);	
		proceed(obj, to, nanos);
	}
	else{
		proceed(obj, to, nanos);
	}
  }
  ***********/

}

