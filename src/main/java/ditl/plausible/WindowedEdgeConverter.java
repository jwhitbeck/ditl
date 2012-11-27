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
package ditl.plausible;

import java.io.IOException;
import java.util.*;

import ditl.*;
import ditl.graphs.*;

public class WindowedEdgeConverter implements Converter, Generator, EdgeTrace.Handler {
	
	private long _window;
	private Bus<Edge> expire_bus = new Bus<Edge>();
	private Bus<Edge> update_bus = new Bus<Edge>();
	private Bus<Edge> pop_bus = new Bus<Edge>();
	private Bus<Edge> state_bus = new Bus<Edge>();
	private Bus<EdgeEvent> bus = new Bus<EdgeEvent>();
	private Map<Edge,EdgeTimeline> edge_timelines = new AdjacencyMap.Edges<EdgeTimeline>();
	
	private StatefulWriter<WindowedEdgeEvent,WindowedEdge> windowed_writer;
	private StatefulReader<EdgeEvent,Edge> edge_reader;
	private WindowedEdgeTrace windowed_edges;
	private EdgeTrace _edges;
	
	private long cur_time;
	private Random rng = new Random();
	
	public WindowedEdgeConverter(WindowedEdgeTrace windowedEdges, EdgeTrace edges, 
			long window ){
		_edges = edges;
		windowed_edges = windowedEdges; 
		_window = window;
	}

	@Override
	public void convert() throws IOException {
		windowed_writer = windowed_edges.getWriter();
		edge_reader = _edges.getReader(0,_window);
		
		edge_reader.setBus(bus);
		edge_reader.setStateBus(state_bus);
		bus.addListener(edgeEventListener());
		state_bus.addListener(edgeListener());
		pop_bus.addListener(popListener());
		expire_bus.addListener(expireListener());
		update_bus.addListener(updateListener());
		
		long minTime = _edges.minTime() - _window;
		long maxTime = _edges.maxTime() + _window;
		
		Runner runner = new Runner(_edges.ticsPerSecond(), minTime, maxTime);
		runner.addGenerator(edge_reader);
		runner.addGenerator(this);
		
		runner.run();
		
		windowed_writer.flush();
		windowed_writer.setProperty(WindowedEdgeTrace.windowLengthKey, _window);
		windowed_writer.setProperty(Trace.maxTimeKey, maxTime);
		windowed_writer.setProperty(Trace.minTimeKey, minTime);
		windowed_writer.setPropertiesFromTrace(_edges);
		windowed_writer.close();
		edge_reader.close();
	}
	
	@Override
	public Bus<?>[] busses() {
		return new Bus<?>[]{ expire_bus, update_bus, pop_bus };
	}

	@Override
	public int priority() {
		return Trace.defaultPriority;
	}

	@Override
	public void incr(long dt) throws IOException {
		windowed_writer.flush(cur_time - _window);
		cur_time += dt;
	}

	@Override
	public void seek(long time) throws IOException {
		cur_time = time;
	}

	@Override
	public Listener<EdgeEvent> edgeEventListener() {
		return new Listener<EdgeEvent>(){
			@Override
			public void handle(long time, Collection<EdgeEvent> events)
					throws IOException {
				// here time is the time in the window-shifted edge event trace
				for ( EdgeEvent eev : events ){
					final Edge e = eev.edge();
					if ( ! edge_timelines.containsKey(e) ){
						edge_timelines.put(e, new EdgeTimeline(e, windowed_writer));
						windowed_writer.queue(time, new WindowedEdgeEvent(e, WindowedEdgeEvent.Type.UP));
					}
					EdgeTimeline timeline = edge_timelines.get(e);
					timeline.append(time+_window, _window, eev);
					
					pop_bus.queue(time+2*_window, e);
					update_bus.queue(time+_window, e);
					if ( eev.isUp() ){
						expire_bus.removeFromQueueAfterTime(time, new Matcher<Edge>(){
							@Override
							public boolean matches(Edge edge) {
								return edge.equals(e);
							}
						});
					} else {
						expire_bus.queue(time+2*_window, e);
					}
				}
			}
		};
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new StatefulListener<Edge>(){

			@Override
			public void reset() {
				edge_timelines.clear();
			}

			@Override
			public void handle(long time, Collection<Edge> edges)
					throws IOException {
				Set<WindowedEdge> init_state = new WindowedEdgeTrace.WindowedEdgesSet();
				for ( Edge e : edges ){
					EdgeTimeline timeline = new EdgeTimeline(e, windowed_writer);
					long delta = (long)(rng.nextDouble()*_window);
					timeline.next_up = time + delta; // random start date for edges that are already up
					timeline.queue(time + delta, new EdgeEvent(e, EdgeEvent.Type.UP));
					pop_bus.queue(time+_window + delta, e);
					update_bus.queue(time+delta, e);
					edge_timelines.put(e, timeline);
					init_state.add(timeline.windowedEdges());
				}
				windowed_writer.setInitState(time, init_state);
			}
		};
	}
	
	public Listener<Edge> updateListener(){
		return new Listener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> edges) throws IOException {
				for ( Edge e : edges ){
					edge_timelines.get(e).update(time);
				}
			}
		};
	}
	
	public Listener<Edge> popListener(){
		return new Listener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> edges) throws IOException {
				for ( Edge e : edges ){
					EdgeTimeline timeline = edge_timelines.get(e);
					if ( timeline != null ){ // it could have been previously expired
						timeline.pop(time, _window);
					}
				}
			}
		};
	}
	
	public Listener<Edge> expireListener(){
		return new Listener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> edges) throws IOException {
				for ( Edge e : edges ){
					edge_timelines.remove(e).expire(time);
				}
			}
		};
	}
}
