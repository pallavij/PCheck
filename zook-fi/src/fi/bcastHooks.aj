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

import org.apache.jute.OutputArchive;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.Record;

//zookeeper specific import
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.ServerCnxn;
import org.apache.zookeeper.server.quorum.QuorumPacket;
import org.apache.zookeeper.server.quorum.LearnerHandler;

//Hooks added by PALLAVI
public aspect bcastHooks {

   Object around(Socket s):
	(call (OutputStream Socket.getOutputStream()) && target(s) && !within(org.fi.*)){
		OutputStream os = (OutputStream)proceed(s);
		FMClient.addSocketOutputStream(os, s);	
		return os;
   }

   Object around(BufferedOutputStream os):
   	(call (BinaryOutputArchive BinaryOutputArchive.getArchive(OutputStream)) && args(os) && !within(org.fi.*)){
		BinaryOutputArchive boa = (BinaryOutputArchive)proceed(os);
		FMClient.addOutputArchive(boa, os);	
		return boa;
   }
  

   Object around(OutputStream os):
   (call (BufferedOutputStream.new(OutputStream)) && args(os) && !within(org.fi.*)){
		BufferedOutputStream bos = (BufferedOutputStream)proceed(os);
		FMClient.addBufferedOutputStream(bos, os);
		return bos;
   }


   void around(LearnerHandler lh):
   (call(void LearnerHandler.setSid(long)) && target(lh) && !within(org.fi.*)){
	proceed(lh);
	Socket s = lh.getSocket();
	int port = s.getPort();
	long sid = lh.getSid();
	FMClient.addServerPortInfo(port, sid);	
   }

   void around(BinaryOutputArchive boa, QuorumPacket qp, String s):
	(call (void OutputArchive.writeRecord(Record, String)) && target(boa) && args(qp, s) && !within(org.fi.*)){
		
		System.out.println("Stack at the time of writeRecord is " + getStack());
	
		//type is 5 for Leader.PING	
		if((s == null) || !s.equals("packet") || (qp.getType() == 5)){
			proceed(boa, qp, s);
			return;
		}
		
		Socket soc = FMClient.getSocket(boa);
		if(soc == null){
			System.out.println("Socket is null");
			proceed(boa, qp, s);
			return;
		}

	        int targetPort = soc.getPort();
                long tServerId = FMClient.getServerForPort(targetPort);

		if(tServerId == 0){
			tServerId = Util.getZkNodeFromPort(targetPort);
		}

		String ctxStr = "NetIO-Node-"+tServerId+":"+targetPort;
		if(tServerId == 0){
		     ctxStr = "NetIO-Node-Unknown:"+targetPort;
		}

		//System.out.println("ctxStr = " + ctxStr + " local port is " + soc.getLocalPort());
	
		Context ctx = new Context(ctxStr);
		File f = new File("a");
		ClassWC cwc = (ClassWC) f;
		cwc.setContext(ctx);

		String pstr = LearnerHandler.packetToString(qp);
		//System.out.println("quorum packet is " + pstr);


		//String[] pstrParts = pstr.split(" ");
		//ctx.setExtraContext(pstrParts[0]);
		ctx.setExtraContext(pstr);
	
		FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
				  JoinPlc.BEFORE, JoinIot.WRITE,
				  JoinExc.IO, JoinRbl.NO);
		FMClient.fiHookNetReadReq(fjp);
		
		proceed(boa, qp, s);
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


   /******************
   Object around(LinkedBlockingQueue q, Request r):
      (call (* LinkedBlockingQueue.add(Object)) 
           && target(q) && args(r) && !within(org.fi.*)){

        File f = new File("a");
        ClassWC cwc = (ClassWC) f;

	String ctxStr = "";
        if(r != null){
		ServerCnxn c = r.cnxn;
		if(c != null){
			InetSocketAddress isa = c.getRemoteAddress();
			InetAddress ia = isa.getAddress();
			System.out.println("hostName = " + ia.getHostName() + " port = " + isa.getPort());
			int targetPort = isa.getPort();
			int tServerId = Util.getZkNodeFromPort(targetPort);
			ctxStr = "NetIO-Node-"+tServerId+":"+targetPort;
                        if(tServerId == 0){
                             ctxStr = "NetIO-Node-Unknown:"+targetPort;
			}
		}
		else{
			System.out.println("c is null");
		}
	}    

        Context ctx = new Context(ctxStr);
        cwc.setContext(ctx);
                            
        ctx.setExtraContext(new Pair<BlockingQueue, Request>(q, r));
        FMJoinPoint fjp = new FMJoinPoint(thisJoinPoint, cwc, null,
				  JoinPlc.BEFORE, JoinIot.NONE,
				  JoinExc.IO, JoinRbl.YES);

	FMClient.fiHookNetReadReq(fjp);
        Object obj = proceed(q, r);
	return obj;
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
*****************/

}
