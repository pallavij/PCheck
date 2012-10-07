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
