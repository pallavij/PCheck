package org.fi;

import java.util.*;
import java.io.*;


public class QueueContext{
	String nodeId;
	String srcLn;

	public QueueContext(String nodeId, String srcLn){
		this.nodeId = nodeId;
		this.srcLn = srcLn;
	}

	public String toString(){
		String s = "nodeId = " + nodeId + "\n";
		s += "srcLn = " + srcLn + "\n";
		return s;
	}

	public String toStringWONode(){
		String s = "srcLn = " + srcLn + "\n";
		return s;
	}

	public int hashCode(){
                String s = toString();
		return s.hashCode();
	}

	public boolean equals(Object o){
		if((o == null) || !(o instanceof QueueContext))
			return false;
		QueueContext otherQ = (QueueContext)o;
		String otherQDesc = otherQ.toString();
		String desc = toString();
		return desc.equals(otherQDesc);
	}
}


class QueueComparator implements Comparator<QueueContext>{
	
	public int compare(QueueContext q1, QueueContext q2){
               if((q1 == null) && (q2 != null)) return -1;
	       if((q2 == null) && (q1 != null)) return 1; 
	       if(((q1 == null) && (q2 == null)) 
			       || q1.equals(q2)){
			return 0;
	       }	      

	       String n1 = q1.nodeId;
	       String n2 = q2.nodeId;

	       String l1 = q1.srcLn;
	       String l2 = q2.srcLn;

	       if((n1 == null) || (l1 == null)){
			return -1;
	       }

	       if(!n1.equals(n2)){
			return n1.compareTo(n2);
	       }

	       return l1.compareTo(l2);
	}
}
