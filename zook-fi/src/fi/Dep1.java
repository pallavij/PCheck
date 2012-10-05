
package org.fi;

import java.util.*;
import java.io.*;

public class Dep1 implements Dep {
	
	public boolean dep(String e1, String e2, List<Pair<Long, Long>> state, String qstate, 
			Event ev1, Event ev2){

		ParsedEvt pe1 = ParsedEvt.getParsedEvt(ev1, e1);
		ParsedEvt pe2 = ParsedEvt.getParsedEvt(ev2, e2);


		if(pe1.isPollEvent || pe2.isPollEvent)
			return false;
		
		
		boolean sameRecrs = (pe1.receiver).equals(pe2.receiver);
		
		Pair<Long, Long> lz = null;
		int idx = Util.getIdValFromNodeId(pe1.receiver)-1;
		if(idx <= (state.size()-1)){
			lz = state.get(idx);
		}
		long l = -1;
		long z = -1;

		if(lz != null){
			l = lz.fst();
			z = lz.snd();
		}

		if(sameRecrs && (pe1.leader >= l) && (pe2.leader >= l) && 
				(pe1.leader != pe2.leader)){
		//if(sameRecrs && (l1 > l) && (l2 > l) && (l1 != l2)){
			return true;
		}
		
		return false;
	}
        
}

class ParsedEvt{
	boolean isPollEvent;
	boolean isSleepEvent;
	String receiver;
	long leader;
	long zxid;
	long epoch;
	String queue;

	public ParsedEvt(){
		this.isPollEvent = false;
		this.isSleepEvent = false;
		this.receiver = null;
		this.leader = -1;
		this.zxid = -1;
		this.epoch = -1;
		this.queue = null;
	}


	public ParsedEvt(boolean isPollEvent, boolean isSleepEvent, String receiver, long leader, long zxid, long epoch, String queue){
		this.isPollEvent = isPollEvent;
		this.isSleepEvent = isSleepEvent;
		this.receiver = receiver;
		this.leader = leader;
		this.zxid = zxid;
		this.epoch = epoch;
		this.queue = queue;
	}
	
	public static ParsedEvt getParsedEvt(Event evt, String e){
		ParsedEvt pe = new ParsedEvt();

		if(evt != null){
			pe.isPollEvent = evt.isPollEvent();
			pe.isSleepEvent = evt.isSleepEvent();
			pe.receiver = evt.getNodeId();
			if(!pe.isPollEvent){
				pe.leader = evt.mc.vctx.leader;
				pe.zxid = evt.mc.vctx.zxid;
				pe.epoch = evt.mc.vctx.epoch;
			}
			pe.queue = evt.q.toString();
		}
		else{
			pe.isPollEvent = Parse.isPollEvent(e);
			pe.isSleepEvent = Parse.isSleepEvent(e);
			pe.receiver = Parse.getNode(e);
			if(!pe.isPollEvent){
				pe.leader = Parse.getLeader(e);
				pe.zxid = Parse.getZxid(e);
				pe.epoch = Parse.getEpoch(e);
			}
			pe.queue = Parse.getQueue(e);
		}
	
		return pe;
	}
}
