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



public final class LinksToPresenceConverter implements Converter, LinkTrace.Handler{
	
	private PresenceTrace _presence;
	private LinkTrace _links;
	
	private Set<Presence> ids = new HashSet<Presence>();
	
	public LinksToPresenceConverter(PresenceTrace presence, LinkTrace links){
		_presence = presence;
		_links = links;
	}

	private void addLink(Link l){
		ids.add(new Presence(l.id1()));
		ids.add(new Presence(l.id2()));
	}

	@Override
	public void convert() throws IOException {
		StatefulWriter<PresenceEvent,Presence> presence_writer = _presence.getWriter(_links.snapshotInterval()); 
		StatefulReader<LinkEvent,Link> links_reader = _links.getReader();
		
		links_reader.stateBus().addListener(linkListener());
		links_reader.bus().addListener(linkEventListener());

		Runner runner = new Runner(_links.maxUpdateInterval(), _links.minTime(), _links.maxTime());
		runner.addGenerator(links_reader);
		runner.run();
		
		presence_writer.setInitState(_links.minTime(), ids);
		presence_writer.setPropertiesFromTrace(_links);
		presence_writer.close();
		links_reader.close();
	}

	@Override
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>(){
			@Override
			public void handle(long time, Collection<LinkEvent> events) {
				for ( LinkEvent event : events )
					addLink(event.link());
			}
		};
	}

	@Override
	public Listener<Link> linkListener() {
		return new Listener<Link>(){
			@Override
			public void handle(long time, Collection<Link> events){
				for ( Link l : events)
					addLink(l);
			}
		};
	}
}
