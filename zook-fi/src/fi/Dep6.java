
package org.fi;

import java.util.*;
import java.io.*;

public class Dep6 implements Dep {
	
	public boolean dep(String e1, String e2, List<Pair<Long, Long>> state, String qstate, 
			Event ev1, Event ev2){

		ParsedEvt pe1 = ParsedEvt.getParsedEvt(ev1, e1);
		ParsedEvt pe2 = ParsedEvt.getParsedEvt(ev2, e2);

		if(!pe1.isSleepEvent && !pe2.isSleepEvent)
			return false;
		
		return true;
	}
        
}
