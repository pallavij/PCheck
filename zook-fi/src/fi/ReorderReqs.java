
package org.fi;

import java.util.*;
import java.io.*;

public class ReorderReqs extends Thread{
	public static LinkedList<Pair<Event, Object>> events;
	public static int nMsgsRecd;
	
	public static List<Integer> evtOrder;
	public static List<Event.EType> eTypeOrder;
	public static List<String> eNodeOrder;
	public static Map<String, List<Integer>> nodeToEvtOrder;

	public static List<String> evtsWoken;
	public static List<String> evtsWokenWONode;
	public static List<String> absStates;
	public static List<String> absQStates;
	public static int nEvtBeingWokenUp;

	public static long seed = 23418;
	public static Random randomGen = new Random(seed);

	public synchronized static void initBeforeEachExp(){
		Util.MESSAGE("Initialized before an experiment started");

		FMServer.clearQueueToNumElems();

		resetNormalExec();

		events = new LinkedList<Pair<Event, Object>>();
		resetNMsgsRecd();

		evtOrder = new LinkedList<Integer>();
		eTypeOrder = new LinkedList<Event.EType>();
		eNodeOrder = new LinkedList<String>();

		nodeToEvtOrder = new HashMap<String, List<Integer>>();
		evtsWoken = new LinkedList<String>();
		evtsWokenWONode = new LinkedList<String>();
		absStates = new LinkedList<String>();
		absQStates = new LinkedList<String>();
		nEvtBeingWokenUp = 0;

		History.readHistory();
		History.readPfx();
		Search.initBeforeEachExp();
	}


	public static void addEvent(Event e, Object waitObj){
		int sz = events.size();
		int idx = sz;

		Pair ep = new Pair(e, waitObj);

		events.add(idx, ep);
		
		if(e.isReadEvent()){
			nMsgsRecd++;
		}
	}


	public static void addAndWait(Event e, Object waitObj){
		synchronized(ReorderReqs.class){
			if(Reorder.isExpStart()){
				initBeforeEachExp();
				Reorder.resetExpStart();
			}
		
			if(isNormalExec()){
				return;
			}

			addEvent(e, waitObj);
		}
		
		synchronized(waitObj){
			try{
				Util.MESSAGE("Making a thread wait here");
				waitObj.wait();
			}
			catch(Exception exp){
				Util.EXCEPTION("ReorderReqs.addAndWait", exp);
			}
		}
	}

	public void run(){
		while(true){
		    try{
			Util.MESSAGE("In ReorderReqs thread");
		
			if(isWriteOver() && !Search.detEvtRetr){
				Util.MESSAGE("Going to wake up all servers and end write");
				resetNMsgsRecd();
				AState.clearState();
				setNormalExec();
				wakeUpAll();
				clearWriteOver();
				endWrite();
				continue;
			}

			if(events == null)
				continue;

			List<Pair<Event, Object>> evtsOld;
			synchronized(ReorderReqs.class){
				evtsOld = new LinkedList<Pair<Event, Object>>(events);
			}
			
			Util.sleep(1000);

			boolean noNewEvt;
			boolean readMsgRecd;
			int nEvts = 0;
			synchronized(ReorderReqs.class){
				noNewEvt = events.equals(evtsOld);
				readMsgRecd = (nMsgsRecd > 0);
				if(events != null) nEvts = events.size();
			}


			if(!noNewEvt || (nEvts == 0)) {
				continue;
			}


			Util.MESSAGE("All servers waiting. Going to see if I can wake a thread up. Size of events is " 
					+ nEvts);


			Pair<Event, String> eWSt = Search.wakeUpBreadthFirst();
			//Pair<Event, String> eWSt = Search.wakeUpDet();
			//Pair<Event, String> eWSt = Search.wakeUpRandom();
			//Pair<Event, String> eWSt = Search.wakeUpPrioritizedNode();
			
			if(eWSt != null){
				Event e = eWSt.fst();
				String state = eWSt.snd();

				evtOrder.add(e.hashCode());
				//Util.MESSAGE("Adding to evtOrder: " + e.hashCode());
				eTypeOrder.add(e.t);
				eNodeOrder.add(e.getNodeId());
				
				String nodeId = e.getNodeId();
				List<Integer> evts = nodeToEvtOrder.get(nodeId);
				if(evts == null){
					evts = new LinkedList<Integer>();
					nodeToEvtOrder.put(nodeId, evts);	
				}
				evts.add(e.hashCodeWONodeStack());

				Search.setLastNode(nodeId);
			}
			else{
				Util.MESSAGE("Event was null");
				evtOrder.add(0);
			}
		   }
	           catch(Exception e){
			Util.EXCEPTION("ReorderReqs.run", e);
		   }	   
		}
	}

        
	public static void letEvtGo(Pair<Event, Object> eWObj, boolean record){
		if(eWObj == null) return;

		Event e = eWObj.fst();
		Object objToNotf = eWObj.snd();
		
		if((e != null) && (objToNotf != null)){
			synchronized(ReorderReqs.class){
				events.remove(eWObj);

				if(record){
					History.recordEvtWoken(events, e, objToNotf, evtsWoken, evtsWokenWONode, absStates, absQStates);	
					History.recordCoverage(e);
				}
			}
			
			synchronized(objToNotf){objToNotf.notify();}		
		}
		else{
			Util.ERROR("The object on which the thread is waiting to block an event is null");
		}

	}


	public static void wakeUpAll(){
		List<Pair<Event, Object>> evts = null;
		synchronized(ReorderReqs.class){
			if((events == null) || events.isEmpty())
				return;
			evts = new LinkedList<Pair<Event, Object>>(events);
		}
		for(Pair<Event, Object> e : evts){
			letEvtGo(e, false);
		}
	}


	public static void wipeOut(){
		File f = new File(FMLogic.EXP_RUN_FLAG);
		if(f.exists()){
			Util.MESSAGE("Wiping out");
			setNormalExec();
			createWipeOutFlag();
			resetNMsgsRecd();
			AState.clearState();
			wakeUpAll();
			f.delete();
		}
	}

	public static void endWrite(){
		File f = new File(FMLogic.EXP_RUN_FLAG);
		if(f.exists()){
			Util.MESSAGE("Going to write to history");
			History.writeToHistory(evtOrder);
			History.writePfx();
			Search.recordPfxDepInfo();
			f.delete();
		}
	}

	
	public static void createWipeOutFlag(){
		File f = new File(FMLogic.WIPE_OUT_FLAG);
		if(!f.exists()){
			try{
				f.createNewFile();
			}
			catch(Exception e){
				Util.ERROR("Exception thrown while creating the wipe out flag file");
			}
		}
	}

	public static synchronized boolean isNormalExec(){
		return FMLogic.NORMAL_EXEC;
	}

	public static synchronized void setNormalExec(){
		FMLogic.NORMAL_EXEC = true;
	}
	
	public static synchronized void resetNormalExec(){
		FMLogic.NORMAL_EXEC = false;
	}

	public static synchronized void resetNMsgsRecd(){
		nMsgsRecd = 0;
	}

	
	public static boolean isWriteOver(){
		return (new File(FMLogic.WRITE_OVER)).exists();
	}
	
	public static void clearWriteOver(){
		File f = new File(FMLogic.WRITE_OVER);
		if(f.exists()){
			f.delete();
		}
	}

	public static synchronized int incEvtBeingWokenUp(){
		nEvtBeingWokenUp++;
		return nEvtBeingWokenUp;
	}

	public static synchronized List<Pair<Event, Object>> getEvents(){
		return events;
	}
	
	public static synchronized Map<String, List<Integer>> getNodeToEvtOrder(){
		return nodeToEvtOrder;
	}
	
	public static synchronized List<Integer> getEvtOrder(){
		return evtOrder;
	}
	
	public static synchronized List<Event.EType> getETypeOrder(){
		return eTypeOrder;
	}
	
	public static synchronized List<String> getENodeOrder(){
		return eNodeOrder;
	}

	public static synchronized List<String> getAbsStates(){
		return absStates;
	}
	
	public static synchronized List<String> getAbsQStates(){
		return absQStates;
	}

}

