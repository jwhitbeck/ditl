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

public class StaticGroupEdgeConverter implements Converter {

	private Map<Integer, Integer> group_map;
	private Map<Edge,Set<Edge>> group_edges = new AdjacencyMap.Edges<Set<Edge>>();
	private StatefulWriter<EdgeEvent,Edge> group_edge_writer;
	private StatefulReader<EdgeEvent,Edge> edge_reader;
	private EdgeTrace g_edges;
	private EdgeTrace _edges;
	
	public StaticGroupEdgeConverter(EdgeTrace groupEdges, EdgeTrace edges, Set<Group> groups){
		g_edges = groupEdges;
		_edges = edges;
		group_map = new HashMap<Integer,Integer>();
		for ( Group g : groups ){
			Integer gid = g.gid();
			for ( Integer id : g.members() )
				group_map.put(id, gid);
		}
	}
	
	private Edge groupEdges(Edge e){
		Integer gid1 = group_map.get(e.id1());
		Integer gid2 = group_map.get(e.id2());
		if ( gid2.equals(gid1) )
			return null;
		return new Edge( gid1, gid2);
	}
	
	private void setInitState(long minTime, Collection<Edge> edges) throws IOException {
		for ( Edge e : edges ){
			Edge gl = groupEdges(e);
			if ( gl != null ){
				if ( ! group_edges.containsKey(gl) )
					group_edges.put(gl, new AdjacencySet.Edges() );
				group_edges.get(gl).add(e);
			}
		}
		group_edge_writer.setInitState(minTime, group_edges.keySet());
	}
	
	private void handleEvents(long time, Collection<EdgeEvent> events) throws IOException {
		for ( EdgeEvent eev : events ){
			Edge e = eev.edge();
			Edge gl = groupEdges(e);
			if ( gl != null ){
				Set<Edge> g_edge = group_edges.get(gl);
				if ( eev.isUp() ){
					if ( g_edge == null ){
						g_edge = new AdjacencySet.Edges();
						group_edges.put(gl, g_edge);
						group_edge_writer.append(time, new EdgeEvent(gl, EdgeEvent.UP));
					}
					group_edges.get(gl).add(e);
				} else {
					g_edge.remove(e);
					if ( g_edge.isEmpty() ){
						group_edge_writer.append(time, new EdgeEvent(gl, EdgeEvent.DOWN));
						group_edges.remove(gl);
					}
				}
			}
		}
	}
	
	@Override
	public void convert() throws IOException {
		group_edge_writer = g_edges.getWriter();
		edge_reader = _edges.getReader();
		long minTime = _edges.minTime();
		edge_reader.seek(minTime);
		Collection<Edge> initEdges = edge_reader.referenceState();
		setInitState(minTime, initEdges);
		while ( edge_reader.hasNext() ){
			long time = edge_reader.nextTime();
			handleEvents(time, edge_reader.next());
		}
		group_edge_writer.setPropertiesFromTrace(_edges);
	}
	
	
}
