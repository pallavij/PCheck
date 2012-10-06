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

import java.util.*;
import java.io.*;

public class History{
	public static List<Integer> evtOrderPfxToTry;
	public static List<String> evtOrdersInHist;

	public static List<LinkedList<Integer>> prevPfxs;
	public static List<LinkedList<Integer>> pfxsThisExp;
	public static int nPrevPfxs;

	
	public static void readHistory(){
		evtOrdersInHist = Util.fileContentToList(FMLogic.EVT_ORDER_HISTORY_FILE);
		pfxsThisExp = new LinkedList<LinkedList<Integer>>();
	}


	public static void readPfx(){

		readPrevPfxs();

		evtOrderPfxToTry = null;
		String pfx = Util.fileContentToString(FMLogic.EVT_ORDER_PFX_TO_TRY);
		Util.doDelete(FMLogic.EVT_ORDER_PFX_TO_TRY);

		if(pfx != null){
			pfx = pfx.trim();
		}

		if((pfx != null) && (!(pfx.equals("")))){
			evtOrderPfxToTry = breakStrIntoParts(pfx);
		}
		else if((pfx != null) && pfx.equals("")){
			evtOrderPfxToTry = new LinkedList<Integer>();
		}
		else if(pfx == null){
			File f = new File(FMLogic.START_FLAG);
			if(!f.exists()){
				File fw = new File(FMLogic.LAST_EXP_WIPE_OUT_FLAG);
				if(!fw.exists()){
					Reorder.wipeOut();
					//ReorderReqs.wipeOut();
				}
				else{
					fw.delete();
				}
			}
			else{
				f.delete();
			}
		}

	}


	public static void readPrevPfxs(){
		String n = Util.fileContentToString(FMLogic.N_PREV_PFXS);
		if(n != null){
			n = n.trim();
			nPrevPfxs = Integer.parseInt(n);
		}
		else{
			nPrevPfxs = 0;
		}

		prevPfxs = new LinkedList<LinkedList<Integer>>();
		for(int i = 0; i < nPrevPfxs; i++){
			String pfx = Util.fileContentToString(FMLogic.PREV_PFX + (i+1));
			if(pfx != null){
				LinkedList l = new LinkedList(breakStrIntoParts(pfx));
				prevPfxs.add(l);
			}
		}
	}

 
	public static List<Integer> breakStrIntoParts(String s){
		List<Integer> lOfSIds = new LinkedList<Integer>();
		if(s != null){
			s = s.trim();
		}
		if((s != null) && !s.equals("")){
			String[] sIds = s.split("\\s+");
			for(String id : sIds){
				lOfSIds.add(Integer.parseInt(id));
			}
		}
		return lOfSIds;
	}


	public static void writeToHistory(List<Integer> evtOrder){
		if((evtOrder != null) && !evtOrder.isEmpty()){
			String order = getStrRep(evtOrder);
			Util.stringToFileContent(order, FMLogic.EVT_ORDER_HISTORY_FILE, true);
		}
	}


	public static String getStrRep(List l){
		String s = "";
		for(Object t : l){
			if(t != null){
				s += t.toString() + " ";
			}
		}
		s = s.trim();
		s += "\n";
		return s;
	}


	public static void writePfx(){
		int nOrders = Search.getNOrders();
		int nOrdsToExplore = Search.getOrdsToExplore();
		int nPfxsThisExp = pfxsThisExp.size();

		int nPfxs = nPrevPfxs + nPfxsThisExp;

		String pfx;
		if(nOrdsToExplore > 0){
			if(nPrevPfxs > 0){
				Util.doDelete(FMLogic.PREV_PFX + nPrevPfxs);
				if(nPrevPfxs > 1){
					Util.stringToFileContent((new Integer(nPrevPfxs-1)).toString(), FMLogic.N_PREV_PFXS, false);
					pfx = getStrRep(prevPfxs.get(nPrevPfxs-2));
				}
				else{
					Util.doDelete(FMLogic.N_PREV_PFXS);
					pfx = "\n";
				}
			}
			else{
				if(nPfxsThisExp > 0){
					Util.doDelete(FMLogic.N_PREV_PFXS);
					pfx = "\n";
				}
				else{
					Util.doDelete(FMLogic.EVT_ORDER_PFX_TO_TRY);
					return;
				}
			}
		}
		else{
			if(nPfxsThisExp > 0){
				pfx = getStrRep(pfxsThisExp.get(nPfxsThisExp-1));	
			}
			else{
				pfx = getStrRep(prevPfxs.get(nPrevPfxs-1));
			}

			Util.stringToFileContent((new Integer(nPfxs)).toString(), FMLogic.N_PREV_PFXS, false);	
		}
	
		//If pfx is empty string, we do not keep information about previous prefixes
		if((pfx == null) || (pfx.trim().equals(""))){
			Util.doDelete(FMLogic.N_PREV_PFXS);
		}

		Util.MESSAGE("pfx to try is " + pfx);	
		Util.stringToFileContent(pfx, FMLogic.EVT_ORDER_PFX_TO_TRY, false);
	
		/****************************	
		String pfx;
		if(nOrders > 0){
			if(nPfxsThisExp > 0){
				pfx = getStrRep(pfxsThisExp.get(nPfxsThisExp-1));	
			}
			else{
				pfx = getStrRep(prevPfxs.get(nPrevPfxs-1));
			}
		}
		else{
			if(nPrevPfxs > 0){
				Util.doDelete(FMLogic.PREV_PFX + nPrevPfxs);
				if(nPrevPfxs > 1){
					Util.stringToFileContent((new Integer(nPrevPfxs-1)).toString(), FMLogic.N_PREV_PFXS, false);
					pfx = getStrRep(prevPfxs.get(nPrevPfxs-2));
				}
				else{
					Util.doDelete(FMLogic.N_PREV_PFXS);
					pfx = "\n";
				}
			}
			else{
				Util.doDelete(FMLogic.EVT_ORDER_PFX_TO_TRY);
				return;
			}
		}
		Util.stringToFileContent(pfx, FMLogic.EVT_ORDER_PFX_TO_TRY, false);

		****************************/	

		
	}



	public static void recordCoverage(Event e){
		int hash = e.hashCode();
		String descStr = e.toString();
		String fName = FMLogic.COVERAGE_COMPLETE_DIR + "h" + hash + ".txt";
		File f = new File(fName);
		
		try{
			f.createNewFile();
			Util.stringToFileContent(descStr, f);
		}
		catch(IOException exp){}
	}



	public static void recordEvtWoken(List<Pair<Event, Object>> events, Event e, Object objToNotf, 
			List<String> evtsWoken, List<String> evtsWokenWONode, List<String> absStates, 
			List<String> absQStates){
		if(objToNotf == null)
			return;
		
		List<Event> evts = (new Pair()).getFstElems(events);
		
		String s = getQueueStr(e, evts, true);
		evtsWoken.add(s.trim());
		Util.stringToFileContent(s, FMLogic.EVTS_WOKEN, true);


		String sWONode = getQueueStr(e, evts, false);
		evtsWokenWONode.add(sWONode.trim());
		Util.stringToFileContent(sWONode, FMLogic.EVTS_WOKEN_WO_NODE, true);
		
		
		String sDetail = "";
		sDetail += "==================WOKEN UP EVT (" + evtsWoken.size()  + ")================\n";
		sDetail += e.toString() + "\n";
		sDetail += " : \n";
		EventComparator ecomp = new EventComparator();
		Collections.sort(evts, ecomp);
		for(Event evt : evts){
			sDetail += evt.toString() + "\n";
		}
		sDetail += "==============================================\n";
		Util.stringToFileContent(sDetail, FMLogic.EVTS_WOKEN_DET, true);
		
		
		String state = AState.getStateOfLeaders();
		absStates.add(state.trim());
		Util.stringToFileContent(state+"\n", FMLogic.EXEC_STATE, true);


		String qState = AState.getStateOfQueues();
		absQStates.add(qState);
		Util.stringToFileContent(qState+"\n", FMLogic.QUEUE_STATE, true);
			
	}

	
	public static void recordLocalEvt(Event e){
		String sDetail = "";
		sDetail += "==================WOKEN UP EVT================\n";
		sDetail += e.toString() + "\n";
		sDetail += " : \n";
		sDetail += "==============================================\n";
		Util.stringToFileContent(sDetail, FMLogic.EVTS_WOKEN_DET, true);
			
	}
	


	private static String getQueueStr(Event e, List<Event> q, boolean inclNode){
		String s = "";
		String eh = (inclNode? (new Integer(e.hashCode())).toString() : "(" + e.getNodeId() + "," + 
				e.hashCodeWONodeStack() + ")");
		s += eh;
		s += " : ";
		for(Event evt : q){
		       String evth = (inclNode? (new Integer(evt.hashCode())).toString() : 
				       "(" + evt.getNodeId() + "," + evt.hashCodeWONodeStack() + ")");
		       s += evth;
		       s += " ";
		}
		s = s.trim();
		s += "\n";
		return s;
	}


	public static List<Integer> getEvtOrderPfxToTry(){
		return evtOrderPfxToTry;
	}

	public static int getNPrevPfxs(){
		return nPrevPfxs;
	}

	public static List<String> getEvtOrdersInHist(){
		if(evtOrdersInHist != null)
			return evtOrdersInHist;
		else
			return (new LinkedList<String>());
	}	

	public static List<LinkedList<Integer>> getPrevPfxs(){
		return prevPfxs;
	}

	public static List<LinkedList<Integer>> getPfxsThisExp(){
		return pfxsThisExp;
	}
}

