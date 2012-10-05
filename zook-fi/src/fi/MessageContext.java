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
