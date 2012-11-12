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

                /*********
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
                **********/
		//else{
			pe.isPollEvent = Parse.isPollEvent(e);
			pe.isSleepEvent = Parse.isSleepEvent(e);
			pe.receiver = Parse.getNode(e);
			if(!pe.isPollEvent){
				pe.leader = Parse.getLeader(e);
				pe.zxid = Parse.getZxid(e);
				pe.epoch = Parse.getEpoch(e);
			}
			pe.queue = Parse.getQueue(e);
		//}
	
		return pe;
	}
}
