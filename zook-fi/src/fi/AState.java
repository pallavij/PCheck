
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

