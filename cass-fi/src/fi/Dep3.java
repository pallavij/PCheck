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

