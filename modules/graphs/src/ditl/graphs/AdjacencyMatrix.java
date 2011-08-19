/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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



public final class AdjacencyMatrix  
	implements LinkHandler, EdgeHandler {
	
	private Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
	
	private void connect(Integer from, Integer to){
		if ( ! map.containsKey(from) )
			map.put(from, new HashSet<Integer>());
		map.get(from).add(to);
	}
	
	private void disconnect(Integer from, Integer to){
		Set<Integer> neighbs = map.get(from);
		neighbs.remove(to);
		if ( neighbs.isEmpty() )
			map.remove(from);
	}
	
	public void removeAllNeighbors(){
		for ( Set<Integer> neighbors : map.values() )
			neighbors.clear();
	}
	
	public void clear(){
		map.clear();
	}
	
	public Set<Integer> getNext(Integer i){
		return map.get(i);
	}
	
	public void addLink(Link l){
		connect(l.id1(),l.id2());
		connect(l.id2(),l.id1());
	}
	
	public void removeLink(Link l){
		disconnect(l.id1(),l.id2());
		disconnect(l.id2(),l.id1());
	}
	
	public void addEdge(Edge e){
		connect(e.from(), e.to());
	}
	
	public void removeEdge(Edge e){
		disconnect(e.from(), e.to());
	}
	
	public boolean edgeExists(Edge e){
		return map.get(e.from()).contains(e.to());
	}
	
	public Set<Map.Entry<Integer, Set<Integer>>> entries(){
		return map.entrySet();
	}
	
	public Set<Integer> nodes(){
		return map.keySet();
	}
	
	@Override
	public Listener<Link> linkListener(){
		return new StatefulListener<Link>() {
			@Override
			public void handle(long time, Collection<Link> events) {
				for ( Link ct : events )
					addLink(ct);
			}

			@Override
			public void reset() {
				removeAllNeighbors();
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
						addLink(cev.link());
					else
						removeLink(cev.link());
				}
			}
		};
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new StatefulListener<Edge>() {
			@Override
			public void handle(long time, Collection<Edge> events){
				for ( Edge e : events )
					addEdge(e);
			}
			
			@Override
			public void reset() {
				removeAllNeighbors();				
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
						addEdge(eev.edge());
					else 
						removeEdge(eev.edge());
				}
			}
		};
	}
}
