
package org.fi;

import java.util.*;
import java.io.*;

public class Search {
	public static final int kEvtsAllowed = 5;

	public static boolean isDepOrdered = false;
	
	//detEvtRetr is true when events have not been reordered
	public static boolean detEvtRetr = false;
	
	private static int nDetEvtRetr = 0;

        public static final int budget = 1;
	public static int ordsToExplore = 0;
	public static int nOrders = 0;
	public static String lastNode = null;
	public static boolean recordLastNode = true;

	//for debugging
	public static List<Pair<Integer, Integer>> allDeps = new LinkedList<Pair<Integer, Integer>>();

	public static void initBeforeEachExp(){
		isDepOrdered = false;
		detEvtRetr = false;
		nDetEvtRetr = 0;
		int nPrevPfxs = History.getNPrevPfxs();
		if(nPrevPfxs == 0)
			ordsToExplore = budget;
		else
			ordsToExplore = budget - nPrevPfxs + 1;
		nOrders = 0;

		lastNode = null;
		recordLastNode = true;

		allDeps = new LinkedList<Pair<Integer, Integer>>();
	}


	public static synchronized Pair<Event, String> wakeUpBreadthFirst(){
		Util.MESSAGE("In wakeUpBreadthFirst: Waking up a thread");
		int nEvtBeingWokenUp = Reorder.incEvtBeingWokenUp();
		//int nEvtBeingWokenUp = ReorderReqs.incEvtBeingWokenUp();

		List<Integer> evtOrderPfxToTry = History.getEvtOrderPfxToTry();

		List<Pair<Event, Object>> events = Reorder.getEvents();
		List<Integer> evtOrder = Reorder.getEvtOrder();
		List<String> eNodeOrder = Reorder.getENodeOrder();
		//List<Pair<Event, Object>> events = ReorderReqs.getEvents();
		//List<Integer> evtOrder = ReorderReqs.getEvtOrder();
		//List<String> eNodeOrder = ReorderReqs.getENodeOrder();

		String state = AState.getStateOfLeaders();
		String qstate = AState.getStateOfQueues();

		int pfxToTrySize;
		if(evtOrderPfxToTry == null){
			//return getAnEvt(events, state);	
			pfxToTrySize = 0;
		}
		else{
			pfxToTrySize = evtOrderPfxToTry.size();
		}

		//the reordering of the two events
		boolean b1 = (nEvtBeingWokenUp > pfxToTrySize);
		boolean b2 = (ordsToExplore > 0);
		boolean b3 = (!detEvtRetr);
		if(b1 && b2 && b3){
			Pair<Integer, Integer> idxs = getEvtsToReorder(events, evtOrder, eNodeOrder);

			int idx1 = -1, idx2 = -1;
			if(idxs != null){
				idx1 = idxs.fst();
				idx2 = idxs.snd();
				Pair<Event, Object> ev1 = events.get(idx1);
				Pair<Event, Object> ev2 = events.get(idx2);
				rearrangeDepEvents(events, ev1, ev2);
				ordsToExplore--;
				nOrders++;
				allDeps.add(new Pair<Integer, Integer>(ev1.fst().hashCode(), ev2.fst().hashCode()));

				int nPrevPfxs = History.getNPrevPfxs();
				if(nPrevPfxs == 0){
					String pfxToWr = History.getStrRep(evtOrder);
					Util.stringToFileContent(pfxToWr, FMLogic.PREV_PFX + nOrders, false);
					(History.getPfxsThisExp()).add(new LinkedList<Integer>(evtOrder));
				}
				else{
					if(nOrders > 1){
						String pfxToWr = History.getStrRep(evtOrder);
						Util.stringToFileContent(pfxToWr, FMLogic.PREV_PFX + (nPrevPfxs + nOrders - 1), false);
						(History.getPfxsThisExp()).add(new LinkedList<Integer>(evtOrder));
					}
					//the last pfx to be done has been done, but more events might have been woken up after the exact pfx
					else if(nOrders == 1){
						String s = Util.fileContentToString(FMLogic.PREV_PFX + nPrevPfxs);
						if(s != null){
							s = s.trim();
						}	
						String pfxToWr = History.getStrRep(evtOrder);
						pfxToWr = pfxToWr.trim();
						if(!pfxToWr.equals(s)){
							Util.stringToFileContent(pfxToWr + "\n", FMLogic.PREV_PFX + nPrevPfxs, false);
							List<LinkedList<Integer>> pp = History.getPrevPfxs();
							pp.remove(pp.size() - 1);
							pp.add(new LinkedList<Integer>(evtOrder));
						}	
					}
				}

			}
			else{
				Pair<Event, Object> eWObj = getEventWConstraintsOrDef(events, lastNode);
				//Pair<Event, Object> eWObj = getEventWConstraintsOrDef(events);
				Reorder.letEvtGo(eWObj, true);
				//ReorderReqs.letEvtGo(eWObj, true);
				return (new Pair(eWObj.fst(), state));
			}

		}
		
		if(detEvtRetr){
			Pair<Event, Object> eWObj = events.get(0);
			Reorder.letEvtGo(eWObj, true);
			//ReorderReqs.letEvtGo(eWObj, true);
			nDetEvtRetr++;	

			Event eBeingWokenUp = eWObj.fst();
			if((nDetEvtRetr == 2) && (eBeingWokenUp != null)){
				detEvtRetr = false;
				nDetEvtRetr = 0;
			}
			return (new Pair(eBeingWokenUp, state));
		}
	
		//executing the tail after all reorderings
		if((nEvtBeingWokenUp > pfxToTrySize) && (ordsToExplore == 0)){
			Pair<Event, Object> eWObj = getEventWConstraintsOrDef(events, lastNode);
			//Pair<Event, Object> eWObj = getEventWConstraintsOrDef(events);
			Reorder.letEvtGo(eWObj, true);
			//ReorderReqs.letEvtGo(eWObj, true);
			return (new Pair(eWObj.fst(), state));
		}


		//executing the prefix
		int evt = evtOrderPfxToTry.get(nEvtBeingWokenUp - 1);
		boolean canReproduce = false;
		Pair<Event, Object> eWObj = null;
	
		for(Pair<Event, Object> eobj : events){
			Event et = eobj.fst();
			if(et.hashCode() == evt){
				canReproduce = true;
				eWObj = eobj;
				break;
			}
		}

		if(canReproduce){
			Reorder.letEvtGo(eWObj, true);					
			//ReorderReqs.letEvtGo(eWObj, true);					
			return (new Pair(eWObj.fst(), state));	
		}

		Reorder.wipeOut();
		//ReorderReqs.wipeOut();
		return null;
	}


	private static Pair<Event, String> getAnEvt(List<Pair<Event, Object>> events, String state){
		Pair<Event, Object> e = getEventWConstraintsOrDef(events);
		Reorder.letEvtGo(e, true);
		//ReorderReqs.letEvtGo(e, true);
		return (new Pair(e.fst(), state));
	}


	private static int findEvt(List<Pair<Event, Object>> evts, Integer eHash){
		if(eHash == null)
			return -1;

		for(int i = 0; i < evts.size(); i++){
			Pair<Event, Object> eWObj = evts.get(i);
			Event e = eWObj.fst();
			if(e.hashCode() == eHash){
				return i;
			}	
		}	

		return -1;
	}


	public static void rearrangeDepEvents(List<Pair<Event, Object>> events, Pair<Event, Object> e1, Pair<Event, Object> e2){
		if((e1 == null) || (e2 == null)){
			return;
		}
		
		events.remove(e2);
		events.add(0, e2);
		events.remove(e1);
		events.add(0, e1);

		detEvtRetr = true;
		
		return;		
	}

	public static Pair<Integer, Integer> getEvtsToReorder(List<Pair<Event, Object>> events, List<Integer> evtOrder, 
			List<String> eNodeOrder){
		int sz = events.size();
		for(int i = 0; i < sz; i++){
			for(int j = 0; j < sz; j++){
				if(i == j) continue;
				Pair<Event, Object> ev1 = events.get(i);
				Pair<Event, Object> ev2 = events.get(j);
				
				String state = getCurAbsState();
				String qState = getCurAbsQState();

				Event e1 = ev1.fst();
				Event e2 = ev2.fst();
				boolean isFair1 = isFair(e1, evtOrder, eNodeOrder);
				List<Integer> newEvtOrder = new LinkedList(evtOrder);
				newEvtOrder.add(e1.hashCode());
				List<String> newENodeOrder = new LinkedList(eNodeOrder);
				newENodeOrder.add(e1.getNodeId());
				boolean isFair2 = isFair(e2, newEvtOrder, newENodeOrder);

				boolean dep = areDep((new Integer(e1.hashCode())).toString(), (new Integer(e2.hashCode())).toString(), 
						state, qState, e1, e2);

				if(isFair1 && isFair2 && dep){
					boolean d = done(e1, e2, evtOrder, History.getEvtOrdersInHist());
					if(!d){
						return (new Pair<Integer, Integer>(i, j));
					}
				}
			}
		}

		return null;
	}

	private static String getCurAbsState(){
		String as = null;

		List<String> absStates = Reorder.getAbsStates();
		//List<String> absStates = ReorderReqs.getAbsStates();
		int sz = absStates.size();
		if(sz == 0){
			return "";
		}
		as = absStates.get(sz-1);
		return as;
	}
	
	private static String getCurAbsQState(){
		String qs = null;
		
		List<String> absQStates = Reorder.getAbsQStates();
		//List<String> absQStates = ReorderReqs.getAbsQStates();
		int sz = absQStates.size();
		if(sz == 0){
			return "";
		}
		qs = absQStates.get(sz-1);
		return qs;
	}

	
	public static boolean isFair(Event e, List<Integer> evtOrder, List<String> eNodeOrder){
		String eNode = e.getNodeId();
		int eHash = e.hashCode();

		int sz1 = evtOrder.size();
		int sz2 = eNodeOrder.size();

		//The following condition shouldn't be true
		if(sz1 != sz2){
			return true;
		}

		int nEvts = 0;

		for(int i = (sz2-1); i >= 0; i--){
			String n = eNodeOrder.get(i);
			if(n.equals(eNode)){
				Integer	h = evtOrder.get(i);
				if(h == eHash){
					nEvts++;
					if(nEvts > (kEvtsAllowed-1)){
						return false;
					}
				}
				else{
					return true;
				}
			}
		}

		return true;


	}


	public static boolean done(Event e1, Event e2, List<Integer> evtOrder, List<String> evtOrdersInHist){
		String seq = "";
		if(evtOrder != null){
			seq = History.getStrRep(evtOrder);
		}
		seq = seq.trim();
		if(!seq.equals("")){
			seq += " ";
		}
		seq += e1.hashCode() + " " + e2.hashCode();
		
		for(String s : evtOrdersInHist){
			if(s.startsWith(seq)){
				return true;
			}
		}

		return false;	
	}

       public static boolean areDep(String e1, String e2, String state, String qstate, Event ev1, Event ev2){
	       String[] ss = state.split(":");
	       List<Pair<Long, Long>> sStates = new LinkedList<Pair<Long, Long>>();
	       
	       if(!state.equals("")){
		       for(int i = 0; i < ss.length; i++){
			       Pair<String, String> lz = Parse.getPairOfEntries(ss[i]);
			       long l = Long.parseLong(lz.fst());
			       long z = Long.parseLong(lz.snd());
			       sStates.add(new Pair<Long, Long>(l, z));
		       }
	       }

	       Dep1 d = new Dep1();
	       //Dep2 d = new Dep2();
	       //Dep3 d = new Dep3();
	       //Dep4 d = new Dep4();
	       //Dep5 d = new Dep5();
	       //Dep6 d = new Dep6();
	       //Dep7 d = new Dep7();

	       return d.dep(e1, e2, sStates, qstate, ev1, ev2);
       }


	public static Pair<Event, String> wakeUpRandom(){
		List<Pair<Event, Object>> events = new LinkedList<Pair<Event, Object>>(Reorder.getEvents());
		//List<Pair<Event, Object>> events = new LinkedList<Pair<Event, Object>>(ReorderReqs.getEvents());
		int idx = Reorder.randomGen.nextInt(events.size());
		//int idx = ReorderReqs.randomGen.nextInt(events.size());
		Pair<Event, Object> e = events.get(idx);
		Reorder.letEvtGo(e, true);
		//ReorderReqs.letEvtGo(e, true);
		
		String state = AState.getStateOfLeaders();
		String qstate = AState.getStateOfQueues();
		
		return (new Pair(e.fst(), state));
	}


	public static Event wakeUpDet(){
		List<Pair<Event, Object>> events = new LinkedList<Pair<Event, Object>>(Reorder.getEvents());
		//List<Pair<Event, Object>> events = new LinkedList<Pair<Event, Object>>(ReorderReqs.getEvents());
		
		EventComparator ecomp = new EventComparator();
		List<Pair<Event, Object>> sortedEvts = (new Pair()).sortByFst(events, ecomp);
		
		Pair<Event, Object> e = getEventWConstraintsOrDef(sortedEvts);
		Reorder.letEvtGo(e, true);
		//ReorderReqs.letEvtGo(e, true);
		return e.fst();
	}

	
	public static Pair<Event, String> wakeUpPrioritizedNode(){
		List<Pair<Event, Object>> events = new LinkedList<Pair<Event, Object>>(Reorder.getEvents());
		//List<Pair<Event, Object>> events = new LinkedList<Pair<Event, Object>>(ReorderReqs.getEvents());
		List<Pair<Event, Object>> filEvts = filterByNode(events, "Node-3");
		
		EventComparator ecomp = new EventComparator();
		List<Pair<Event, Object>> sortedEvts; 
		Pair<Event, Object> e = null;	

		if(filEvts.size() > 0){
			sortedEvts = (new Pair()).sortByFst(filEvts, ecomp);
			e = getEventWORepetition(sortedEvts, false);
		}
		
		if(e == null){
			sortedEvts = (new Pair()).sortByFst(events, ecomp);
			e = getEventWORepetition(sortedEvts, true);
		}

		String state = AState.getStateOfLeaders();
		Reorder.letEvtGo(e, true);
		//ReorderReqs.letEvtGo(e, true);
		return (new Pair(e.fst(), state));
	}


	public static List<Pair<Event, Object>> filterByNode(List<Pair<Event, Object>> events, String node){
		List<Pair<Event, Object>> filEvts = new LinkedList<Pair<Event, Object>>();
		for(Pair<Event, Object> e : events){
			Event evt = e.fst();
			String nid = evt.getNodeId();
			if((nid != null) && (nid.equals(node))){
				filEvts.add(e);
			}
		}	
		return filEvts;
	}



	public static int getNumPrevWakeUps(Pair<Event, Object> eWObj){
		Map<String, List<Integer>> nodeToEvtOrder = Reorder.getNodeToEvtOrder();	
		//Map<String, List<Integer>> nodeToEvtOrder = ReorderReqs.getNodeToEvtOrder();	
		
		Event e = eWObj.fst();
		String nodeId = e.getNodeId();
		List<Integer> evtOrderForNode = nodeToEvtOrder.get(nodeId);
		int n = 0;
		if(evtOrderForNode != null){
			int eHash = e.hashCodeWONodeStack();
			int s = evtOrderForNode.size();
			for(int i = (s-1); i >= 0; i--){
				int h = evtOrderForNode.get(i);
				if(h != eHash)
					break;
				else
					n++;
			}
		}
		return n;
	}


	public static boolean allEvtsOfSameType(List<Pair<Event, Object>> events){
		Event.EType lastT = null;	
		boolean allSame = true;
		for(Pair<Event, Object> ev : events){
			Event e = ev.fst();
			Event.EType t = e.t;
			if((lastT != null) && (t != lastT)){
				allSame = false;
				break;
			}
			lastT = t;
		}
		return allSame;
	}	


	public static boolean isETypeSameAsLastEvt(Pair<Event, Object> ewobj){
		if(ewobj == null)
			return false;

		List<Event.EType> typeOrder = Reorder.getETypeOrder();
		//List<Event.EType> typeOrder = ReorderReqs.getETypeOrder();
		int sz = typeOrder.size();
		if(sz < 5){
			return false;
		}
		Event.EType last1 = typeOrder.get(sz-1);
		Event.EType last2 = typeOrder.get(sz-2);
		Event.EType last3 = typeOrder.get(sz-3);
		Event.EType last4 = typeOrder.get(sz-4);

		Event cur = ewobj.fst();
		return ((cur.t == last1) && (last1 == last2) && (last2 == last3) && (last3 == last4));	
			
	}


	public static boolean isPollGoingToWait(Pair<Event, Object> ewobj){
		if(ewobj == null)
			return false;

		Event e = ewobj.fst();
		if(!e.isPollEvent())
			return false;
		QueueContext q = e.getQueue();
		boolean b = FMServer.isEmpty(q);
		return b;
	}


	public static Pair<Event, Object> getEventWConstraintsOrDef(List<Pair<Event, Object>> events, String node){
		if(node == null){
			return getEventWConstraintsOrDef(events);
		}
		else{
			ArrayList<Pair<Event, Object>> evtsToConsider = new ArrayList<Pair<Event, Object>>(events);
			PairComparator pc = new PairComparator();
			Collections.sort(evtsToConsider, pc);
			
			List<Pair<Event, Object>> l = new LinkedList<Pair<Event, Object>>();
			for(Pair<Event, Object> p : evtsToConsider){
				Event e = p.fst();
				String n = e.getNodeId();
				if(node.equals(n)){
					l.add(p);
				}
			}
		
			Pair<Event, Object> eObj = getEventWConstraints(l);
		
			if(eObj == null){
				eObj = getEventWConstraintsOrDef(evtsToConsider);
				resetRecordLastNode();
			}

			return eObj;
		}
	}


	public static Pair<Event, Object> getEventWConstraintsOrDef(List<Pair<Event, Object>> events){
		ArrayList<Pair<Event, Object>> evtsToConsider = new ArrayList<Pair<Event, Object>>(events);
		PairComparator pc = new PairComparator();
		Collections.sort(evtsToConsider, pc);
		
		Pair<Event, Object> e = getEventWConstraints(evtsToConsider);
		
		if((e == null) && (evtsToConsider.size() > 0)){
			e = evtsToConsider.get(0);
		}

		return e;
	}


	//evtsToConsider should be sorted
	public static Pair<Event, Object> getEventWConstraints(List<Pair<Event, Object>> evtsToConsider){
		Pair<Event, Object> e = null;
		
		for(int i = 0; i < evtsToConsider.size(); i++){
			Pair<Event, Object> evt = evtsToConsider.get(i);
			//For fairness of threads (the same timeout event might get woken up again and again otherwise)
			boolean b1 = isPollGoingToWait(evt);
			boolean b2 = allEvtsOfSameType(evtsToConsider);
			boolean b3 = isETypeSameAsLastEvt(evt);
			boolean b4 = (getNumPrevWakeUps(evt) > kEvtsAllowed);
			if(b1 || (!b2 && b3) || b4){
				continue;
			}
			else{
				e = evt;
				break;
			}
		}
	
		return e;	
	}


	public static int getNumPrevEvts(Event e){
		List<Integer> evtOrder = Reorder.getEvtOrder();
		List<Event.EType> eTypeOrder = Reorder.getETypeOrder();
		List<String> eNodeOrder = Reorder.getENodeOrder();
		//List<Integer> evtOrder = ReorderReqs.getEvtOrder();
		//List<Event.EType> eTypeOrder = ReorderReqs.getETypeOrder();
		//List<String> eNodeOrder = ReorderReqs.getENodeOrder();
		
		int sz = evtOrder.size();
		
		int num = 0;
		int eHash = e.hashCode();
		Event.EType eType = e.t;
		boolean checkType = ((eType == Event.EType.POLL) || (eType == Event.EType.SLEEP));
		String eNode = e.getNodeId();
	
			

		for(int i = (sz-1); i >= 0; i--){
			int evt = evtOrder.get(i);
			Event.EType evtType = eTypeOrder.get(i);
			String evtNode = eNodeOrder.get(i);

			if(((checkType && (evtType == eType)) || (!checkType && (evt == eHash))) && (evtNode.equals(eNode))){
				num++;
			}
			else{
				break;
			}
		}

		return num;
	}


	public static Pair<Event, Object> getEventWORepetition(List<Pair<Event, Object>> events, boolean retDefault){
		Pair<Event, Object> evt = null;
		
		for(Pair<Event, Object> e : events){
			Event ev = e.fst();
			
			if(getNumPrevEvts(ev) > 3){
				continue;
			}
			else if(ev.isPollEvent()){
				if(isPollGoingToWait(e)){
					continue;
				}
			}
			
			evt = e;
			break;
		}

		if((evt == null) && !events.isEmpty() && retDefault){
			evt = events.get(0);	
		}

		return evt;
	}


	public static void recordPfxDepInfo(){
		int dsz = allDeps.size();
		List<LinkedList<Integer>> prevPfxs = History.getPrevPfxs();
		List<LinkedList<Integer>> pfxsThisExp = History.getPfxsThisExp();
		int ppsz = prevPfxs.size();
		int pesz = pfxsThisExp.size();

		Util.MESSAGE("In recordPfxDepInfo dsz = " + dsz + " psz = " + (ppsz + pesz));

		String s = "";

		for(int i = 0; i < dsz; i++){
			String ipfx = "";
			if(i == 0){
				if(ppsz != 0){
					ipfx = History.getStrRep(prevPfxs.get(ppsz-1));
				}
				else{
					ipfx = History.getStrRep(pfxsThisExp.get(0));
				}
			}
			else{
				if(ppsz != 0){
					ipfx = History.getStrRep(pfxsThisExp.get(i-1));
				}
				else{
					ipfx = History.getStrRep(pfxsThisExp.get(i));
				}
			}
		
			ipfx = ipfx.trim();	
			Pair<Integer, Integer> dep = allDeps.get(i);
			String idep = dep.toString();

			s += ipfx + " " + dep + "\n";	
		}

		if(s.equals("")){
			s += "\n";
		}

		int n = ((new File(FMLogic.PFX_ORDER_HISTORY_DIR)).listFiles()).length;
	        Util.stringToFileContent(s, FMLogic.PFX_ORDER_HISTORY_DIR + "pfx" + (n+1), false);
	}


	public static int getOrdsToExplore(){
		return ordsToExplore;
	}
	
	public static int getNOrders(){
		return nOrders;
	}

	public static void setLastNode(String n){
		if(getRecordLastNode() == false){
			setRecordLastNode();
			return;
		}

		lastNode = n;
	}

	public static void setRecordLastNode(){
		recordLastNode = true;
	}
	
	public static void resetRecordLastNode(){
		recordLastNode = false;
	}

	public static boolean getRecordLastNode(){
		return recordLastNode;
	}
}

