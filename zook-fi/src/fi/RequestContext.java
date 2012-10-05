package org.fi;

import java.util.*;
import java.io.*;


public class RequestContext {
	String nodeId;
	String othrNodeId;

	String packet;

	public RequestContext(String nodeId, String othrNodeId, String packet){
		this.nodeId = nodeId;
		this.othrNodeId = othrNodeId;
		this.packet = packet;
	}

	public String toString(){
		String s = "nodeId = " + nodeId + "\n";
		s += "othrNodeId = " + othrNodeId + "\n";
		s += "packet = " + packet + "\n";
		return s;
	}
	
	public String toStringWONode(){
		String s = "othrNodeId = " + othrNodeId + "\n";
		s += "packet = " + packet + "\n";
		return s;
	}

	public int hashCode(){
                String s = toString();
		int hash = s.hashCode();
		if(hash == 0){
			Util.MESSAGE("Hash of RequestContext is 0.");
		}
		return hash;
	}

	public boolean equals(Object o){
		if((o == null) || !(o instanceof RequestContext))
			return false;
		RequestContext otherRC = (RequestContext)o;
		String otherDesc = otherRC.toString();
		String desc = toString();
		return (desc.equals(otherDesc));
	}
}


class RequestComparator implements Comparator<RequestContext>{
	
	public int compare(RequestContext r1, RequestContext r2){
               if((r1 == null) && (r2 != null)) return -1;
	       if((r2 == null) && (r1 != null)) return 1; 
	       if(((r1 == null) && (r2 == null)) 
			       || r1.equals(r2)){
			return 0;
	       }	      

	       String n1 = r1.nodeId;
	       String n2 = r2.nodeId;

	       String othrN1 = r1.othrNodeId;
	       String othrN2 = r2.othrNodeId;

	       String p1 = r1.packet;
	       String p2 = r2.packet;

	       if((n1 == null) || (othrN1 == null)){
			return -1;
	       }

	       if(!n1.equals(n2)){
			return n1.compareTo(n2);
	       }

	       if(!othrN1.equals(othrN2)){
		       return othrN1.compareTo(othrN2);
	       }

	       return p1.compareTo(p2); 
	
	}

}
