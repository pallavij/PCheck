//
package org.fi;

// ************************************ org fi
import org.fi.*;
import org.fi.FMServer.FailType;
import org.fi.FMJoinPoint.*;


// ************************************ jol
import jol.core.JolSystem;
import jol.core.Runtime;
import jol.types.basic.BasicTupleSet;
import jol.types.basic.Tuple;
import jol.types.basic.TupleSet;
import jol.types.exception.JolRuntimeException;
import jol.types.exception.UpdateException;
import jol.types.table.TableName;
import jol.types.table.Table.Callback;
import jol.types.table.Table;

// ************************************ aspect
import org.aspectj.lang.Signature; // include this for Signature, etc!
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.SourceLocation;


// ************************************ java
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.lang.Thread;
import java.lang.StackTraceElement;
import java.net.URL;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

// ************************************ XML RPC
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// FMClient send to FMServer a list of String
// actually it will go to FMServer first
// the list of String is basically what the event should
// schedule

public class FMClient {


  public static final int FAAS_SYSTEM_EXIT_STATUS = 13;



  // #####################################################################
  // #####################################################################
  // ##                                                                 ##
  // ##              M A I N   E N T R Y   P O I N T S                  ##
  // ##                                                                 ##
  // #####################################################################
  // #####################################################################


  // ******************************************************
  // Each hook has some important properties:
  //  - Return object:
  //      The hook must return an object, this is important
  //      if we want to process corruption. The aspect always
  //      uses this returned value. By default the object is
  //      the object we get from proceed().
  //  - Throws exception:
  //      For exceptions that we want to exercise such as
  //      IOException and FileNotFoundException, then we must
  //      specify explicit hooks for that, so that these special
  //      hooks can return the exceptions as specified.
  //  - FailType.EXCEPTION
  //      Fail type exception is processed here. Other fail types
  //      should be proceed deeper in a centralized location.
  //      FailType.Corruption is alredy processed by now, and
  //      the resulting corruption should be in JoinRov
  //  - FailType.BadDisk
  // ******************************************************
  public static Object fiHookIox(FMJoinPoint fjp)  throws IOException {
    FailType ft = tryFiHook(fjp);
    if (fjp.getIox() != null) throw fjp.getIox();
    return fjp.getJoinRov();
  }

  // ******************************************************
  public static Object fiHookFnfx(FMJoinPoint fjp) throws FileNotFoundException {
    FailType ft = tryFiHook(fjp);
    if (fjp.getFnfx() != null) throw fjp.getFnfx();
    return fjp.getJoinRov();
  }

  // ******************************************************
  public static Object fiHookNox(FMJoinPoint fjp) {
    Util.MESSAGE("In fiHookNox");
    FailType ft = tryFiHook(fjp);
    return fjp.getJoinRov();
  }

  private static Set<BlockingQueue> recvQueues = new HashSet<BlockingQueue>();

  public static void clearRecvQueues(){
	synchronized(recvQueues){
		recvQueues.clear();
	}
  }

  public static boolean queuesAllEmpty(){
	synchronized(recvQueues){  
		for(BlockingQueue q : recvQueues){
			if(!q.isEmpty()){
				return false;	
			}
		}
		return true;
	}
  }

  public static void addQueue(BlockingQueue q){
	synchronized(recvQueues){  
		recvQueues.add(q);
	}	
  }

  public static boolean isQueuePresent(BlockingQueue q){
	synchronized(recvQueues){  
		return recvQueues.contains(q);
	}	
  }


  // ******************************************************
  // PALLAVI: for reordering messages
  // ******************************************************
  public static void fiHookNetRead(FMJoinPoint fjp) {
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		ClassWC cwc = fjp.getClassWC();
		Context c = cwc.getContext();

		Object extra = c.getExtraContext();
		
		if((extra != null) && (extra instanceof Pair)){
			Pair vCtxAndQueue = (Pair)extra;
			VoteContext vctx = (VoteContext)vCtxAndQueue.fst();
			BlockingQueue q = (BlockingQueue)vCtxAndQueue.snd();
			//addQueue(q);
			String loc = getLoc(q);
		        String srcloc = getSourceLoc(fjp); 	
			waitUntilMyTurnNetRd(fjp, vctx, loc, srcloc);
			//System.out.println("Going to put a message into queue...loc of queue is " + loc);
		}	
		
		return;	
	}
  }


 public static void fiHookNetReadReq(FMJoinPoint fjp){
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		ClassWC cwc = fjp.getClassWC();
		Context c = cwc.getContext();
		String extra = (String)c.getExtraContext();

		Util.MESSAGE("In fiHookNetReadReq...packet is " + extra);

		if(extra != null){
			String srcloc = getSourceLoc(fjp);
		        waitUntilMyTurnNetRdReq(fjp, extra, srcloc);	
		}
	}	
 }


  // ******************************************************
  // PALLAVI: for local read events that do not need to be reordered
  // ******************************************************
  public static void fiHookLocalRead(FMJoinPoint fjp, boolean noWait) {
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		ClassWC cwc = fjp.getClassWC();
		Context c = cwc.getContext();
		Object extra = c.getExtraContext();
		
		String targetIO = c.getTargetIO();
		Pair<VoteContext, BlockingQueue> p = (Pair<VoteContext, BlockingQueue>)extra;
		VoteContext vc = p.fst();
		BlockingQueue q = p.snd();
		int sender = -1;
		String netIOPfx = "NetIO-";
		if(targetIO.startsWith(netIOPfx)){
			String s = targetIO.substring(netIOPfx.length());
			s = s.substring("Node-".length());
			sender = Integer.parseInt(s);
		}

		//addQueue(q);

		String loc = getLoc(q);
		System.out.println("Going to put a message into local queue at " + fjp.getSourceLoc() + 
				"...loc of queue is " + loc);
		
		String srcloc = getSourceLoc(fjp); 	
		sendLocalRdToServer(loc, srcloc, noWait, vc, sender);
	}
  }
  

  // ******************************************************
  // PALLAVI: for controlling polls
  // ******************************************************
  public static long fiHookPoll(FMJoinPoint fjp) {
	JoinPoint jp = fjp.getJoinPoint();  
	Object[] args = jp.getArgs();
	Long time = (Long)args[0];
	
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		ClassWC cwc = fjp.getClassWC();
		Context c = cwc.getContext();
		Object extra = c.getExtraContext();
		BlockingQueue q = (BlockingQueue)extra;
		
		//boolean containsQ = isQueuePresent(q);	

		//if(containsQ){
			String loc = getLoc(q);
		        //System.out.println("Going to wait before this poll event...loc of queue is " + loc);	
		        String srcloc = getSourceLoc(fjp); 	
			waitUntilMyTurnPoll(loc, srcloc, getStack());
		        //System.out.println("Done waiting for this poll event...loc of queue is " + loc);	
			return 0;
		//}
		//else{
		//	System.out.println("Queue created at " + loc + " is not yet considered");
		//	return time.longValue();
		//}
	}
	else{
		return time.longValue();	
	}
  }

  
  // ******************************************************
  // PALLAVI: for intercepting clears of queues
  // ******************************************************
  public static void fiHookClearQueue(FMJoinPoint fjp) {
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		ClassWC cwc = fjp.getClassWC();
		Context c = cwc.getContext();
		Object extra = c.getExtraContext();
		BlockingQueue q = (BlockingQueue)extra;
		
		//boolean containsQ = isQueuePresent(q);	

		//if(containsQ){
			String loc = getLoc(q);
		        String srcloc = getSourceLoc(fjp); 	
			sendClearInfoToServer(loc, srcloc);	
		//}	
	}	
  }


  private static Map<BlockingQueue, String> queueToLoc = new HashMap<BlockingQueue, String>(); 
  
  public static void addQueueLoc(BlockingQueue q, String loc){
	synchronized(queueToLoc){
		queueToLoc.put(q, loc);
	}
  }

  public static void clearQueueToLocInfo(){
	synchronized(queueToLoc){
		queueToLoc.clear();
	}	
  }

  public static String getLoc(BlockingQueue q){
	synchronized(queueToLoc){
		String loc = queueToLoc.get(q);
		return loc;
	}	
  }


  // ******************************************************
  // PALLAVI: for recording creation of queues
  // ******************************************************
  public static void fiHookCreateQueue(FMJoinPoint fjp) {
	//if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		Util.MESSAGE("In fiHookCreateQueue");
		Object rov = fjp.getJoinRov();
		//if(rov instanceof LinkedBlockingQueue){
		if(rov instanceof BlockingQueue){
			String loc = fjp.getSourceLoc();
			BlockingQueue q = (BlockingQueue)rov;
			addQueueLoc(q, loc);
		}
	//}
  } 
  
  // ******************************************************
  // PALLAVI: for recording creation of queues
  // ******************************************************
  public static void fiHookStateChange(FMJoinPoint fjp) {
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		//Util.MESSAGE("In fiHookStateChange");
		ClassWC cwc = fjp.getClassWC();
		Context c = cwc.getContext();
		Object extra = c.getExtraContext();
		VoteContext vc = (VoteContext)extra;

		String srcloc = getSourceLoc(fjp); 	
		sendStateChangeToServer(vc, srcloc);	
		//Util.MESSAGE("Done with fiHookStateChange...updated leader " + vc.leader + 
		//		" zxid = " + vc.zxid + " in node " + FMContext.nodeIntId());
	}
  }

  // ******************************************************
  // PALLAVI: for controlling the time lapsed in sleep
  // ******************************************************
  public static void fiHookSleep(FMJoinPoint fjp) {
	//Util.MESSAGE("In fiHookSleep...before going into the if statement");
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		//Util.MESSAGE("In fiHookSleep...after going into the if statement");
		Thread t = Thread.currentThread();
		StackTraceElement[] st = t.getStackTrace();
		
		boolean inMeth = false;
		String stStr = "";
		for(int i = 0; i < st.length; i++){
			String stElem = st[i].toString();
			if(!inMeth && stElem.contains("Leader.lead")){
				inMeth = true;
			}	
			stStr += stElem;
			if(i != (st.length - 1)){
				stStr += "\n";
			}	
		}

		//Util.MESSAGE("In fiHookSleep...in the beginning. Stack is " + stStr + " inMeth is " + inMeth);
		
		if(inMeth){
			Util.MESSAGE("In fiHookSleep");
			String srcloc = getSourceLoc(fjp); 	
			sendSleepToServer(srcloc, stStr);	
		}
	}
  }
  
  
  // ***********************************************************************************
  // PALLAVI: for letting the server know that a leader has been elected and is serving
  // ***********************************************************************************
  public static void fiHookLeaderElected() {
	//Util.MESSAGE("In fiHookLeaderElected...before the if statement");
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		//Util.MESSAGE("In fiHookLeaderElected...after the if statement");
		sendLeaderElectedToServer();
	}
  }

  // ***********************************************************************************
  // PALLAVI: leader election being started
  // ***********************************************************************************
  public static void fiHookStartElection() {
	//Util.MESSAGE("In fiHookStartElection...before the if statement");
	if((new File(FMLogic.EXP_RUN_FLAG)).exists()){
		//Util.MESSAGE("In fiHookStartElection...after the if statement");
		sendStartElectionToServer();
	}
  }

  
  
 // ***********************************************************************************
 // PALLAVI: keeping track of sockets and associated output streams
 // ***********************************************************************************
 private static Map<OutputStream, Socket> osToSoc = new HashMap<OutputStream, Socket>();
 private static Map<Object, BufferedOutputStream> boaToOs = new HashMap<Object, BufferedOutputStream>();
 private static Map<BufferedOutputStream, OutputStream> bsToS = new HashMap<BufferedOutputStream, OutputStream>();
 private static Map<Integer, Long> portToSid = new HashMap<Integer, Long>();

 public static void addSocketOutputStream(OutputStream os, Socket s){
	osToSoc.put(os, s);
 }

 public static void addOutputArchive(Object boa, BufferedOutputStream os){
	boaToOs.put(boa, os);
 }
 
 public static void addBufferedOutputStream(BufferedOutputStream bos, OutputStream os){
	bsToS.put(bos, os);
 }

 public static void addServerPortInfo(int port, long sid){
	portToSid.put(port, sid);
 }

 public static long getServerForPort(int port){
	Long sid = portToSid.get(port);
	if(sid == null){
		return 0;
	}
	return sid.longValue();
 }

 public static Socket getSocket(Object boa){
	BufferedOutputStream bos = boaToOs.get(boa);
	if(bos == null){
		return null;
	}
	OutputStream os = bsToS.get(bos);
	if(os == null){
		return null;
	}
	Socket s = osToSoc.get(os);
	return s;
 }


  // #####################################################################
  // #####################################################################
  // ##                                                                 ##
  // ##          S A N I T Y  C H E C K   &   F I L T E R I N G         ##
  // ##                                                                 ##
  // #####################################################################
  // #####################################################################


  // ******************************************************
  private static FailType tryFiHook(FMJoinPoint fjp) {
    	return syncedTryFiHook(fjp);
  }


  // ******************************************************
  // first do some sanity checking and filtering
  //  1. check that context is not null
  //  2. check that fm server is connected
  //  3. check that we pass client filter, whatever we define here
  //  4. let's do actual hook
  // ******************************************************
  private static synchronized FailType syncedTryFiHook(FMJoinPoint fjp) {

    // TODO: this is a bit wrong, because for FNFException ..
    // which is thrown by RAF.new FOS.new .. we haven't seen
    // the context so it's still null, but we might want to
    // throw an FNFException ... right now we're throwing FNFException
    // after the call RAF.nww and FOS.new
    if (isNullContext(fjp))
      return FailType.NONE;

    if (!passClientFilter(fjp))
      return FailType.NONE;

    FailType ft = doFiHook(fjp);
    return ft;

  }

  // ******************************************************
  // context is okay to be null here because we're not weaving
  // this aspect.aj to all files .. so ClassWC objects generated in
  // non-weaved files will not have context
  // ******************************************************
  private static boolean isNullContext(FMJoinPoint fjp) {

    if (fjp.getClassWC() == null) {
      Util.WARNING_ONELINE(fjp.getJoinPoint(), "null class WC at failure hook (FMClient)");
      return true;
    }

    if (fjp.getClassWC().getContext() == null) {

      // FIXME, hack:
      // check fi cwc is an instance of file or not
      // if so let's get the absolute path .. 
      // the reason why File doesn't have context is because
      // sometimes File is obtained from File.listFile()
      ClassWC cwc = fjp.getClassWC();
      if (cwc instanceof File) {
	File f = (File)cwc;
	f.context = new Context(f.getAbsolutePath());
	return false;
      }
      
      // 
      Util.WARNING_ONELINE(fjp.getJoinPoint(), "null context at failure hook (FMClient)");
      return true;
    }

    return false;
  }


  // ******************************************************
  // some filtering we could do at client
  private static boolean passClientFilter(FMJoinPoint fjp) {

    boolean pass = false;

    // we're interested in after join place only
    // if (fjp.getJoinPlc() == JoinPlc.AFTER) {
    // pass = true;
    // }

    // DEPRECATED policy
    // A little logic in insertFiHook, because depending on the
    // context, we want to do the failure before/after.
    // So far, here's a little policy:
    //   - Context = Disk, crash after
    //   - Context = NetIO, crash before and after

    pass = true;

    return pass;

  }




  // #####################################################################
  // #####################################################################
  // ##                                                                 ##
  // ##                  F M C L I E N T     L O G I C                  ##
  // ##                                                                 ##
  // #####################################################################
  // #####################################################################


  // ******************************************************
  private static FailType doFiHook(FMJoinPoint fjp) {

    // Util.WARNING(jp, "intercepted!!!");


    // ****************************
    // 1. prepare
    // prepare FST, FMC, FJP, id (all Writable)
    // ****************************
    Thread t = Thread.currentThread();
    FMStackTrace fst = new FMStackTrace(t.getStackTrace());

    ClassWC cwc = fjp.getClassWC();
    Context c = cwc.getContext();
    
    //PALLAVI: get the vote related context if present
    Object extra = c.getExtraContext();
    VoteContext vctx = null;
    if ((extra != null) && (extra instanceof VoteContext)){
	vctx = (VoteContext)extra;
    }
   
    FMContext ctx = new FMContext(c.getTargetIO(), vctx);
    ctx.setCutpointRandomId();

    // ****************************
    // 2. let's get failure
    // REMEMBER: remember that if this hangs, then this means
    // that some of the args that are passed here do not have
    // a correct Writable read and write implementation!
    // So, do check each argument
    // ****************************
    FMAllContext fac = new FMAllContext(fjp, ctx, fst);
    FailType ft = sendContextOptimizer(fac);



    // ****************************
    // 3. now let's check if there is any persistent failure
    // ****************************
    ft = FMLogic.checkPersistentFailure(fac, ft);


    // ****************************
    // 4. let's process the failure
    // ****************************
    printFailType(fjp, fst, ctx, ft);
    processFailure(fjp, fst, ctx, ft);

    // some FailTypes (e.g. exception might not have been processsed
    // here, so we need to pass this on)

    return ft;

  }

  // ******************************************************
  // Do more optimization at the client, so that we reduce
  // communication to the fm server
  // ******************************************************
  private static FailType sendContextOptimizer(FMAllContext fac) {


    // if we have reached the max fsn .. then there is no point
    // we're checking this to the fm server logic
    // but remember we still need to check for persistent failures
    if (isClientOptimizeFlagExist()) {
      
      if (!FMLogic.isEnableFailureFlagExist()) {
	return FailType.NONE;
      }

      if (FMLogic.hasReachedMaxFsn()) {
        return FailType.NONE;
      }
    }

    FailType ft = sendContextViaXmlRpc(fac);

    return ft;

  }


  // ***********************************************************
  private static boolean isClientOptimizeFlagExist() {
    File f = new File(FMLogic.CLIENT_OPTIMIZE_FLAG);
    if (f.exists())
      return true;
    return false;
  }



  // ******************************************************
  private static void printFailType(FMJoinPoint fjp,
                                    FMStackTrace fst,
                                    FMContext ctx,
                                    FailType ft) {


    if (ft == FailType.NONE)
      return;

    if (!FMServer.debug)
      return;

    Util.MESSAGE("I'm failing this (see below) with FailType: " + ft);
    System.out.println(ctx);
    System.out.println(fjp);
    System.out.println(fst);
    System.out.println("");

  }


  // *****************************************************
  private static void processFailure(FMJoinPoint fjp,
                                     FMStackTrace fst,
                                     FMContext ctx,
                                     FailType ft)  {
    if (ft == FailType.NONE)
      return;

    else if (ft == FailType.CRASH)
      processCrash(fjp, fst, ctx);

    else if (ft == FailType.CORRUPTION)
      processCorruption(fjp, fst, ctx);

    else if (ft == FailType.RETFALSE)
      processReturnFalse(fjp, fst, ctx);

    else if (ft == FailType.EXCEPTION)
      processException(fjp, fst, ctx);

    else if (ft == FailType.BADDISK)
      processBadDisk(fjp, fst, ctx);

  }


  // ******************************************************
  private static void processCrash(FMJoinPoint fjp,
                                   FMStackTrace fst,
                                   FMContext ctx) {

    String pidToCrash = Util.getPid();

    Util.WARNING("I'm crashing here, and should see no more output");

    // 1) let's do the forceful way, use kill
    // String cmd = String.format("kill -s KILL %5s", pidToCrash);
    // String cmdout = Util.runCommand(cmd);

    // )2 or, let's do the normal way
    System.exit(FAAS_SYSTEM_EXIT_STATUS);
    Util.ERROR("if you see this, we are not crashing properly");

    // if we ever see this file, we're not failing properly
    File f = new File(FMLogic.TMPFI + "CRASH-FAILED");
    try { f.createNewFile(); } catch (Exception e) { }
  }



  // ******************************************************
  private static void processRestart(FMJoinPoint fjp,
                                   FMStackTrace fst,
                                   FMContext ctx, int serverId) {
	System.out.flush();

        String cmd = String.format("bin/zkServer.sh start zoo%d.cfg %d", serverId, serverId);

        String cmdout = Util.runCommand(cmd);
        Util.MESSAGE(cmdout);
        Util.MESSAGE("\n\n");
        Util.sleep(1000);	  
  }	  



  // ******************************************************
  private static void processCorruption(FMJoinPoint fjp,
                                        FMStackTrace fst,
                                        FMContext ctx) {

    Object jrov = fjp.getJoinRov();

    // something is wrong, for corruption jrov should not be null
    if (jrov == null) {
      Util.FATAL("Corrupting a null Join ROV");
      return;
    }

    if (jrov instanceof java.lang.Long) {
      long tmp = ((Long)jrov).longValue();
      long tmp2;
      tmp2 = tmp - (tmp % 100000);
      tmp2 += (2 * 3600 * 1000);
      Long newJrov = new Long(tmp2);
      fjp.setJoinRov(newJrov);

      Util.MESSAGE("Corrupting read long from " +
                   tmp + " to " +
                   newJrov.longValue());
    }

  }

  // ******************************************************
  private static void processReturnFalse(FMJoinPoint fjp,
                                         FMStackTrace fst,
                                         FMContext ctx) {

    Object jrov = fjp.getJoinRov();

    // something is wrong, for ret false, jrov must be null!
    if (jrov != null) {
      // if we get to this point, there must be some race condition
      // where at JoinPlc.BEFORE, this join point does not see
      // the failure, so it doesn't return a false, but then
      // at JoinPlc.AFTER, some other join point in other threads
      // is being failed (E.g. baddisk), and hence, suddently
      // JoinPlc.AFTER for this point "Sees" the failure, but
      // we have run proceed() for this joinpoint. So in this case,
      // we should just say an error rather than a fatal
      Util.ERROR("Returning false, but Join ROV is not null");
      return;
    }

    Boolean newJrov = new Boolean(false);
    fjp.setJoinRov(newJrov);

    Util.MESSAGE("Returning false now ... ");
  }


  // ******************************************************
  // We set the exception that we should throw later in
  // FMJoinPoint
  // ******************************************************
  private static void processException(FMJoinPoint fjp,
                                       FMStackTrace fst,
                                       FMContext ctx) {

    if (fjp.getJoinExc() == JoinExc.IO) {
      fjp.setIox(new IOException("Intentional IOException from " + fjp.toString()));
    }
    else if (fjp.getJoinExc() == JoinExc.FNF) {
      fjp.setFnfx(new FileNotFoundException("Intentional IOException from FM"));
    }
  }


  // ******************************************************
  // If a bad disk, then we want to see what's the
  // join point is about. If it's exception then baddisk
  // will manifest to an exception. If it's a boolean return
  // value, it will manifest to a false return value
  // ******************************************************
  private static void processBadDisk(FMJoinPoint fjp,
                                     FMStackTrace fst,
                                     FMContext ctx) {
    if (fjp.getJoinExc() != JoinExc.NONE)
      processException(fjp, fst, ctx);
    else if (fjp.getJoinRbl() == JoinRbl.YES)
      processReturnFalse(fjp, fst, ctx);
  }


  // ******************************************************
  // DEPRECATED -- never crash the recipient of we get
  // a weird dead RPC to the FM server --- because when we
  // crash the other datanode, the other datanode could
  // be inside the FM server, and the FM server cannot return
  // properly, so then other RPC is dead ...
  private static void crashRecipient(FMContext ctx) {
    // String nodeId = Util.getNodeIdFromNetIO(ctx.getTargetIO());
    // String pidToCrash = Util.getPidFromNodeId(nodeId);
    // if (pidToCrash.equals("0")) { bad
  }


  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####           R E O R D E R     M E S S A G E S                   ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################
 
  public static void waitUntilMyTurnNetRd(FMJoinPoint fjp, VoteContext vctx, String loc, String srcloc){
	boolean electionOver = (Util.doesExist(FMLogic.ELECTION_OVER));
	if(electionOver || cannotConnectToServer() || (!Reorder.isExpStart() && Reorder.isNormalExec())){
		return;
	}

	if(vctx == null){
		return;
	}
	
        int randId = Util.r8();
        File f = Util.getRpcFile(randId);
        DataOutputStream dos = Util.getRpcOutputStream(randId);

        try{
	     //write the data here
	     dos.writeInt(FMContext.nodeIntId());
		
	     ClassWC cwc = fjp.getClassWC();
	     Context c = cwc.getContext();
	     String othrNode = Util.getServerIdFromNetTargetIO(c.getTargetIO());
	     
	     int othrNodeIntId = Util.getIdValFromNodeId(othrNode);
	     dos.writeInt(othrNodeIntId);	     

	     dos.writeLong(vctx.leader);
	     dos.writeLong(vctx.zxid);
	     dos.writeLong(vctx.epoch);
	     dos.writeUTF(vctx.stack);
	     
	     dos.writeUTF(loc);
	     dos.writeUTF(srcloc);
	     
	     dos.close();

	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendNetRdContext", params);
	     f.delete();
	}
	catch(Exception e){
	     f.delete();
	     Util.EXCEPTION("RPC client error", e);
	}

	return;

  }


 // ****************************************************************************

 public static String getStack(){
  	return Util.getStackTrace();
 }


  public static void waitUntilMyTurnNetRdReq(FMJoinPoint fjp, String r, String srcloc){
	
	boolean writeOver = (Util.doesExist(FMLogic.WRITE_OVER));
	if(writeOver || cannotConnectToServer() || (!Reorder.isExpStart() && Reorder.isNormalExec())){
		return;
	}

	if(r == null){
		return;
	}
	
        int randId = Util.r8();
        File f = Util.getRpcFile(randId);
        DataOutputStream dos = Util.getRpcOutputStream(randId);

        try{
	     //write the data here
	     ClassWC cwc = fjp.getClassWC();
	     Context c = cwc.getContext();
	     String othrNode = Util.getServerIdFromNetTargetIO(c.getTargetIO());
	     int othrNodeIntId = Util.getIdValFromNodeId(othrNode);
	     dos.writeInt(othrNodeIntId);	     
	     
	     dos.writeInt(FMContext.nodeIntId());

	     dos.writeUTF(srcloc);

	     dos.writeUTF(r);
	     
	     dos.close();

	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendNetRdReqContext", params);
	     f.delete();
	}
	catch(Exception e){
	     f.delete();
	     Util.EXCEPTION("RPC client error", e);
	}

	return;

  }



  public static void sendLocalRdToServer(String loc, String srcloc, boolean noWait, VoteContext vc, int sender){
	boolean electionOver = (Util.doesExist(FMLogic.ELECTION_OVER));
	if(electionOver || cannotConnectToServer() || (!Reorder.isExpStart() && Reorder.isNormalExec())){
		return;
	}

        int randId = Util.r8();
        File f = Util.getRpcFile(randId);
        DataOutputStream dos = Util.getRpcOutputStream(randId);

	try{
	     //write the data here
	     dos.writeInt(sender);
	     dos.writeInt(FMContext.nodeIntId());
	     dos.writeUTF(loc);
	     dos.writeUTF(srcloc);
	     dos.writeLong(vc.leader);
	     dos.writeLong(vc.zxid);
	     dos.writeLong(vc.epoch);
	     dos.close();

	     Object[] params = new Object[]{new Integer(randId)};
	     if(!noWait){
		     Integer result = (Integer) fmClient.execute("FMServer.sendLocalRdContext", params);
	     }
	     else{
		     Integer result = (Integer) fmClient.execute("FMServer.sendLocalRdNoWaitContext", params);
	     }
	     f.delete();
	}
	catch(Exception e){
	     f.delete();
	     Util.EXCEPTION("RPC client error", e);
	}

  }


  public static void waitUntilMyTurnPoll(String loc, String srcloc, String stack){
	boolean electionOver = (Util.doesExist(FMLogic.ELECTION_OVER));
	if(electionOver || cannotConnectToServer() || (!Reorder.isExpStart() && Reorder.isNormalExec())){
		return;
	}

        int randId = Util.r8();
        File f = Util.getRpcFile(randId);
        DataOutputStream dos = Util.getRpcOutputStream(randId);

        try{
	     //write the data here
	     dos.writeInt(FMContext.nodeIntId());
	     dos.writeUTF(loc);
	     dos.writeUTF(srcloc);
	     dos.writeUTF(stack);
	     dos.close();
	     
	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendPollContext", params);
	     f.delete();

	}
	catch(Exception e){
	     f.delete();
	     Util.EXCEPTION("RPC client error", e);
	}

  }

  public static void sendClearInfoToServer(String loc, String srcloc){
	boolean electionOver = (Util.doesExist(FMLogic.ELECTION_OVER));
	if(electionOver || cannotConnectToServer() || (!Reorder.isExpStart() && Reorder.isNormalExec())){
		return;
	}

        int randId = Util.r8();
        File f = Util.getRpcFile(randId);
        DataOutputStream dos = Util.getRpcOutputStream(randId);

	try{
	     //write the data here
	     dos.writeInt(FMContext.nodeIntId());
	     dos.writeUTF(loc);
	     dos.writeUTF(srcloc);
	     dos.close();

	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendClearQContext", params);
	     f.delete();
	}
	catch(Exception e){
	     f.delete();
	     Util.EXCEPTION("RPC client error", e);
	}

  }  
  
  public static void sendStateChangeToServer(VoteContext vc, String srcloc){
	boolean electionOver = (Util.doesExist(FMLogic.ELECTION_OVER));
	if(electionOver || cannotConnectToServer() || (!Reorder.isExpStart() && Reorder.isNormalExec())){
		return;
	}

        int randId = Util.r8();
        File f = Util.getRpcFile(randId);
        DataOutputStream dos = Util.getRpcOutputStream(randId);

	try{
	     //write the data here
	     dos.writeUTF(srcloc);
	     dos.writeInt(FMContext.nodeIntId());
	     dos.writeLong(vc.leader);
	     dos.writeLong(vc.zxid);
	     dos.close();
	     
	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendStateChange", params);
	     f.delete();
	}
	catch(Exception e){
	     f.delete();
	     Util.EXCEPTION("RPC client error", e);
	}

  }


  public static void sendSleepToServer(String srcloc, String stack){
	if(cannotConnectToServer() || (!Reorder.isExpStart() && Reorder.isNormalExec())){
		return;
	}
	
        int randId = Util.r8();
        File f = Util.getRpcFile(randId);
        DataOutputStream dos = Util.getRpcOutputStream(randId);

	try{
	     dos.writeUTF(srcloc);
	     dos.writeUTF(stack);
	     dos.writeInt(FMContext.nodeIntId());
	     dos.close();
	     
	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendSleep", params);
	     f.delete();
	}
	catch(Exception e){
		f.delete();
		Util.EXCEPTION("RPC client error", e);
	}
  }


  public static void sendLeaderElectedToServer(){
	if(cannotConnectToServer()){
		return;
	}
	
        int randId = Util.r8();
	try{
	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendLeaderElected", params);
	}
	catch(Exception e){
		Util.EXCEPTION("RPC client error", e);
	}

  }
  
  
  public static void sendStartElectionToServer(){
	if(cannotConnectToServer()){
		return;
	}
	
        int randId = Util.r8();
	try{
	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendStartElection", params);
	}
	catch(Exception e){
		Util.EXCEPTION("RPC client error", e);
	}

  }


  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####                    X M L       R P C                          ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################

  private static XmlRpcClient fmClient;


  // ******************************************************
  private static boolean cannotConnectToServer() {
    if (fmClient != null)
      return false;
    connectToFMServer();
    if (fmClient == null)
      return true;
    return false;
  }



  // ******************************************************
  private static void connectToFMServer() {
    try {
      XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
      String httpAddr = "http://127.0.0.1:" + FMServer.PORT + "/xmlrpc";
      config.setServerURL(new URL(httpAddr));
      fmClient = new XmlRpcClient();
      fmClient.setConfig(config);
    } catch (Exception e) {
      fmClient = null;
    }
  }


  // ******************************************************
  private static FailType sendContextViaXmlRpc(FMAllContext fac) {

    FailType ft = FailType.NONE;

    // this is okay, because we don't always want to
    // run hdfs with fm server
    if (cannotConnectToServer())
      return ft;

    int randId = fac.ctx.getCutpointRandomId();
    File f = Util.getRpcFile(randId);
    DataOutputStream dos = Util.getRpcOutputStream(randId);

    try {

      fac.write(dos);
      dos.close();
      
      // System.out.format("- Sending %d \n", randId);
      
      Object[] params = new Object[]{new Integer(randId)};
      Integer result = 0;
      result = (Integer) fmClient.execute("FMServer.sendContext", params);
      
      ft = Util.intToFailType(result);
      
      // System.out.format("- Received %d %s \n", result, ft);
      
      f.delete();

    } catch (Exception e) {
      f.delete();
      
      Util.EXCEPTION("RPC client error", e);
      // Util.FATAL("RPC client error"); // it's okay that if we cannot connect
    }

    return ft;

  }


  //PALLAVI: Used in ZooKeeper

  public static void inWaitState(int sid){
	//Util.MESSAGE("In inWaitState...sid = " + sid);  

	if(cannotConnectToServer()){
		return;
	}
        
	int randId = Util.r8();
        File f = Util.getRpcFile(randId);
        DataOutputStream dos = Util.getRpcOutputStream(randId);
        
	try{
	     dos.writeInt(sid);
	     dos.close();

	     Object[] params = new Object[]{new Integer(randId)};
	     Integer result = (Integer) fmClient.execute("FMServer.sendNodeWaitState", params);
	     f.delete();
	}
	catch(Exception e){
	     f.delete();
	     Util.EXCEPTION("RPC client error", e);
	}
  }


  public static String getSourceLoc(FMJoinPoint fjp){
	String loc = "";
	JoinPoint jp = fjp.getJoinPoint();
	if(jp != null){
		   SourceLocation sl = jp.getSourceLocation();
		   int line = sl.getLine();
		   
		   Class type = sl.getWithinType();
		   Package pkg = type.getPackage();
		   String shortName = sl.getFileName();
		   String filename = String.format("%s/%s", pkg.getName().replace(".","/"), shortName);

		   loc = filename + "(" + line + ")";
	}

	return loc;
  }

  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####                 H A D O O P    R P C                          ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################

  /*

    private static FMProtocol fmp = null;
    private static Configuration conf = new Configuration();
    private static InetSocketAddress addr =
    new InetSocketAddress(FMServer.bindAddr, FMServer.port);


    // ******************************************************
    private static FailType sendContextViaHadoopRPC(FMAllContext fac) {

    if (isNullFMServer())
    return FailType.NONE;

    FailType ft = FailType.NONE;
    ft = fmp.sendContext(fac.fjp, fac.ctx, fac.fst);
    return ft;
    }

    // ******************************************************
    private static boolean isNullFMServer() {
    if (fmp != null)
    return false;
    if (connectToFMServer() != null)
    return false;
    return true;
    }

    // ******************************************************
    private static FMProtocol connectToFMServer() {

    if (fmp != null)
    return fmp;

    try {
    // this shouldn't be wait for proxy, if fm server is not there, continue
    fmp = (FMProtocol)
    RPC.getProxy(FMProtocol.class, FMProtocol.versionID, addr, conf);
    // RPC.waitForProxy(FMProtocol.class, FMProtocol.versionID, addr, conf);
    } catch (IOException e) {
    Util.WARNING("cannot contact FM");
    return null;
    }
    return fmp;
    }

  */

}
