
package org.fi;
import java.util.List;


public interface Dep {
	
	public boolean dep(String e1, String e2, List<Pair<Long, Long>> state, String qstate, Event ev1, Event ev2);
        
}

