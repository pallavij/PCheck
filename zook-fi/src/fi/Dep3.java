
package org.fi;

import java.util.*;
import java.io.*;

public class Dep3 implements Dep {
	
	public boolean dep(String e1, String e2, List<Pair<Long, Long>> state, String qstate, Event ev1, Event ev2){
		ParsedEvt pe1 = ParsedEvt.getParsedEvt(ev1, e1);
		ParsedEvt pe2 = ParsedEvt.getParsedEvt(ev2, e2);

	
		if(!(pe1.isPollEvent && !pe2.isPollEvent))
			return false;
		
		String r1 = pe1.receiver;
		String r2 = pe2.receiver;

		String q1 = pe1.queue;
		String q2 = pe2.queue;
		
		if((q1 == null) || (q2 == null))
			return false;

		boolean sameRecrs = r1.equals(r2);
		boolean sameQueues = q1.equals(q2);
		boolean isQueueEmpty = false;

		if(!qstate.equals("")){
			int q1h = q1.hashCode();
		
			String[] qstateParts = qstate.split(" ");
			boolean foundQ = false;
			for(String sp : qstateParts){
				Pair<String, String> qn = Parse.getPairOfEntries(sp);
				int qh = Integer.parseInt(qn.fst());
				int n = Integer.parseInt(qn.snd());
				if(qh == q1h){
					foundQ = true;
					if(n == 0){
						isQueueEmpty = true;
						break;
					}
				}
			}

			if(!foundQ){
				isQueueEmpty = true;
			}
		}
		else{
			isQueueEmpty = true;
		}


		//if(sameRecrs && sameQueues && isQueueEmpty){
		if(sameRecrs && isQueueEmpty){
			return true;
		}
		
		return false;
	}
        
}

