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


public class FloodingReachableConverter implements 
	LinkTrace.Handler, PresenceTrace.Handler, Generator, Converter {

	private long _tau;
	private long _delay;
	private long min_time;
	
	private StatefulWriter<EdgeEvent,Edge> edge_writer;
	
	private Set<Edge> state = new TreeSet<Edge>();
	private Set<Integer> present = new HashSet<Integer>();
	private AdjacencySet.Edges rev_matrix = new AdjacencySet.Edges();
	private AdjacencySet.Links matrix = new AdjacencySet.Links();
	private boolean started = false;
	
	private LinkTrace _links;
	private PresenceTrace _presence;
	private ReachabilityTrace _reachability;
	
	private Bus<Object> update_bus = new Bus<Object>();
	private Bus<Infection> infection_bus = new Bus<Infection>();
	
	public FloodingReachableConverter(ReachabilityTrace reachability, PresenceTrace presence,
			LinkTrace links, long tau, long period, long minTime) {
		min_time = minTime;
		_links = links;
		_presence = presence;
		_reachability = reachability;
		_tau = tau;
		_delay = period;
		update_bus.addListener(new UpdateListener());
		infection_bus.addListener(new InfectionListener());
	}

	@Override
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>() {
			@Override
			public void handle(long time, Collection<LinkEvent> events) throws IOException {
				for ( LinkEvent lev : events ){
					Link l = lev.link();
					if ( lev.isUp() ){
						matrix.add(l);
						Set<Integer> already_inf_1 = rev_matrix.getNext(l.id1());
						Set<Integer> already_inf_2 = rev_matrix.getNext(l.id2());
						if ( already_inf_1 != null ){
							Edge e = new Edge(l.id1(), l.id2());
							for ( Integer orig : already_inf_1 ){
								if ( already_inf_2 == null || ! already_inf_2.contains(orig) ){
									infection_bus.queue(time+_tau, new Infection(orig, e));
								}
							}
						}
						if ( already_inf_2 != null ){
							Edge e = new Edge(l.id2(), l.id1() );
							for ( Integer orig : already_inf_2 ){
								if ( already_inf_1 == null || ! already_inf_1.contains(orig) ){
									infection_bus.queue(time+_tau, new Infection(orig, e));
								}
							}
						} 	
					} else {
						matrix.remove(l);
						infection_bus.removeFromQueueAfterTime(time, new LinkMatcher(l));
					}
				}
			}
		};
	}
	

	@Override
	public Listener<Link> linkListener() {
		return new Listener<Link>(){
			@Override
			public void handle(long time, Collection<Link> events) {
				for ( Link l : events ){
					matrix.add(l);
				}
			}
		};
	}
	
	
	@Override
	public void incr(long time) throws IOException {}
	
	
	@Override
	public void seek(long time) throws IOException {
		update_bus.queue(min_time, Collections.emptySet());
	}
	
	@Override
	public void convert() throws IOException {
		StatefulReader<LinkEvent,Link> link_reader = _links.getReader();
		StatefulReader<PresenceEvent,Presence> presence_reader = _presence.getReader();
		
		edge_writer = _reachability.getWriter();
		
		edge_writer.setProperty(ReachabilityTrace.delayKey, _delay);
		edge_writer.setProperty(Trace.ticsPerSecondKey, _links.ticsPerSecond());
		edge_writer.setProperty(ReachabilityTrace.tauKey, _tau);
		edge_writer.setProperty(Trace.minTimeKey, _links.minTime());
		edge_writer.setProperty(Trace.maxTimeKey, _links.maxTime());
		
		link_reader.stateBus().addListener(linkListener());
		link_reader.bus().addListener(linkEventListener());
		
		presence_reader.stateBus().addListener(presenceListener());
		presence_reader.bus().addListener(presenceEventListener());
		
		Runner runner = new Runner(_tau, _links.minTime(), _links.maxTime());
		runner.addGenerator(presence_reader);
		runner.addGenerator(link_reader);
		runner.addGenerator(this);
		runner.run();
		
		edge_writer.flush();
		edge_writer.close();
		link_reader.close();
		presence_reader.close();
	}

	@Override
	public Bus<?>[] busses() {
		return new Bus<?>[]{ infection_bus, update_bus };
	}

	@Override
	public int priority() {
		return Integer.MAX_VALUE; // this should come after all other events
	}
	
	private final static class Infection {
		Integer _orig;
		Edge _edge;
		Infection(Integer orig, Edge edge){ _orig = orig; _edge = edge; }
		Edge edge(){ return new Edge(_orig,_edge.to()); }
		Integer rcpt(){ return _edge.to(); }
		@Override
		public String toString(){ return edge().toString(); }
	}
	
	private final class UpdateListener implements Listener<Object> {
		@Override
		public void handle(long time, Collection<Object> events) throws IOException {
			if ( started ){
				// first handle previous time period
				long t = time - _delay;
				if ( t == _links.minTime() ){ // this should be the initial state  
					edge_writer.setInitState(min_time, state);
				} else {
					Set<Edge> cur_state = edge_writer.states();
					for ( Edge e : state ){
						if ( ! cur_state.contains(e) )
							edge_writer.queue(t, new EdgeEvent(e, EdgeEvent.UP));
					}
					for ( Edge e : cur_state ){
						if ( ! state.contains(e) )
							edge_writer.queue(t, new EdgeEvent(e, EdgeEvent.DOWN));
					}
					edge_writer.flush();
				}
			
				// then clear state and start new epidemic
				state.clear();
				rev_matrix.clear();
				infection_bus.reset();
			} else {
				started = true;
				if ( min_time > _links.minTime() ) // starting after min_time => empty initial state
					edge_writer.setInitState(_links.minTime(), Collections.<Edge>emptySet());
			}
			
			for ( Integer i : present ){
				rev_matrix.add(new Edge(i,i));
				broadcast(time, i, i);
			}
			
			update_bus.queue(time+_delay, Collections.emptySet());
		}
	}
	
	private final class InfectionListener implements Listener<Infection> {
		@Override
		public void handle(long time, Collection<Infection> events) throws IOException {
			for ( Infection infection : events ){
				Edge e = infection.edge();
				state.add(e);
				rev_matrix.add(e.reverse());
				infection_bus.removeFromQueueAfterTime(time, new TransferMatcher(infection.rcpt(), infection._orig));
				broadcast(time, infection.rcpt(), infection._orig);
			}
		}
	}
	
	private void broadcast(long time, Integer id, Integer orig){
		Set<Integer> neighbs = matrix.getNext(id);
		if ( neighbs != null ){
			for ( Integer i : neighbs ){
				Edge e = new Edge(orig,i);
				if ( ! state.contains(e) ){
					Infection inf = new Infection( orig, new Edge(id,i) );
					infection_bus.queue(time+_tau, inf);
				}
			}
		}
	}
	
	private final class LinkMatcher implements Matcher<Infection> {
		Link _link;
		LinkMatcher(Link link){ _link = link; }
		@Override
		public boolean matches(Infection item) {
			return item._edge.link().equals(_link);
		}
	}
	
	private final class NodeMatcher implements Matcher<Infection> {
		Integer _id;
		NodeMatcher(Integer id){ _id = id; }
		@Override
		public boolean matches(Infection item) {
			return item._edge.from().equals(_id) || item._edge.to().equals(_id); 
		}
	}
	
	private final class TransferMatcher implements Matcher<Infection> {
		Integer _id;
		Integer _orig;
		TransferMatcher(Integer id, Integer orig){ _id = id; _orig = orig; }
		@Override
		public boolean matches(Infection item) {
			return _orig.equals(item._orig) && item._edge.to().equals(_id); 
		}
	}

	@Override
	public Listener<Presence> presenceListener() {
		return new Listener<Presence>(){
			@Override
			public void handle(long time, Collection<Presence> events) {
				for ( Presence p : events )
					present.add(p.id());
			}
		};
	}

	@Override
	public Listener<PresenceEvent> presenceEventListener() {
		return new Listener<PresenceEvent>(){
			@Override
			public void handle(long time, Collection<PresenceEvent> events) {
				for ( PresenceEvent pev : events ){
					Integer id = pev.id();
					if ( pev.isIn() ){
						present.add(id);
						rev_matrix.add(new Edge(id,id));
						broadcast(time, id, id);
					} else {
						present.remove(id);
						infection_bus.removeFromQueueAfterTime(time, new NodeMatcher(id));
					}
				}
			}
		};
	}
}
