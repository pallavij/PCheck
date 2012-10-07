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

//PALLAVI: used by fleHooks.aj
public class VoteContext implements Comparable{
	long leader;
	long zxid;
	long epoch;
	String stack;

        public VoteContext(long leader, long zxid, long epoch, String stack){
		this.leader = leader;
		this.zxid = zxid;
		this.epoch = epoch;
		this.stack = stack;
	}	
	
	public long getLeader(){
		return leader;
	}
	
	public long getZxid(){
		return zxid;
	}
	
	public long getEpoch(){
		return epoch;
	}

	public String getStack(){
		return stack;
	}

	public String toString(){
		String s = "";
		s += "Leader = " + leader + "\n";
		s += "ZxID = " + zxid + "\n";
		s += "Epoch = " + epoch + "\n";
		s += "Stack = " + stack + "\n";
		return s;
	}

	public String toStringWOStack(){
		String s = "";
		s += "Leader = " + leader + "\n";
		s += "ZxID = " + zxid + "\n";
		s += "Epoch = " + epoch + "\n";
		return s;
	}
	
	public int getHash(){
		String s = toStringWOStack();
		return s.hashCode();
	}

	public boolean equals(Object o){
		if(!(o instanceof VoteContext)){
			return false;
		}
		VoteContext otherVCtx = (VoteContext)o;
	        if((leader == otherVCtx.leader) && (zxid == otherVCtx.zxid) && (epoch == otherVCtx.epoch) 
				//&& (((stack != null) && stack.equals(otherVCtx.stack)) || 
				//	((stack == null) && (otherVCtx.stack == null)))
					){
			return true;
		}	
		return false;
	}

	public int compareTo(Object o){
		if(!(o instanceof VoteContext)){
			return 1;
		}

		VoteContext otherVCtx = (VoteContext)o;
		if(equals(otherVCtx)){
			return 0;
		}

		if(leader != otherVCtx.leader){
			int c = ((leader < otherVCtx.leader)?-1:1);
			return c;
		}
		
		if(zxid != otherVCtx.zxid){
			int c = ((zxid < otherVCtx.zxid)?-1:1);
			return c;
		}
		
		//if(epoch != otherVCtx.epoch){
			int c = ((epoch < otherVCtx.epoch)?-1:1);
			return c;
		//}

		//return stack.compareTo(otherVCtx.stack);
	}
}
