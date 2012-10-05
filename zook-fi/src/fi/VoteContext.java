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
