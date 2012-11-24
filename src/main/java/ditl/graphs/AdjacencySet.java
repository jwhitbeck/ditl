/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
 *                                                                             *
 * DITL is free software: you can redistribute it and/or modify                *
 * it under the terms of the GNU General Public License as published by        *
 * the Free Software Foundation, either version 3 of the License, or           *
 * (at your option) any later version.                                         *
 *                                                                             *
 * DITL is distributed in the hope that it will be useful,                     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
 * GNU General Public License for more details.                                *
 *                                                                             *
 * You should have received a copy of the GNU General Public License           *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
 *******************************************************************************/
package ditl.graphs;

import java.util.*;

import ditl.*;



public abstract class AdjacencySet<C extends Couple> implements Set<C> {
	
	protected AdjacencyMap<C,C> map = null;
	
	@Override
	public void clear(){
		map.clear();
	}
	
	public Set<Integer> getNext(Integer i){
		return Collections.unmodifiableSet(map.getStartsWith(i).keySet());
	}
	
	public Set<Integer> vertices(){
		return map.vertices();
	}
	
	@Override
	public boolean add(C e) {
		return ( map.put(e, e) != null );
	}

	@Override
	public boolean addAll(Collection<? extends C> cs) {
		boolean changed = false;
		for ( C c : cs )
			changed |= add(c);
		return changed;
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean containsAll(Collection<?> cs) {
		for ( Object o : cs )
			if ( ! map.containsKey(o) )
				return false;
		return true;
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<C> iterator() {
		return map.valuesIterator();
	}

	@Override
	public boolean remove(Object o) {
		return (map.remove(o)!=null);
	}

	@Override
	public boolean removeAll(Collection<?> cs) {
		boolean changed = false;
		for ( Object o : cs )
			changed |= remove(o);
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return map.keySet().retainAll(c);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Object[] toArray() {
		return map.values().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return map.values().toArray(a);
	}

	public final static class Links extends AdjacencySet<Link>
		implements LinkTrace.Handler {
		
		public Links(){
			map = new AdjacencyMap.Links<Link>();
		}
		
		@Override
		public Listener<Link> linkListener(){
			return new StatefulListener<Link>() {
				@Override
				public void handle(long time, Collection<Link> events) {
					for ( Link ct : events )
						add(ct);
				}
				@Override
				public void reset() {
					clear();
				}
			};
		}
		
		@Override
		public Listener<LinkEvent> linkEventListener(){
			return new Listener<LinkEvent>() {
				@Override
				public void handle(long time, Collection<LinkEvent> events) {
					for ( LinkEvent cev : events ){
						if ( cev.isUp() )
							add(cev.link());
						else
							remove(cev.link());
					}
				}
			};
		}
	}

	public final static class Edges extends AdjacencySet<Edge>
		implements EdgeTrace.Handler {
		
		public Edges(){
			map = new AdjacencyMap.Edges<Edge>();
		}
		
		@Override
		public Listener<Edge> edgeListener() {
			return new StatefulListener<Edge>() {
				@Override
				public void handle(long time, Collection<Edge> events){
					for ( Edge e : events )
						add(e);
				}
				
				@Override
				public void reset() {
					clear();				
				}
			};
		}
	
		@Override
		public Listener<EdgeEvent> edgeEventListener() {
			return new Listener<EdgeEvent>(){
				@Override
				public void handle(long time, Collection<EdgeEvent> events) {
					for ( EdgeEvent eev : events ){
						if ( eev.isUp() )
							add(eev.edge());
						else 
							remove(eev.edge());
					}
				}
			};
		}
	}
}
