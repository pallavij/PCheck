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


// used to be FMCore

package org.fi;

import org.fi.*;

import java.io.*;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;

import org.fi.FMJoinPoint.*;


import org.apache.xmlrpc.common.TypeConverterFactoryImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;



// ******************************************************************
// Important note on FMServer usage:
// FMServer should be just a server that passes decision to its
// members.  For example, to decide whether we should do a fault
// injection or not, it just calls ManualFI .. later we want to do
// AutoFI for example.
// And also, to do modeling, we will use the Frog class
// ******************************************************************

// public class FMServer implements FMProtocol {
public class FMServer {

  
  //public static boolean debug = false;
  public static boolean debug = true;
  


  // private FrogServer frogServer;

  // the failure types
  // transient = exception
  // persistent = baddisk
  public static enum FailType {
    CRASH, BADDISK, EXCEPTION, RETFALSE, CORRUPTION, NETFAILURE, NONE;
      }

  // ***********************************************************
  public FMServer() {
    // also check frogHooks.aj is enabled!!
    // frogServer = new FrogServer();
  }


  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####                F M    S E R V E R   S E R V I C E             ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################


  // ***********************************************************
  private static synchronized FailType syncedSendContext(FMAllContext fac) {

    // 1. print all context
    if (debug) printAllContext(fac);

    // 2. mark that we "cover" this, although we haven't excercised this
    //    (only if we want to compare, but normally, let's jsut comment this out
    //     otherwise to many output in tmp-fi-out)
    Coverage.recordBeforeFilter(fac);
    
    // 3. we always go to the failure logic, but check if we should
    //    fail or not later, so that we can get the coverage point
    FailType ft = doFail(fac);

    // print and return
    if (debug) printFailure(fac, ft);
    
    return ft;
    
  }


  // ***********************************************************
  private static void printFailure(FMAllContext fac, FailType ft) {
    if (ft == FailType.NONE)
      return;
    Util.MESSAGE("Server: I'm failing this (see above) [ft:" + ft + "]");
  }

  // ***********************************************************
  private static void printAllContext(FMAllContext fac) {


    // print context
    System.out.println("# -----------------------------------------------");
    System.out.println("# Receive sendContext: [" + fac.ctx.getCutpointRandomId() + "]\n");


    System.out.println(fac.ctx);

    // print joinpoint
    System.out.println(fac.fjp);

    // print stack
    System.out.println(fac.fst);
    System.out.println("# -----------------------------------------------");

  }


  // ***********************************************************
  // doFail should consult the model checker,
  // but if we want to drive the model checker with specific
  // manual stuffs, then we also consult the doFail
  // ***********************************************************
  private static FailType doFail(FMAllContext fac) {

    FailType ft = FailType.NONE;
    
    // By default we want to call the fmlogic
    ft = FMLogic.run(fac);
    

    // or do manual stuffs
    // boolean isFail = manualFI.doFail_02(fac);

    // fail03: fail03 with more filters, very specific
    // boolean isFail = manualFI.doFail_03(fac);

    // this is sufficient for doFail 01
    // ft =  manualFI.doFail_01(fjp, ctx, fst);

    return ft;
  }

  // ***********************************************************
  /*
  public void sendFrogEvent(FMJoinPoint fjp, FMStackTrace fst, FrogEvent fev) {

    if (frogServer == null) {
      Util.WARNING("Please instantiate FrogServer first, see FMServer");
      return;
    }

    System.out.println("Receive sendFrogEvent: ");

    // print joinpoint
    System.out.println(fjp);

    // print stack
    System.out.println(fst);



    // System.out.format("++ haha %s \n", fev);
    frogServer.processFrogEvent(fjp, fst, fev);
  }
  */



  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####                    X M L       R P C                          ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################
  
  public final static int PORT = 16000;


  // ***********************************************************
  public void start() {

    try {
      System.out.println("- Starting XML-RPC Server...");
      WebServer webServer = new WebServer(PORT);

      XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      phm.addHandler("FMServer", org.fi.FMServer.class);
      xmlRpcServer.setHandlerMapping(phm);

      XmlRpcServerConfigImpl serverConfig =
        (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
      serverConfig.setEnabledForExtensions(true);
      serverConfig.setContentLengthOptional(false);

      System.out.println("- Waiting for incoming ...");

      webServer.start();

      System.out.println("- After webserver.start ...");

      //Reorder rt = new Reorder();
      ReorderReqs rt = new ReorderReqs();
      rt.start();
      System.out.println("- After rt.start ...");

    } catch (Exception e) {
      Util.EXCEPTION("Can't start server", e);
      Util.FATAL("Can't start server");
    }
  }




  // ************************************
  // THis is the send context via XML RPC
  // ************************************
  public int sendContext(int randId) {
    // System.out.format("- Received %d \n", randId);
    return syncedSendContext(randId);
  }

  // ************************************
  // THis is the send context of network messages via XML RPC
  // ************************************
  public int sendNetRdContext(int randId) {
    syncedNetRdSendContext(randId);
    return 0;
  }


  public int sendNetRdReqContext(int randId) {
    syncedNetRdReqSendContext(randId);
    return 0;
  }
  
  // ************************************
  // THis is the send context of local reads of network messages via XML RPC
  // ************************************
  public int sendLocalRdContext(int randId) {
    syncedLocalRdSendContext(randId, false);
    return 0;
  }
  
  
  // ************************************
  // THis is the send context of local reads of network messages that are added to recvqueue via offer()
  // ************************************
  public int sendLocalRdNoWaitContext(int randId) {
    syncedLocalRdSendContext(randId, true);
    return 0;
  }
  
  
  // ************************************
  // THis is the send context of poll events via XML RPC
  // ************************************
  public int sendPollContext(int randId) {
    syncedPollSendContext(randId);
    return 0;
  }
  
  // ************************************
  // THis is the send context of clearing of queue via XML RPC
  // ************************************
  public int sendClearQContext(int randId) {
    syncedClearQSendContext(randId);
    return 0;
  }
  
  // ************************************
  // THis is the send context of state change via XML RPC
  // ************************************
  public int sendStateChange(int randId) {
    syncedStateChangeSendContext(randId);
    return 0;
  }
  
  // ************************************
  // THis is the send context of sleep via XML RPC
  // ************************************
  public int sendSleep(int randId) {
    syncedSleepSendContext(randId);
    return 0;
  }
  

  // ************************************
  // THis is the send context of gossip state via XML RPC
  // ************************************
  public int sendGossipState(int randId) {
    syncedGossipStateSendContext(randId);
    return 0;
  }


  // ************************************
  // It is important for send context to be synchronized AND static !!
  // the reason is that web xml services always create a new FMServer
  // to process a request. So if we don't create this as static,
  // the synchronization is useless!
  // ************************************
  private static synchronized int syncedSendContext(int randId) {

    DataInputStream dis = Util.getRpcInputStream(randId);
    FMAllContext fac = new FMAllContext();

    try {
      fac.readFields(dis);
    } catch (Exception e) {
      Util.EXCEPTION("fac.readFields", e);
      Util.FATAL("fac.readFields");
    }

    FailType ft = syncedSendContext(fac);
    int rv = Util.failTypeToInt(ft);
    return rv;

  }

  
  // ************************************
  // This is the send context of sendLeaderElected via XML RPC
  // ************************************
  public int sendLeaderElected(int randId) {
    syncedLeaderElectedSendContext(randId);
    return 0;
  }
 
  // ************************************
  // This is the send context of sendLeaderElected via XML RPC
  // ************************************
  public int sendStartElection(int randId) {
    syncedStartElectionSendContext(randId);
    return 0;
  }

  private static synchronized void syncedLeaderElectedSendContext(int randId){
	  Util.MESSAGE("Going to call endElection");
	  Reorder.endElection();
  }

  private static synchronized void syncedStartElectionSendContext(int randId){
	//Another round of leader election has started 
	Util.doDelete(FMLogic.ELECTION_OVER);	
  }
  
  private static void syncedNetRdSendContext(int randId) {
     
    if(!Reorder.isExpStart() && Reorder.isNormalExec()) return;	  

    DataInputStream dis = Util.getRpcInputStream(randId);
   
    try{
	    int nodeIdVal = dis.readInt();
	    String nodeId = Util.getIdStrFromIntId(nodeIdVal);
	    int othrNodeIdVal = dis.readInt();
	    String othrNodeId = Util.getIdStrFromIntId(othrNodeIdVal);	

	    VoteContext vctx = new VoteContext(0, 0, 0, "");
	    vctx.leader = dis.readLong();
	    vctx.zxid = dis.readLong();
	    vctx.epoch = dis.readLong();
	    vctx.stack = dis.readUTF();
	    
	    String loc = dis.readUTF();
	    String srcloc = dis.readUTF();
	    
	    dis.close();	

	    MessageContext mctx = new MessageContext(nodeId, othrNodeId, vctx);
	    QueueContext q = new QueueContext(nodeId, loc);
	    Event e = new Event(Event.EType.NETREAD, mctx, q);
	    e.setSourceLoc(srcloc);

	    //if(!Reorder.isNormalExec(){
	    //	    updateWaitState(nodeIdVal, true); 
	    //} 
	    Object waitObj = new Object();
            Util.MESSAGE("Adding a message read event");
	    Reorder.addAndWait(e, waitObj);	  
	    addMsgToQueue(q, e); 
	    //System.out.println("Message from " + nodeId + " to " + othrNodeId + " has leader = " 
	    //		    + vctx.leader + " zxid = " + vctx.zxid + " epoch = " + vctx.epoch);
    }
    catch(Exception e){
	    Util.EXCEPTION("FMServer.syncedNetRdSendContext", e);
	    Util.FATAL("FMServer.syncedNetRdSendContext");
    }

  }

 
  // ***************************************************************************
 // PALLAVI: give deterministic zxids to packets in the client write workload
 // Assumes 3 nodes and 2 proposals
 // ***************************************************************************

 private static Map<String, Integer> realZxidToDetZxid = new HashMap<String, Integer>();

 private static String renamePacket(String pstr, int sender, int recvr){
	synchronized(realZxidToDetZxid){ 
		String newpstr = pstr; 
		String[] pstrParts = pstr.split(" ");
		String type = pstrParts[0];
		if(type.equals("PROPOSAL") || type.equals("COMMIT") || type.equals("ACK")){
			String zxid = pstrParts[1];
			zxid = zxid.trim();
			String newZxid = "-1"; 
			if(realZxidToDetZxid.containsKey(zxid)){
				newZxid = Integer.toString(realZxidToDetZxid.get(zxid));
			}
			else{
				int sz = realZxidToDetZxid.size();
				if(sz == 2){
					Util.MESSAGE("Cur ZXID = " + zxid + " Key-values in map: " + realZxidToDetZxid.entrySet());
				realZxidToDetZxid.clear();
				sz = 0;
			}
			realZxidToDetZxid.put(zxid, sz + 1);
			Util.MESSAGE("zxid = " + zxid + " is mapped to " + (sz+1));
			newZxid = Integer.toString(sz + 1);
		}
		newpstr = type + " " + newZxid;	
		}
		return newpstr;
	}
 }

  
  private static void syncedNetRdReqSendContext(int randId) {
     
    if(!Reorder.isExpStart() && Reorder.isNormalExec()) return;	  

    DataInputStream dis = Util.getRpcInputStream(randId);
   
    try{
	    int nodeIdVal = dis.readInt();
	    String nodeId = Util.getIdStrFromIntId(nodeIdVal);
	    
	    int othrNodeIdVal = dis.readInt();
	    String othrNodeId = Util.getIdStrFromIntId(othrNodeIdVal);	

	    String srcloc = dis.readUTF();
	    String packet = dis.readUTF();
	    dis.close();

            String newpacket = renamePacket(packet, othrNodeIdVal, nodeIdVal); 	    

	    RequestContext rctx = new RequestContext(nodeId, othrNodeId, newpacket);
	    Event e = new Event(Event.EType.NETREAD, rctx, null);
	    e.setSourceLoc(srcloc);

	    Object waitObj = new Object();
            Util.MESSAGE("Adding a message read event");
	    ReorderReqs.addAndWait(e, waitObj);	  
    }
    catch(Exception e){
	    Util.EXCEPTION("FMServer.syncedNetRdSendContext", e);
	    Util.FATAL("FMServer.syncedNetRdSendContext");
    }

  }

  private static void syncedLocalRdSendContext(int randId, boolean noWait) {
  
    if(!Reorder.isExpStart() && Reorder.isNormalExec()) return;	  
	  
    DataInputStream dis = Util.getRpcInputStream(randId);
   
    try{
	    int senderIdVal = dis.readInt();
	    String senderId = Util.getIdStrFromIntId(senderIdVal);;
	    int nodeIdVal = dis.readInt();
	    String nodeId = Util.getIdStrFromIntId(nodeIdVal);
	    String loc = dis.readUTF();
	    String srcloc = dis.readUTF();
	    long leader = dis.readLong(); 
	    long zxid = dis.readLong();
	    long epoch = dis.readLong();    
	    dis.close();	

	    QueueContext q = new QueueContext(nodeId, loc);
	    addMsgToQueue(q, null); 
	    
	    VoteContext vctx = new VoteContext(leader, zxid, epoch, null);

	    MessageContext mctx = new MessageContext(nodeId, senderId, vctx);

	   
	    //if(!noWait){ 
		    Event e = new Event(Event.EType.LOCALREAD, mctx, q);
		    e.setSourceLoc(srcloc);
		    History.recordLocalEvt(e);
	    //}

    }
    catch(Exception e){
	    Util.EXCEPTION("FMServer.syncedLocalRdSendContext", e);
	    Util.FATAL("FMServer.syncedLocalRdSendContext");
    }

  }


  private static void syncedPollSendContext(int randId) {
    
    if(!Reorder.isExpStart() && Reorder.isNormalExec()) return;	  
   
    DataInputStream dis = Util.getRpcInputStream(randId);
   
    try{
	    int nodeIdVal = dis.readInt();
	    String nodeId = Util.getIdStrFromIntId(nodeIdVal);
	    String loc = dis.readUTF();
	    String srcloc = dis.readUTF();
	    String stack = dis.readUTF();
	    //Util.MESSAGE("Stack read is " + stack);
	    dis.close();
		
	    QueueContext q = new QueueContext(nodeId, loc);
	    Event e = new Event(Event.EType.POLL, null, q, stack);
	    e.setSourceLoc(srcloc);

	    Object waitObj = new Object();
	    Reorder.addAndWait(e, waitObj);

	    removeMsgFromQueue(q, e);
	    	    
    }
    catch(Exception e){
	    Util.EXCEPTION("FMServer.syncedPollSendContext", e);
	    Util.FATAL("FMServer.syncedPollSendContext");
    }

  }


  private static void syncedClearQSendContext(int randId) {
    
    if(!Reorder.isExpStart() && Reorder.isNormalExec()) return;	  
   
    DataInputStream dis = Util.getRpcInputStream(randId);
   
    try{
	    int nodeIdVal = dis.readInt();
	    String nodeId = Util.getIdStrFromIntId(nodeIdVal);
	    String loc = dis.readUTF();
	    String srcloc = dis.readUTF();
	    dis.close();
		
	    QueueContext q = new QueueContext(nodeId, loc);
	    clearQueue(q);
	    	    
    }
    catch(Exception e){
	    Util.EXCEPTION("FMServer.syncedClearQSendContext", e);
	    Util.FATAL("FMServer.syncedClearQSendContext");
    }

  }


  private static void syncedStateChangeSendContext(int randId) {
    
    if(!Reorder.isExpStart() && Reorder.isNormalExec()) return;	  
   
    DataInputStream dis = Util.getRpcInputStream(randId);
   
    try{
	    String srcloc = dis.readUTF();
	    int sid = dis.readInt();
	    long leader = dis.readLong();
	    long zxid = dis.readLong();
	    dis.close();

	    AState.setChangedVCtx(sid, leader, zxid);

	    VoteContext vc = new VoteContext(leader, zxid, -1, null);
	    MessageContext mc = new MessageContext(Util.getIdStrFromIntId(sid), null, vc);
	    Event e = new Event(Event.EType.STATECHANGE, mc, null);
	    e.setSourceLoc(srcloc);
	    History.recordLocalEvt(e);

    }
    catch(Exception e){
	    Util.EXCEPTION("FMServer.syncedStateChangeSendContext", e);
	    Util.FATAL("FMServer.syncedStateChangeSendContext");
    }

  }
  
  
  private static void syncedSleepSendContext(int randId) {
    
    if(!Reorder.isExpStart() && Reorder.isNormalExec()) return;	  
   
    DataInputStream dis = Util.getRpcInputStream(randId);
   
    try{
	    String srcloc = dis.readUTF();
	    String stack = dis.readUTF();
	    int sid = dis.readInt();
	    dis.close();
	   
	    Event e = new Event(Event.EType.SLEEP, null, null, stack+"\n");
	    e.setSourceLoc(srcloc);
	    e.setNodeId(Util.getIdStrFromIntId(sid));
	    
	    Object waitObj = new Object();
	    Reorder.addAndWait(e, waitObj);
    }
    catch(Exception e){
	    Util.EXCEPTION("FMServer.syncedSleepSendContext", e);
	    Util.FATAL("FMServer.syncedSleepSendContext");
    }
  
  }    

   //PALLAVI: Used while testing message orders in ZooKeeper

   private static Map<Integer, Boolean> waitStates = new HashMap<Integer, Boolean>();

   public static boolean allWaiting(){
	   synchronized(waitStates){
		   for(int i = 0; i < FMLogic.nZkServers; i++){
			   Boolean wState = waitStates.get(i+1);
			   if((wState == null) || (wState == false)){
				   Util.MESSAGE("Server " + (i+1) + " is not waiting yet");
				   return false;
			   }
		   }
		   return true;
	   }
   }

  public int sendNodeWaitState(int randId) {
         updateWaitState(randId, false);
	 return 0;
  }

   private static void updateWaitState(int id, boolean isSId) {
	 if(isSId){
		synchronized(waitStates){
			Util.MESSAGE("Setting wait state of server " + id + " to true");
			waitStates.put(id, true);
		}
		return;
	 }	 


	 DataInputStream dis = Util.getRpcInputStream(id);

	 try{
		int sid = dis.readInt();
		dis.close();

		Util.MESSAGE("Setting wait state of server " + sid + " to true");
		
		synchronized(waitStates){
			waitStates.put(sid, true);
		}
	 }
	 catch(Exception e){
		Util.EXCEPTION("FMServer.updateWaitState", e);
	        Util.FATAL("FMServer.updateWaitState");

	 }
   }	 


   public static void serverNoLongerWaiting(int sid){
	 synchronized(waitStates){
		 Util.MESSAGE("Resetting wait state of server " + sid + " to false");
		 waitStates.put(sid, false);
	 }
   }

   public static void clearWaitStates(){
	synchronized(waitStates){
		Util.MESSAGE("Resetting wait states of all servers");
		for(int i = 0; i < FMLogic.nZkServers; i++){
			waitStates.put(i, false);
		}
	}	
   }

   public static void removeWaitStates(){
	synchronized(waitStates){
		waitStates.clear();
	}	
   } 


   //PALLAVI: Keeping track of messages in queues

   private static Map<QueueContext, Integer> queueToNumElems = new HashMap<QueueContext, Integer>();
   private static Map<QueueContext, LinkedList<Event>> queueToElems = new HashMap<QueueContext, LinkedList<Event>>();	

   public static int getNumMsgsInQueue(QueueContext qctx){
	synchronized(queueToNumElems){   
		Integer n = queueToNumElems.get(qctx);	
		if(n == null)
			return 0;
		return n;
	}
   }

   public static void addMsgToQueue(QueueContext qctx, Event e){
	Util.MESSAGE("Adding message to queue " + qctx.toString());
	int n = getNumMsgsInQueue(qctx);
	synchronized(queueToNumElems){   
		queueToNumElems.put(qctx, (n+1));
	}
	AState.setChangedQueue(qctx, (n+1));
	/*************************************
	synchronized(queueToElems){
		LinkedList<Event> l = queueToElems.get(qctx);
		if(l == null){
			l = new LinkedList<Event>();
			queueToElems.put(qctx, l);
		}
		l.addLast(e);
	}
	*************************************/
   }

   public static void removeMsgFromQueue(QueueContext qctx, Event e){
	int n = getNumMsgsInQueue(qctx);
	synchronized(queueToNumElems){   
		if(n != 0){
			Util.MESSAGE("Removing message from queue " + qctx.toString());
			if(n > 1){
				queueToNumElems.put(qctx, (n-1));
			}	
			else{
				queueToNumElems.remove(qctx);
			}
		}
	}
	
	AState.setChangedQueue(qctx, (n-1));

	/*******************************
	synchronized(queueToElems){
		if((n > 0) && (e != null) && e.isPollEvent()){
			LinkedList<Event> l = queueToElems.get(qctx);
			Event evt = l.removeFirst();
			if(evt != null){
				Util.MESSAGE("Removed event is " + evt);
			}
			else{
				Util.MESSAGE("Removed event is null");
			}
		}
	}
	*******************************/
   } 
   
   public static void clearQueue(QueueContext qctx){
	Util.MESSAGE("Removing all messages from queue " + qctx.toString());
	synchronized(queueToNumElems){   
		queueToNumElems.remove(qctx);
	}	
	AState.setChangedQueue(qctx, 0);
   }

   public static boolean areQueuesEmpty(){
	synchronized(queueToNumElems){   
		boolean isEmpty = queueToNumElems.isEmpty();
		if(!isEmpty){
			Set<QueueContext> qs = queueToNumElems.keySet();
			for(QueueContext q : qs){
				Integer n = queueToNumElems.get(q);
				Util.MESSAGE("Queue (" + q.nodeId + "," + q.srcLn + ") is not empty. It has " + n + " elements.");
			}
		}
		else{
			Util.MESSAGE("All queues are empty.");
		}	
		return isEmpty;
        }
   }

   public static boolean isEmpty(QueueContext q){
   	int n = getNumMsgsInQueue(q);
	return (n == 0);
   }
   
   public static void clearQueueToNumElems(){
	synchronized(queueToNumElems){   
		queueToNumElems.clear();
        }
	AState.clearQueues();
   }




  //Used by cassandra (keep track of which node is done with sending all gossip packets to others)

  private static Set<Integer> doneGossiping = new HashSet<Integer>();
  private static boolean gossipPktsSent = false;

  public static synchronized void resetDoneGossiping(){
	doneGossiping = new HashSet<Integer>();
  }
   
  public static synchronized boolean allGossipPktsSent(){
	return gossipPktsSent;
  }

  public static synchronized void setAllGossipPktsSent(){
	gossipPktsSent = true;
  }

  public static synchronized void resetAllGossipPktsSent(){
	gossipPktsSent = false;
  }
  
	
   private static synchronized void syncedGossipStateSendContext(int randId) {
    
    if(!Reorder.isExpStart() && Reorder.isNormalExec()) return;	  
   
    DataInputStream dis = Util.getRpcInputStream(randId);
   
    try{
	    int sid = dis.readInt();
	    dis.close();

	    doneGossiping.add(sid);

	    Util.MESSAGE("Server " + sid + " is now done sending gossip packets...size of doneGossiping is " + doneGossiping.size());

	    if(doneGossiping.size() == FMLogic.nCassServers){
	    	//Reorder.setNormalExec();
		setAllGossipPktsSent();	
	    }
	   
    }
    catch(Exception e){
	    Util.EXCEPTION("FMServer.syncedSleepSendContext", e);
	    Util.FATAL("FMServer.syncedSleepSendContext");
    }
  
  }    











  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####                 H A D O O P    R P C                          ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################


  /*

  public final static String bindAddr = "localhost";

  // ***********************************************************
  public long getProtocolVersion(String protocol, long clientVersion)
  throws IOException {
  return FMProtocol.versionID;
  }

  // ***********************************************************
  public void initialize() throws InterruptedException {
  System.out.println("FMServer: Init ...");
  Configuration conf = new Configuration();
  Server server;
  try {
  server = RPC.getServer(this, bindAddr, port, conf);
  // this runs forever ...
  server.start();
  server.join();
  System.out.println("FMServer: After server join ...");
  } catch (IOException e) {
  System.out.println("FMServer: Exception ...");
  e.printStackTrace();
  }
  }


  // ***********************************************************
  public FailType sendContext(FMJoinPoint fjp,
  FMContext ctx, FMStackTrace fst) {
  FMAllContext fac = new FMAllContext(fjp, ctx, fst);
  return syncedSendContext(fac);
  }

  */

}
