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

import java.io.IOException;
import java.util.*;

import ditl.*;



public final class BufferEdgesConverter implements Converter, 
	EdgeTrace.Handler, Listener<EdgeEvent> {
	
	private EdgeTrace buffered_edges;
	private EdgeTrace _edges;
	private long before_b_time;
	private long after_b_time;
	private boolean _randomize;
	private Random rng = new Random();
	private StatefulWriter<EdgeEvent,Edge> buffer_writer;
	private Set<Edge> init_state = new AdjacencySet.Edges();
	private Map<Edge,Integer> up_count = new AdjacencyMap.Edges<Integer>();
	private Bus<EdgeEvent> event_bus = new Bus<EdgeEvent>();
	private boolean init_state_set = false;
	private long min_time;
	
	public BufferEdgesConverter(EdgeTrace bufferedEdges, EdgeTrace edges, 
			long beforeBufferTime, long afterBufferTime, boolean randomize){
		buffered_edges = bufferedEdges;
		_edges = edges;
		before_b_time = beforeBufferTime;
		after_b_time = afterBufferTime;
		_randomize = randomize;
		min_time = _edges.minTime();
	}


	@Override
	public void convert() throws IOException {
		buffer_writer = buffered_edges.getWriter(); 
		StatefulReader<EdgeEvent,Edge> edge_reader = _edges.getReader();
		
		edge_reader.stateBus().addListener(edgeListener());
		edge_reader.bus().addListener(edgeEventListener());
		event_bus.addListener(this);
		
		Runner runner = new Runner(_edges.maxUpdateInterval(), _edges.minTime(), _edges.maxTime());
		runner.addGenerator(edge_reader);
		runner.run();
		
		event_bus.flush();
		
		buffer_writer.setPropertiesFromTrace(_edges);
		buffer_writer.close();
		edge_reader.close();
	}

	@Override
	public Listener<EdgeEvent> edgeEventListener() {
		return new Listener<EdgeEvent>(){
			@Override
			public void handle(long time, Collection<EdgeEvent> events) throws IOException {
				for ( EdgeEvent event : events ){
					if ( event.isUp() ){
						event_bus.queue(begin(time), event);
					} else {
						event_bus.queue(end(time), event);
					}
				}
				event_bus.flush(time-before_b_time);
			}
		};
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new Listener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> events) {
				for ( Edge e : events ){
					init_state.add(e);
					incrEdgeCount(e);
				}
			}
		};
	}
	
	private long rand(long dt){
		return Math.abs(rng.nextLong()) % dt;
	}
	
	private long begin(long time){
		if ( _randomize && before_b_time > 0 )
			return time - rand(before_b_time);
		return time - before_b_time;
	}
	
	private long end(long time){
		if ( _randomize && after_b_time > 0 )
			return time + rand(after_b_time);
		return time + after_b_time;
	}
	
	private int incrEdgeCount(Edge e){
		Integer i = up_count.get(e);
		if ( i==null ){
			up_count.put(e, 1);
			return 1;
		}
		up_count.put(e, i+1);
		return i+1;
	}
	
	private int decrEdgeCount(Edge e){
		Integer i = up_count.remove(e);
		if ( i > 1 ){
			up_count.put(e, i-1);
			return i-1;
		}
		return 0;
	}

	@Override
	public void handle(long time, Collection<EdgeEvent> events) throws IOException {
		Deque<EdgeEvent> down_edges = new LinkedList<EdgeEvent>();
		for ( EdgeEvent eev : events ){
			Edge e = eev.edge();
			if ( time < min_time ){
				init_state.add(e);
				incrEdgeCount(e);
			} else {
				if ( ! init_state_set ){
					buffer_writer.setInitState(min_time, init_state);
					init_state_set = true;
				}
				if ( eev.isUp() ){
					if ( incrEdgeCount(e) == 1 ){ // edge just came up
						buffer_writer.append(time, eev);
					}
				} else {
					down_edges.addLast(eev);
				}
			}
		}
		if ( time >= min_time ){
			while ( ! down_edges.isEmpty() ){
				EdgeEvent deev = down_edges.poll();
				if ( decrEdgeCount(deev.edge()) == 0 )
					buffer_writer.append(time, deev);
			}
		}
	}
}
