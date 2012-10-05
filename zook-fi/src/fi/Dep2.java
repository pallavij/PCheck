
package org.fi;

import java.util.*;
import java.io.*;

public class Dep2 implements Dep {
	
	public boolean dep(String e1, String e2, List<Pair<Long, Long>> state, String qstate, Event ev1, Event ev2){
		ParsedEvt pe1 = ParsedEvt.getParsedEvt(ev1, e1);
		ParsedEvt pe2 = ParsedEvt.getParsedEvt(ev2, e2);


		if(pe1.isPollEvent || pe2.isPollEvent)
			return false;
		
		
		boolean sameRecrs = (pe1.receiver).equals(pe2.receiver);
		
		if(sameRecrs && ((pe1.leader != pe2.leader) || (pe1.zxid != pe2.zxid) || 
					(pe1.epoch != pe2.epoch))){
			return true;
		}
		
		return false;
	}
        
}

