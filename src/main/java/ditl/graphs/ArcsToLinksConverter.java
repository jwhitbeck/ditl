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



public final class ArcsToLinksConverter implements Converter {

	public final static boolean UNION = true;
	public final static boolean INTERSECTION = false;
	
	private boolean _union; 
	private Set<Arc> arcs = new AdjacencySet.Arcs();
	private StatefulWriter<LinkEvent,Link> link_writer;
	private StatefulReader<ArcEvent,Arc> arc_reader;
	private LinkTrace _links;
	private ArcTrace _arcs;
	
	public ArcsToLinksConverter(LinkTrace links, ArcTrace arcs, boolean union){
		_arcs = arcs;
		_links = links;
		_union = union;
	}
	

	private void setInitStateFromArcs(long time, Set<Arc> states) throws IOException {
		Set<Link> contacts = new AdjacencySet.Links();
		for ( Arc arc : states ){
			arcs.add(arc);
			if ( _union ){
				contacts.add(arc.link());
			} else if ( arcs.contains(arc.reverse())) {
				contacts.add(arc.link());
			}
		}
		link_writer.setInitState(time, contacts);
	}
	
	private void handleArcEvent(long time, ArcEvent event ) throws IOException {
		Arc a = event.arc();
		Link l = a.link();
		if ( _union && ! arcs.contains(a.reverse()) ) {
			if ( event.isUp() ){
				link_writer.append(time, new LinkEvent(l,LinkEvent.UP) );
			} else {
				link_writer.append(time, new LinkEvent(l,LinkEvent.DOWN) );
			}
		} else if ( ! _union && arcs.contains( a.reverse() )){ // intersect and reverse if present
			if ( event.isUp() ){
				link_writer.append(time, new LinkEvent(l,LinkEvent.UP) );
			} else {
				link_writer.append(time, new LinkEvent(l,LinkEvent.DOWN) );
			}
		}
		if ( event.isUp() )
			arcs.add(a);
		else
			arcs.remove(a);
	}
	
	@Override
	public void convert() throws IOException{
		arc_reader = _arcs.getReader();
		link_writer = _links.getWriter();
		long minTime = _arcs.minTime();
		arc_reader.seek(minTime);
		setInitStateFromArcs(minTime,arc_reader.referenceState());
		while ( arc_reader.hasNext() )
			for ( ArcEvent event : arc_reader.next() )
				handleArcEvent(arc_reader.time(), event);
		link_writer.setPropertiesFromTrace(_arcs);
		arc_reader.close();
		link_writer.close();
	}
}
