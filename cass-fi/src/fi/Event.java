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

import org.aspectj.lang.reflect.SourceLocation;

public class Event {

	public static enum EType { STATECHANGE, LOCALREAD, NETREAD, SLEEP, POLL; }

	String loc = null;

	EType t;

	//For NETREAD (and LOCALREAD) events
	MessageContext mc;
	RequestContext rc;

	QueueContext q;

	String stack = null;

	String nodeId = null;

	public Event(EType t, MessageContext mc, QueueContext q){
		this.t = t;
		this.mc = mc;
		this.q = q;
	}

	public Event(EType t, MessageContext mc, QueueContext q, String stack){
		this.t = t;
		this.mc = mc;
		this.q = q;
		this.stack = stack;
	}

	public Event(EType t, RequestContext rc, QueueContext q){
		this.t = t;
		this.rc = rc;
		this.q = q;
	}

	public void setSourceLoc(String l){
		this.loc = l;
	}
	
	public void setNodeId(String nid){
		this.nodeId = nid;
	}
	
	public boolean isReadEvent(){
		return ((t == EType.NETREAD) || (t == EType.LOCALREAD));
	}

	public boolean isPollEvent(){
		return (t == EType.POLL);
	}
	
	public boolean isSleepEvent(){
		return (t == EType.SLEEP);
	}

	public QueueContext getQueue(){
		return q;
	}

	public boolean equals(Object o){
		if((o == null) || !(o instanceof Event)) 
			return false;

		Event other = (Event)o;
		
		boolean locEq = ((loc == null) ? (other.loc == null) : (loc.equals(other.loc)));
		if(!locEq) return false;
		
		if(t != other.t) return false;
		
		boolean mcEq = ((mc == null) ? (other.mc == null) : (mc.equals(other.mc)));
		if(!mcEq) return false;

		boolean rcEq = ((rc == null) ? (other.rc == null) : (rc.equals(other.rc)));
		if(!rcEq) return false;
		
		boolean qEq = ((q == null) ? (other.q == null) : (q.equals(other.q)));
		if(!qEq) return false;

		boolean stEq = ((stack == null) ? (other.stack == null) : (stack.equals(other.stack)));

		return stEq;
	}

	public String toString(){
		String s = "";
		s += "TYPE: " + t.toString() + "\n";
		if(nodeId != null){
			s += "NodeId: " + nodeId + "\n";
		}
		if(loc != null){
			s += "LOC: " + loc + "\n";
		}
		if(q != null){
			s += "QUEUE: " + q.toString();
		}
		if(mc != null){
			s += "MESSAGE: " + mc.toString();
		}
		if(rc != null){
			s += "PACKET: " + rc.toString();
		}
		if(stack != null){
			s += "STACK: " + stack;
		}
		return s;
	}
	
	public String toStringWOStack(){
		String s = "";
		s += "TYPE: " + t.toString() + "\n";
		if(nodeId != null){
			s += "NodeId: " + nodeId + "\n";
		}
		if(loc != null){
			s += "LOC: " + loc + "\n";
		}
		if(q != null){
			s += "QUEUE: " + q.toString();
		}
		if(mc != null){
			s += "MESSAGE: " + mc.toStringWOStack();
		}
		if(rc != null){
			s += "PACKET: " + rc.toString();
		}
		return s;
	}
	
	public String toStringWONodeStack(){
		String s = "";
		s += "TYPE: " + t.toString() + "\n";
		if(loc != null){
			s += "LOC: " + loc + "\n";
		}
		if(q != null){
			s += "QUEUE: " + q.toString();
		}
		if(mc != null){
			s += "MESSAGE: " + mc.toStringWONodeStack();
		}
		if(rc != null){
			s += "PACKET: " + rc.toString();
		}
		return s;
	}

	public int hashCode(){
		String s = toStringWOStack();
		return s.hashCode();
	}

	public int hashCodeWONodeStack(){
		String s = toStringWONodeStack();
		return s.hashCode();
	}

	public String getNodeId(){
		if(mc != null)
			return mc.nodeId;
		if(rc != null)
			return rc.nodeId;
		if(q != null)
			return q.nodeId;	
		return nodeId;	
	}
	
	public String getOthrNodeId(){
		if(mc != null)
			return mc.othrNodeId;
		if(rc != null)
			return rc.othrNodeId;
		return null;	
	}
}	

class EventComparator implements Comparator<Event>{
	
	public int compare(Event e1, Event e2){
               if((e1 == null) && (e2 != null)) return -1;
	       if((e2 == null) && (e1 != null)) return 1; 
	       if(((e1 == null) && (e2 == null)) 
			       || e1.equals(e2)){
			return 0;
	       }	      

	       String n1 = e1.getNodeId();
	       String n2 = e2.getNodeId();

	       Event.EType t1 = e1.t;
	       Event.EType t2 = e2.t;
	       
	       MessageContext m1 = e1.mc;
	       MessageContext m2 = e2.mc;

	       RequestContext r1 = e1.rc;
	       RequestContext r2 = e2.rc;
	       
	       QueueContext q1 = e1.q;
	       QueueContext q2 = e2.q;
		
	       String loc1 = e1.loc;
	       String loc2 = e2.loc;
	       
	       String st1 = e1.stack;
	       String st2 = e2.stack;


	       //if((t1 == null) || (m1 == null) || (q1 == null))
	       //	       return -1;

	       if((n1 == null) && (n2 != null)) return -1;	
	       if((n1 != null) && !n1.equals(n2)) return n1.compareTo(n2);

	       if((t1 == null) && (t2 != null)) return -1;	
	       if((t1 != null) && !t1.equals(t2)) return t1.compareTo(t2);

	       if((m1 == null) && (m2 != null)) return -1;	
	       if((m1 != null) && !m1.equals(m2)){
			MessageComparator mcomp = new MessageComparator();
			return mcomp.compare(m1, m2);
	       }
	       
	       if((r1 == null) && (r2 != null)) return -1;	
	       if((r1 != null) && !r1.equals(r2)){
			RequestComparator rcomp = new RequestComparator();
			return rcomp.compare(r1, r2);
	       }

	       if((q1 == null) && (q2 != null)) return -1;	
	       if((q1 != null) && !q1.equals(q2)){
		       QueueComparator qcomp = new QueueComparator();
		       return qcomp.compare(q1, q2);
	       }
	       
	       if((loc1 == null) && (loc2 != null)) return -1;	
	       if((loc1 != null) && !loc1.equals(loc2)) return loc1.compareTo(loc2);

	       if((st1 == null) && (st2 != null)) return -1;	
	       if((st1 != null) && !st1.equals(st2)) return st1.compareTo(st2);
	       
	       return -1;
	}
}
