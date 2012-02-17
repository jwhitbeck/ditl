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

import java.io.IOException;
import java.util.*;

import ditl.*;



public final class EdgesToDominatingSetConverter implements Converter,
	EdgeTrace.Handler, PresenceTrace.Handler, Generator, Listener<Object> {
	 
	private StatefulWriter<GroupEvent,Group> group_writer;
	private GroupTrace dominating_set;
	private EdgeTrace _edges;
	private PresenceTrace _presence;
	
	private Set<Integer> ds_nodes = null;
	private Set<Integer> present = new HashSet<Integer>();
	private AdjacencySet.Edges matrix = new AdjacencySet.Edges();
	private AdjacencySet.Edges reverse_matrix = new AdjacencySet.Edges();
	private Bus<Object> update_bus = new Bus<Object>();
	private long min_time;
	private Integer gid = 0;
	
	public EdgesToDominatingSetConverter(GroupTrace dominatingSet, 
			EdgeTrace edges, PresenceTrace presence){
		_edges = edges;
		_presence = presence;
		dominating_set = dominatingSet;
		min_time = _presence.minTime();
	}

		
	@Override
	public void convert() throws IOException{
		StatefulReader<EdgeEvent,Edge> edge_reader = _edges.getReader();
		StatefulReader<PresenceEvent,Presence> presence_reader = _presence.getReader();
		group_writer = dominating_set.getWriter(_edges.snapshotInterval());
		update_bus.addListener(this);
		
		edge_reader.bus().addListener(this.edgeEventListener());
		edge_reader.stateBus().addListener(this.edgeListener());
		presence_reader.bus().addListener(this.presenceEventListener());
		presence_reader.stateBus().addListener(this.presenceListener());
		
		Runner runner = new Runner(_edges.maxUpdateInterval(), _presence.minTime(), _presence.maxTime());
		runner.addGenerator(edge_reader);
		runner.addGenerator(presence_reader);
		runner.addGenerator(this);
		runner.run();
		
		group_writer.setProperty(Trace.ticsPerSecondKey, _edges.ticsPerSecond());
		group_writer.setProperty(GroupTrace.labelsKey, "dominating set");
		group_writer.close();
		edge_reader.close();
		presence_reader.close();
	}



	@Override
	public Listener<EdgeEvent> edgeEventListener() {
		return new Listener<EdgeEvent>(){
			@Override
			public void handle(long time, Collection<EdgeEvent> events){
				for ( EdgeEvent eev : events ){
					Edge e = eev.edge();
					if ( eev.isUp() ){
						matrix.add(e);
						reverse_matrix.add(e.reverse());
					} else {
						matrix.remove(e);
						reverse_matrix.remove(e.reverse());
					}
				}
				scheduleUpdate(time);
			}
		};
	}



	@Override
	public Listener<Edge> edgeListener() {
		return new Listener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> events) {
				for ( Edge e : events ){
					matrix.add(e);
					reverse_matrix.add(e.reverse());
				}
				scheduleUpdate(time);
			}
		};
	}



	@Override
	public Listener<PresenceEvent> presenceEventListener() {
		return new Listener<PresenceEvent>(){
			@Override
			public void handle(long time, Collection<PresenceEvent> events) {
				for ( PresenceEvent pev : events ){
					if ( pev.isIn() ){
						present.add(pev.id());
					} else {
						present.remove(pev.id());
					}
				}
				scheduleUpdate(time);
			}
		};
	}

	

	@Override
	public Listener<Presence> presenceListener() {
		return new Listener<Presence>(){
			@Override
			public void handle(long time, Collection<Presence> events) {
				for ( Presence p : events )
					present.add(p.id());
				scheduleUpdate(time);
			}
		};
	}
	
	class DSCalculator {
		TreeMap<Integer,Set<Integer>> degree_map = new TreeMap<Integer,Set<Integer>>(); 
		Map<Integer,Set<Integer>> remainders = new HashMap<Integer,Set<Integer>>();
		Set<Integer> covered = new HashSet<Integer>();
		Set<Integer> new_ds = new HashSet<Integer>();
		
		DSCalculator() {
			for ( Integer id : present ){
				if ( reverse_matrix.getNext(id) == null ){ // no incoming edges, must be chosen
					new_ds.add(id);
					covered.add(id);
					Set<Integer> dests = matrix.getNext(id);
					if ( dests != null )
						covered.addAll(dests);
				} else { // prepare entry in the remainder map 
					Set<Integer> dests = matrix.getNext(id);
					if ( dests != null ){ // nodes with incoming but no outgoing edges should never be in the dominating set
						Set<Integer> r_dests = new HashSet<Integer>(dests);
						r_dests.removeAll(covered);
						remainders.put(id, r_dests);
						setNodeDegree(id, r_dests.size());
					}
				}
			}
		}
		
		Set<Integer> calculateNewDS(){
			while ( ! allCovered() ){
				pickAlreadyInDS();
			}
			return new_ds;
		}
		
		boolean allCovered(){
			return covered.size() >= present.size(); // covered may be greater than the number of present nodes (e.g., edges in reachability traces)
		}
		
		void pick(Integer node){
			new_ds.add(node);
			Set<Integer> newly_covered = remainders.remove(node);
			newly_covered.add(node);
			degree_map.clear();
			for ( Map.Entry<Integer, Set<Integer>> e : remainders.entrySet()){
				Integer n = e.getKey();
				Set<Integer> dests = e.getValue();
				dests.removeAll(newly_covered);
				setNodeDegree(n, dests.size());
			}
			covered.addAll(newly_covered);
		}
		
		
		void pickAlreadyInDS(){
			Integer id = null;
			Iterator<Integer> i = greedyChoice().iterator();
			while ( i.hasNext() ){
				id = i.next();
				if ( ds_nodes == null )
					break;
				if ( ds_nodes.contains(id) )
					break;
			}
			pick(id);
		}
		
		Set<Integer> greedyChoice(){
			return degree_map.lastEntry().getValue();
		}
		
		void setNodeDegree(Integer node, Integer new_degree ){
			if ( ! degree_map.containsKey(new_degree) )
				degree_map.put(new_degree, new HashSet<Integer>());
			degree_map.get(new_degree).add(node);
		}

	}

	private void scheduleUpdate(long time){
		update_bus.queue(time, Collections.<Object>emptyList());
	}
	
	@Override
	public Bus<?>[] busses() {
		return new Bus<?>[]{update_bus};
	}


	@Override
	public int priority() {
		return Trace.lowestPriority; // this should run with the lowest priority
	}


	@Override
	public void incr(long dt) throws IOException {}


	@Override
	public void seek(long time) throws IOException {}


	@Override
	public void handle(long time, Collection<Object> events) throws IOException {
		DSCalculator calc = new DSCalculator();
		Set<Integer> new_ds_nodes = calc.calculateNewDS();
		if ( time == min_time ){ // set the init state
			ds_nodes = new_ds_nodes;
			Group grp = new Group(gid, new_ds_nodes);
			group_writer.setInitState(min_time, Collections.singleton(grp));
		} else {
			Set<Integer> to_del = new HashSet<Integer>();
			Set<Integer> to_add = new HashSet<Integer>();
			for ( Integer n : ds_nodes )
				if ( ! new_ds_nodes.contains(n) )
					to_del.add(n);
			for ( Integer n : new_ds_nodes )
				if ( ! ds_nodes.contains(n) )
					to_add.add(n);
			if ( ! to_del.isEmpty() )
				group_writer.append(time, new GroupEvent(gid, GroupEvent.LEAVE, to_del));
			if ( ! to_add.isEmpty() )
				group_writer.append(time, new GroupEvent(gid, GroupEvent.JOIN, to_add));
			ds_nodes = new_ds_nodes;
		}
	}
}
