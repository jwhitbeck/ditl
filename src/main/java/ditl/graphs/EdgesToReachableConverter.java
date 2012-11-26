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


public class EdgesToReachableConverter implements EdgeTrace.Handler, Generator, Converter {

	private long _tau;
	private long _delay;
	private long _eta;
	private long min_time;
	private long cur_time;
	private StatefulWriter<ArcEvent,Arc> arc_writer;
	private Set<Arc> init_state = new AdjacencySet.Arcs();
	private EdgeTrace _edges;
	private ReachabilityTrace _reachability;
	private Bus<Edge> edge_bus = new Bus<Edge>();
	private Bus<ArcEvent> outbus = new Bus<ArcEvent>();
	private Set<Edge> cur_edges = new AdjacencySet.Edges();
	
	public EdgesToReachableConverter(ReachabilityTrace reachability, EdgeTrace edges, long eta, long tau, long delay) {
		min_time = edges.minTime();
		_edges = edges;
		_reachability = reachability;
		_tau = tau;
		_delay = delay;
		_eta = eta;
		edge_bus.addListener(new EdgeListener());
		outbus.addListener(new Outputer());
	}

	@Override
	public Listener<EdgeEvent> edgeEventListener() {
		return new Listener<EdgeEvent>() {
			@Override
			public void handle(long time, Collection<EdgeEvent> events) throws IOException {
				for ( EdgeEvent eev : events ){
					final Edge e = eev.edge();
					if ( eev.isUp() ){
						edge_bus.queue(time+_tau,e);
					} else {
						if ( cur_edges.contains(e) ){
							fire(time-_tau, e, false);
							cur_edges.remove(e);
						} else {
							edge_bus.removeFromQueueAfterTime(time-_tau, new Matcher<Edge>(){
								@Override
								public boolean matches(Edge item) {
									return item.equals(e);
								}
							});
						}
					}
				}
			}
		};
	}
	
	private void fire(long time, Edge e, boolean up) {
		outbus.queue(time, new ArcEvent(e.id1(),e.id2(),up));
		outbus.queue(time, new ArcEvent(e.id2(),e.id1(),up));
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new Listener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> events) {
				for ( Edge e : events ){
					edge_bus.queue(time+_tau, e);
				}
			}
		};
	}
	
	@Override
	public void incr(long dt) throws IOException {
		outbus.flush(cur_time - _delay - _tau);
		cur_time += dt;
	}

	@Override
	public void seek(long time) throws IOException {
		cur_time = time;
	}
	
	@Override
	public void convert() throws IOException {
		StatefulReader<EdgeEvent,Edge> edge_reader = _edges.getReader();
		arc_writer = _reachability.getWriter();
	
		arc_writer.setProperty(ReachabilityTrace.delayKey, _delay);
		arc_writer.setProperty(ReachabilityTrace.tauKey, _tau);
		arc_writer.setProperty(ReachabilityTrace.etaKey, _eta);
		arc_writer.setPropertiesFromTrace(_edges);
		
		min_time = _edges.minTime();
		
		if ( _delay >= _tau ){
			edge_reader.stateBus().addListener(edgeListener());
			edge_reader.bus().addListener(edgeEventListener());
		
			Runner runner = new Runner(_tau, _edges.minTime(), _edges.maxTime()+1);
			runner.addGenerator(edge_reader);
			runner.addGenerator(this);
			runner.run();
			edge_bus.flush(_edges.maxTime());
			outbus.flush(_edges.maxTime());
		} else {
			arc_writer.setInitState(min_time, Collections.<Arc>emptySet());
		}
		arc_writer.close();
		edge_reader.close();
	}

	@Override
	public Bus<?>[] busses() {
		return new Bus<?>[]{ edge_bus };
	}

	@Override
	public int priority() {
		return Trace.highestPriority;
	}

	private final class EdgeListener implements Listener<Edge> {
		@Override
		public void handle(long time, Collection<Edge> events) {
			for ( Edge e : events ){
				cur_edges.add(e);
				fire(time-_delay, e, true);
			}
		}
	}
	
	private final class Outputer implements Listener<ArcEvent> {
		@Override
		public void handle(long time, Collection<ArcEvent> events)
				throws IOException {
			if ( init_state != null && time >= min_time ){
				arc_writer.setInitState(min_time, init_state);
				init_state = null;
			}
			Deque<ArcEvent> down_events = new LinkedList<ArcEvent>();
			for ( ArcEvent aev : events ){
				if ( init_state != null ){
					if ( aev.isUp() )
						init_state.add(aev.arc());
				} else {
					if ( aev.isUp() ){
						arc_writer.append(time, aev);
					} else {
						down_events.addLast(aev);
					}
				}
			}
			while ( ! down_events.isEmpty() )
				arc_writer.append(time, down_events.poll());
		}
	}
}
