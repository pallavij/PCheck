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

public class AState {
	public static Map<Integer, Pair<Long, Long>> currentLeaders = 
	          new HashMap<Integer, Pair<Long, Long>>();
	public static Map<Integer, Integer> qToNumElems = 
		  new HashMap<Integer, Integer>();


	public static String getStateOfLeaders(){
		synchronized(currentLeaders){
			String s = "";
			
			for(int i = 1; i <= FMLogic.nZkServers; i++){
				Pair<Long, Long> lz = getLeaderZxid(i);
				s += "(" + lz.fst() + "," + lz.snd() + "):";
			}

			s = s.substring(0, s.length()-1);
			return s;
		}
	}

	public static String getStateOfQueues(){
		synchronized(qToNumElems){
			String qState = "";
			Set<Integer> qs = getQueues();
			for(Integer qh : qs){
				int qn = getNElemsInQ(qh);
				qState += "(" + qh  + "," + qn + ")";
				qState += " ";
			}
			qState = qState.trim();
			return qState;
		}
	}

	public static void clearState(){
		clearLeaders();
		clearQueues();
	}	


	public static void setChangedVCtx(int sid, long leader, long zxid){
		synchronized(currentLeaders){
			Util.MESSAGE("Changing leader in server " + sid + ". New leader is " + leader);
			currentLeaders.put(sid, new Pair<Long, Long>(leader, zxid));
		}
	}


	public static Pair<Long, Long> getLeaderZxid(int sid){
	        synchronized(currentLeaders){
			Pair<Long, Long> lz = (Pair<Long, Long>)currentLeaders.get(sid);
			if(lz != null)
				return lz;
			else
				return (new Pair(-1, -1));
		}
	}


	public static long getLeader(int sid){
	        synchronized(currentLeaders){
			Pair<Long, Long> leaderZxid = (Pair<Long, Long>)currentLeaders.get(sid);
			if(leaderZxid == null)
				return -1;
			return leaderZxid.fst();
		}
	}

	public static long getZxid(int sid){
		synchronized(currentLeaders){
			Pair<Long, Long> leaderZxid = (Pair<Long, Long>)currentLeaders.get(sid);
			if(leaderZxid == null)
				return -1;
			return leaderZxid.snd();
		}
        }

	public static void clearLeaders(){
		synchronized(currentLeaders){
			currentLeaders.clear();
		}
	}


	public static void setChangedQueue(QueueContext q, int nElems){
		int h = q.hashCode();
		synchronized(qToNumElems){
			if(nElems != 0){
				qToNumElems.put(h, nElems);
			}
			else{
				qToNumElems.remove(h);
			}
		}	
	}

	public static int getNElemsInQ(QueueContext q){
		int h = q.hashCode();
		return getNElemsInQ(h);
	}


	public static int getNElemsInQ(int qh){
		Integer n = null;
		synchronized(qToNumElems){	
			n = qToNumElems.get(qh);
		}
		if(n == null){
			return 0;
		}
		return n.intValue();
	}

	public static Set<Integer> getQueues(){
		synchronized(qToNumElems){	
			Set<Integer> qs = qToNumElems.keySet();
			return qs;
		}	
	}	

	public static void clearQueues(){
		synchronized(qToNumElems){
			qToNumElems.clear();
		}
	}
        
}

