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

public class Dep7 implements Dep {
	
	public boolean dep(String e1, String e2, List<Pair<Long, Long>> state, String qstate, Event ev1, Event ev2){
		ParsedReq pq1 = ParsedReq.getParsedReq(ev1, e1);
		ParsedReq pq2 = ParsedReq.getParsedReq(ev2, e2);

		boolean sameRecr = (pq1.receiver).equals(pq2.receiver);
		boolean knownR1 = !((pq1.receiver).contains("Unknown"));
		boolean knownR2 = !((pq2.receiver).contains("Unknown"));

		//boolean acks = (pq1.isAck() && pq2.isAck());
		//boolean proposals = (pq1.isProposal() && pq2.isProposal());
		//boolean reqs = (pq1.isRequest() && pq2.isRequest());
		//boolean diffzxids = ((pq1.zxid == null) ? (pq2.zxid != null) : !pq1.zxid.equals(pq2.zxid));

		//if((sameRecr && diffzxids && acks) || (!sameRecr && diffzxids && proposals) 
				//|| (!diffzxids && pq1.isAck() && pq2.isCommit())
		//		){
		//	return true;
		//}
		
		if(sameRecr && knownR1 && knownR2){
			Util.MESSAGE("Returning true in Dep7");
			return true;
		}
		
		return false;
	}
        
}


class ParsedReq{
	String receiver;
	String type;
	String zxid;

	public ParsedReq(){
		this.receiver = null;
		this.type = null;
		this.zxid = null;
	}
	
	public static ParsedReq getParsedReq(Event evt, String e){
		ParsedReq pq = new ParsedReq();

		if(evt != null){
			pq.receiver = evt.getNodeId();
			//RequestContext rc = evt.rc;
			//if(rc != null){
			//	String pstr = rc.packet;
			//	if(pstr != null){
			//		String[] pstrParts = pstr.split(" ");
			//		pq.type = pstrParts[0];
			//		pq.zxid = pstrParts[1];
			//	}
			//}
		}
		else{
			pq.receiver = Parse.getNode(e);
			//pq.type = Parse.getRequestType(e);
			//pq.zxid = Parse.getRequestZxid(e);
		}

		return pq;
	}

	public boolean isAck(){
		return (type.equals("ACK"));
	}
	
	public boolean isProposal(){
		return (type.equals("PROPOSAL"));
	}
	
	public boolean isRequest(){
		return (type.equals("REQUEST"));
	}
	
	public boolean isCommit(){
		return (type.equals("COMMIT"));
	}
}	
