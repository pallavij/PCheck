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

public class Parse{

	public static Map<String, Boolean> isPollMap = new HashMap<String, Boolean>();
	public static Map<String, Boolean> isSleepMap = new HashMap<String, Boolean>();
	public static Map<String, String> eventToNodeMap = new HashMap<String, String>();
	public static Map<String, Long> eventToLeaderMap = new HashMap<String, Long>();
	public static Map<String, Long> eventToZxidMap = new HashMap<String, Long>();
	public static Map<String, Long> eventToEpochMap = new HashMap<String, Long>();


	public static boolean isPollEvent(String e){
		return isEvtOfGivenType(e, "TYPE: POLL", isPollMap);
	}

	
	public static boolean isSleepEvent(String e){
		return isEvtOfGivenType(e, "TYPE: SLEEP", isSleepMap);
	}


	public static boolean isEvtOfGivenType(String e, String type, Map<String, Boolean> typeM){
		Boolean b = typeM.get(e);
		if(b != null)
			return b;

		String fName = "h" + e + ".txt";
		File f = new File(FMLogic.COVERAGE_COMPLETE_DIR + fName);
		String fStr = Util.fileContentToString(f);
		if(fStr != null){
			boolean p = (fStr.contains(type));
			typeM.put(e, p);
			return p;
		}
		return false;
	}


	public static String getNode(String e){
		if(eventToNodeMap.containsKey(e)){
			String n = eventToNodeMap.get(e);
			return n;
		}

		String v = getFieldVal(e, "nodeId = ");
		if(v == null){
			v = getFieldVal(e, "NodeId: ");
		}
		eventToNodeMap.put(e, v);
		return v;
	}


	public static String getFieldVal(String e, String sToFind){
		String fName = "h" + e + ".txt";
		File f = new File(FMLogic.COVERAGE_COMPLETE_DIR + fName);
		String fStr = Util.fileContentToString(f);
		String n = sToFind;
		if(fStr != null){
			String[] fStrParts = fStr.split("\\n");
			for(String s : fStrParts){
				if(s.contains(n)){
					s = s.trim();
					int idx = s.indexOf(n);
					return s.substring(idx + n.length(), s.length());
				}
			}		
		}
		return null;
	}

	public static long getLeader(String e){
		if(eventToLeaderMap.containsKey(e)){
			return eventToLeaderMap.get(e);
		}

		String l = getFieldVal(e, "Leader = ");
		long lVal = -1;
		if(l != null){
			lVal = Long.parseLong(l);
		}
		eventToLeaderMap.put(e, lVal);
		return lVal;
	}

	public static long getZxid(String e){
		if(eventToZxidMap.containsKey(e)){
			return eventToZxidMap.get(e);
		}
		
		String l = getFieldVal(e, "ZxID = ");
		long zVal = -1;
		if(l != null){
			zVal = Long.parseLong(l);
		}
		eventToZxidMap.put(e, zVal);
		return zVal;
	}

	public static long getEpoch(String e){
		if(eventToEpochMap.containsKey(e)){
			return eventToEpochMap.get(e);
		}

		String ep = getFieldVal(e, "Epoch = ");
		long epVal = -1;
		if(ep != null){
			epVal = Long.parseLong(ep);
		}
		eventToEpochMap.put(e, epVal);
		return epVal;
	}


	public static String getQueue(String e){
		String fName = "h" + e + ".txt";
		File f = new File(FMLogic.COVERAGE_COMPLETE_DIR + fName);
		String fStr = Util.fileContentToString(f);

		if(fStr != null){
			String[] fStrParts = fStr.split("\\n");
			boolean start = false;
			String q = "";	
			String n = "QUEUE: ";

			for(String s : fStrParts){
				if(s.contains(n)){
					q += s.substring(n.length(), s.length());
					q += "\n";
					start = true;
				}
				else{
					if(start){
						if(s.contains("MESSAGE: ") || s.contains("STACK: ")){
							break;
						}
						q += s;
						q += "\n";
					}
				}
			}

			return q;
		}

		return null;
	}



	public static Pair<String, String> getPairOfEntries(String s){
		String[] entries = s.split(",");
		String entry1 = (entries[0]).substring(1);
		String entry2 = (entries[1]).substring(0, entries[1].length() - 1);
		return (new Pair(entry1, entry2));
	}

		
	public static Pair<String, String[]> getEvtQueue(String s){
		String[] sParts = s.split(":");
		String e = sParts[0];
		e = e.trim();
		if(sParts.length > 1){
			String q = sParts[1];
			q = q.trim();
			String[] qParts = q.split(" ");
			return (new Pair<String, String[]>(e, qParts));
		}
		else{
			return (new Pair<String, String[]>(e, new String[0]));
		}
	}


	public static String getRequestType(String s){
		String pstr = getFieldVal(s, "packet = ");
		if(pstr != null){
			String[] pstrParts = pstr.split(" ");
			return pstrParts[0];
		}
		return null;
	}

	
	public static String getRequestZxid(String s){
		String pstr = getFieldVal(s, "packet = ");
		if(pstr != null){
			String[] pstrParts = pstr.split(" ");
			return pstrParts[1];
		}
		return null;
	}

}

