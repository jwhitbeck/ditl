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



public final class StrictEdgesToPresenceConverter implements Converter, EdgeTrace.Handler{
	
	private PresenceTrace _presence;
	private EdgeTrace _edges;
	
	private Map<Integer, Long> exit_times = new HashMap<Integer, Long>();
	private Set<Integer> seen_nodes = new HashSet<Integer>();
	private StatefulWriter<PresenceEvent,Presence> presence_writer;
	
	public StrictEdgesToPresenceConverter(PresenceTrace presence, EdgeTrace edges){
		_presence = presence;
		_edges = edges;
	}

	@Override
	public void convert() throws IOException {
		presence_writer = _presence.getWriter(); 
		StatefulReader<EdgeEvent,Edge> edge_reader = _edges.getReader();
		
		edge_reader.stateBus().addListener(edgeListener());
		edge_reader.bus().addListener(edgeEventListener());
		
		Runner runner = new Runner(_edges.maxUpdateInterval(), _edges.minTime(), _edges.maxTime());
		runner.addGenerator(edge_reader);
		runner.run();

		for ( Map.Entry<Integer, Long> e : exit_times.entrySet() ){
			PresenceEvent pev = new PresenceEvent(e.getKey(), PresenceEvent.Type.OUT);
			presence_writer.queue(e.getValue(), pev);
		}
		presence_writer.flush();
		
		presence_writer.setPropertiesFromTrace(_edges);
		presence_writer.close();
		edge_reader.close();
	}

	@Override
	public Listener<EdgeEvent> edgeEventListener() {
		return new Listener<EdgeEvent>(){
			@Override
			public void handle(long time, Collection<EdgeEvent> events) {
				for ( EdgeEvent event : events ){
					if ( event.isUp() ){
						if ( ! seen_nodes.contains(event.id1()) ){
							presence_writer.queue(time, new PresenceEvent(event.id1(), PresenceEvent.Type.IN));
							seen_nodes.add(event.id1());
						}
						if ( ! seen_nodes.contains(event.id2()) ){
							presence_writer.queue(time, new PresenceEvent(event.id2(), PresenceEvent.Type.IN));
							seen_nodes.add(event.id2());
						}
					} else {
						exit_times.put(event.id1(), time);
						exit_times.put(event.id2(), time);
					}
				}
			}
		};
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new Listener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> events) throws IOException{
				Set<Presence> init_nodes = new HashSet<Presence>();
				for ( Edge e : events){
					init_nodes.add(new Presence(e.id1()));
					init_nodes.add(new Presence(e.id2()));
					seen_nodes.add(e.id1());
					seen_nodes.add(e.id2());
				}
				presence_writer.setInitState(time, init_nodes);
			}
		};
	}
}
