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


public class MessageContext {
	String nodeId;
	String othrNodeId;
	VoteContext vctx;

	public MessageContext(String nodeId, String othrNodeId, VoteContext vctx){
		this.nodeId = nodeId;
		this.othrNodeId = othrNodeId;
		this.vctx = vctx;
	}

	public String toString(){
		String s = "nodeId = " + nodeId + "\n";
		s += "othrNodeId = " + othrNodeId + "\n";
		s += vctx.toString();
		return s;
	}
	
	public String toStringWOStack(){
		String s = "nodeId = " + nodeId + "\n";
		s += "othrNodeId = " + othrNodeId + "\n";
		s += vctx.toStringWOStack();
		return s;
	}
	
	public String toStringWONodeStack(){
		String s = "othrNodeId = " + othrNodeId + "\n";
		s += vctx.toStringWOStack();
		return s;
	}

	public int hashCode(){
                String s = toStringWOStack();
		int hash = s.hashCode();
		if(hash == 0){
			Util.MESSAGE("Hash of MessageContext is 0.");
		}
		return hash;
	}

	public boolean equals(Object o){
		if((o == null) || !(o instanceof MessageContext))
			return false;
		MessageContext otherMC = (MessageContext)o;
		String otherDesc = otherMC.toString();
		String desc = toString();
		return (desc.equals(otherDesc));
	}
}


class MessageComparator implements Comparator<MessageContext>{
	
	public int compare(MessageContext m1, MessageContext m2){
               if((m1 == null) && (m2 != null)) return -1;
	       if((m2 == null) && (m1 != null)) return 1; 
	       if(((m1 == null) && (m2 == null)) 
			       || m1.equals(m2)){
			return 0;
	       }	      

	       String n1 = m1.nodeId;
	       String n2 = m2.nodeId;

	       VoteContext v1 = m1.vctx;
	       VoteContext v2 = m2.vctx;

	       String othrN1 = m1.othrNodeId;
	       String othrN2 = m2.othrNodeId;

	       if((n1 == null) || (v1 == null) || (othrN1 == null)){
			return -1;
	       }

	       if(!n1.equals(n2)){
			return n1.compareTo(n2);
	       }

	       if(!othrN1.equals(othrN2)){
			return othrN1.compareTo(othrN2);
	       }
	       
	       return v1.compareTo(v2);
	
	}

	/*****************
	public boolean equals(MessageContext m1, MessageContext m2){
	        String n1 = m1.nodeId;
	        String n2 = m2.nodeId;

	        VoteContext v1 = m1.vctx;
	        VoteContext v2 = m2.vctx;

	        String othrN1 = m1.othrNodeId;
	        String othrN2 = m2.othrNodeId;
	
       
	        if((n1 != null) && n1.equals(n2) && (v1 != null) && v1.equals(v2) 
				&& (othrN1 != null) && othrN1.equals(othrN2)){
			return true;
	        }
		
		return false;
	}
	******************/
}
