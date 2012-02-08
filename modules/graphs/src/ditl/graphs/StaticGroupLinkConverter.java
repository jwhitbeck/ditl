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

public class StaticGroupLinkConverter implements Converter {

	private Map<Integer, Integer> group_map;
	private Map<Link,Set<Link>> group_links = new TreeMap<Link,Set<Link>>();
	private StatefulWriter<LinkEvent,Link> group_link_writer;
	private StatefulReader<LinkEvent,Link> link_reader;
	private LinkTrace g_links;
	private LinkTrace _links;
	
	public StaticGroupLinkConverter(LinkTrace groupLinks, LinkTrace links, Set<Group> groups){
		g_links = groupLinks;
		_links = links;
		group_map = new HashMap<Integer,Integer>();
		for ( Group g : groups ){
			Integer gid = g.gid();
			for ( Integer id : g.members() )
				group_map.put(id, gid);
		}
	}
	
	private Link groupLink(Link l){
		Integer gid1 = group_map.get(l.id1());
		Integer gid2 = group_map.get(l.id2());
		if ( gid2.equals(gid1) )
			return null;
		return new Link( gid1, gid2);
	}
	
	private void setInitState(long minTime, Collection<Link> links) throws IOException {
		for ( Link l : links ){
			Link gl = groupLink(l);
			if ( gl != null ){
				if ( ! group_links.containsKey(gl) )
					group_links.put(gl, new TreeSet<Link>() );
				group_links.get(gl).add(l);
			}
		}
		group_link_writer.setInitState(minTime, group_links.keySet());
	}
	
	private void handleEvents(long time, Collection<LinkEvent> events) throws IOException {
		for ( LinkEvent lev : events ){
			Link l = lev.link();
			Link gl = groupLink(l);
			if ( gl != null ){
				Set<Link> g_link = group_links.get(gl);
				if ( lev.isUp() ){
					if ( g_link == null ){
						g_link = new TreeSet<Link>();
						group_links.put(gl, g_link);
						group_link_writer.append(time, new LinkEvent(gl, LinkEvent.UP));
					}
					group_links.get(gl).add(l);
				} else {
					g_link.remove(l);
					if ( g_link.isEmpty() ){
						group_link_writer.append(time, new LinkEvent(gl, LinkEvent.DOWN));
						group_links.remove(gl);
					}
				}
			}
		}
	}
	
	@Override
	public void convert() throws IOException {
		group_link_writer = g_links.getWriter(_links.snapshotInterval());
		link_reader = _links.getReader();
		long minTime = _links.minTime();
		link_reader.seek(minTime);
		Collection<Link> initLinks = link_reader.referenceState();
		setInitState(minTime, initLinks);
		while ( link_reader.hasNext() ){
			long time = link_reader.nextTime();
			handleEvents(time, link_reader.next());
		}
		group_link_writer.setProperty(Trace.maxTimeKey, _links.maxTime());
		group_link_writer.setProperty(Trace.ticsPerSecondKey, _links.ticsPerSecond());
	}
	
	
}
