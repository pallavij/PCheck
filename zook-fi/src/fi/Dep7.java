
package org.fi;

import java.util.*;
import java.io.*;

public class Dep7 implements Dep {
	
	public boolean dep(String e1, String e2, List<Pair<Long, Long>> state, String qstate, Event ev1, Event ev2){
		ParsedReq pq1 = ParsedReq.getParsedReq(ev1, e1);
		ParsedReq pq2 = ParsedReq.getParsedReq(ev2, e2);

		boolean sameRecr = (pq1.receiver).equals(pq2.receiver);
		boolean acks = (pq1.isAck() && pq2.isAck());
		boolean proposals = (pq1.isProposal() && pq2.isProposal());
		boolean reqs = (pq1.isRequest() && pq2.isRequest());
		boolean diffzxids = ((pq1.zxid == null) ? (pq2.zxid != null) : !pq1.zxid.equals(pq2.zxid));

		if((sameRecr && diffzxids && acks) || (!sameRecr && diffzxids && proposals) 
				//|| (!diffzxids && pq1.isAck() && pq2.isCommit())
				){
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
			RequestContext rc = evt.rc;
			if(rc != null){
				String pstr = rc.packet;
				if(pstr != null){
					String[] pstrParts = pstr.split(" ");
					pq.type = pstrParts[0];
					pq.zxid = pstrParts[1];
				}
			}
		}
		else{
			pq.receiver = Parse.getNode(e);
			pq.type = Parse.getRequestType(e);
			pq.zxid = Parse.getRequestZxid(e);
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
