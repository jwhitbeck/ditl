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



public final class EdgesToPresenceConverter implements Converter, EdgeTrace.Handler{
	
	private PresenceTrace _presence;
	private EdgeTrace _edges;
	
	private Set<Presence> ids = new HashSet<Presence>();
	
	public EdgesToPresenceConverter(PresenceTrace presence, EdgeTrace edges){
		_presence = presence;
		_edges = edges;
	}

	private void addEdge(Edge e){
		ids.add(new Presence(e.id1()));
		ids.add(new Presence(e.id2()));
	}

	@Override
	public void convert() throws IOException {
		StatefulWriter<PresenceEvent,Presence> presence_writer = _presence.getWriter(); 
		StatefulReader<EdgeEvent,Edge> edge_reader = _edges.getReader();
		
		edge_reader.stateBus().addListener(edgeListener());
		edge_reader.bus().addListener(edgeEventListener());

		Runner runner = new Runner(_edges.maxUpdateInterval(), _edges.minTime(), _edges.maxTime());
		runner.addGenerator(edge_reader);
		runner.run();
		
		presence_writer.setInitState(_edges.minTime(), ids);
		presence_writer.setPropertiesFromTrace(_edges);
		presence_writer.close();
		edge_reader.close();
	}

	@Override
	public Listener<EdgeEvent> edgeEventListener() {
		return new Listener<EdgeEvent>(){
			@Override
			public void handle(long time, Collection<EdgeEvent> events) {
				for ( EdgeEvent event : events )
					addEdge(event.edge());
			}
		};
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new Listener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> events){
				for ( Edge e : events)
					addEdge(e);
			}
		};
	}
}
