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



public final class EdgesToLinksConverter implements Converter {

	public final static boolean UNION = true;
	public final static boolean INTERSECTION = false;
	
	private boolean _union; 
	private Set<Edge> edges = new TreeSet<Edge>();
	private StatefulWriter<LinkEvent,Link> link_writer;
	private StatefulReader<EdgeEvent,Edge> edge_reader;
	private LinkTrace _links;
	private EdgeTrace _edges;
	
	public EdgesToLinksConverter(LinkTrace links, EdgeTrace edges, boolean union){
		_edges = edges;
		_links = links;
		_union = union;
	}
	

	private void setInitStateFromEdges(long time, Set<Edge> states) throws IOException {
		Set<Link> contacts = new TreeSet<Link>();
		for ( Edge edge : states ){
			edges.add(edge);
			if ( _union ){
				contacts.add(edge.link());
			} else if ( edges.contains(edge.reverse())) {
				contacts.add(edge.link());
			}
		}
		link_writer.setInitState(time, contacts);
	}
	
	private void handleEdgeEvent(long time, EdgeEvent event ) throws IOException {
		Edge e = event.edge();
		Link l = e.link();
		if ( _union && ! edges.contains(e.reverse()) ) {
			if ( event.isUp() ){
				link_writer.append(time, new LinkEvent(l,LinkEvent.UP) );
			} else {
				link_writer.append(time, new LinkEvent(l,LinkEvent.DOWN) );
			}
		} else if ( ! _union && edges.contains( e.reverse() )){ // intersect and reverse if present
			if ( event.isUp() ){
				link_writer.append(time, new LinkEvent(l,LinkEvent.UP) );
			} else {
				link_writer.append(time, new LinkEvent(l,LinkEvent.DOWN) );
			}
		}
		if ( event.isUp() )
			edges.add(e);
		else
			edges.remove(e);
	}
	
	@Override
	public void convert() throws IOException{
		edge_reader = _edges.getReader();
		link_writer = _links.getWriter(_edges.snapshotInterval());
		long minTime = _edges.minTime();
		edge_reader.seek(minTime);
		setInitStateFromEdges(minTime,edge_reader.referenceState());
		while ( edge_reader.hasNext() )
			for ( EdgeEvent event : edge_reader.next() )
				handleEdgeEvent(edge_reader.time(), event);
		link_writer.setProperty(Trace.ticsPerSecondKey, _edges.ticsPerSecond());
		edge_reader.close();
		link_writer.close();
	}
}
