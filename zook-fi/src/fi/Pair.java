package org.fi;

import java.util.*;

public class Pair<T, V>{
	T a;
	V b;
	
	public Pair(){
		this.a = null;
		this.b = null;
	}

        public Pair(T a, V b){
		this.a = a;
                this.b = b;
        }

        public T fst(){
		return a;
        }

        public V snd(){
		return b;
        }

	public String toString(){
		String s = "";
		s += "(" + a.toString() + ", ";
		s += b.toString() + ")";
		return s;
	}

	public boolean equals(Object o){
		if(!(o instanceof Pair))
			return false;
		Pair otherP = (Pair)o;
		Object otherA = otherP.fst();
		Object otherB = otherP.snd();
		
		boolean eqA = (a == null)? (otherA == null) : (a.equals(otherA));
		boolean eqB = (b == null)? (otherB == null) : (b.equals(otherB));

		return (eqA && eqB);
	}

	public int hashCode(){
		int h1 = 0;
		int h2 = 0;

		if(a != null)
			h1 = a.hashCode();
		if(b != null)
			h2 = b.hashCode();

		return (h1<<32 + h2);
	}

	public List<T> getFstElems(List<Pair<T, V>> lst){
		if(lst == null)
			return null;

		List newL = new LinkedList<T>();
		for(Pair<T,V> p : lst){
			newL.add(p.fst());			
		}

		return newL;
	}

	public List<Pair<T, V>> sortByFst(List<Pair<T, V>> lst, Comparator<T> comp){
		List<T> fstElems = getFstElems(lst);
		Collections.sort(fstElems, comp);

		List<Pair<T, V>> res = new LinkedList<Pair<T, V>>();
		T lastTConsidered = null;
		for(T t : fstElems){
			if((lastTConsidered == null) || (!t.equals(lastTConsidered))){
				lastTConsidered = t;
				List<Pair<T, V>> elemsWT = getElemsWFst(lst, t);
				res.addAll(elemsWT);
			}
		}		
		return res;
	}

	private List<Pair<T, V>> getElemsWFst(List<Pair<T, V>> lst, T t){
		List<Pair<T, V>> res = new LinkedList<Pair<T, V>>();
		for(Pair<T, V> e : lst){
			T f = e.fst();
			if(f.equals(t)){
				res.add(e);
			}
		}
		return res;
	}

}


class PairComparator implements Comparator<Pair<Event, Object>>{

	public int compare(Pair<Event, Object> p1, Pair<Event, Object> p2){
		Event e1 = p1.fst();
		Event e2 = p2.fst();

		EventComparator ec = new EventComparator();
		int c = ec.compare(e1, e2);
		if(c != 0)
			return c;

		Object o1 = p1.snd();
		Object o2 = p2.snd();
		int h1 = o1.hashCode();
		int h2 = o2.hashCode();

		if(h1 == h2)
			return 0;
		
		if(h1 < h2)
			return -1;
		else 
			return 1;
	}

}

