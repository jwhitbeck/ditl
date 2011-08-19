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



public final class LinksToPresenceConverter implements Converter, LinkHandler{
	
	private StatefulWriter<PresenceEvent,Presence> presence_writer;
	private StatefulReader<LinkEvent,Link> links_reader;
	
	private Set<Presence> ids = new HashSet<Presence>();
	
	public LinksToPresenceConverter(StatefulWriter<PresenceEvent,Presence> presenceWriter,
			StatefulReader<LinkEvent,Link> linksReader){
		presence_writer = presenceWriter;
		links_reader = linksReader;
	}

	private void addLink(Link l){
		ids.add(new Presence(l.id1()));
		ids.add(new Presence(l.id2()));
	}
	
	@Override
	public void close() throws IOException {
		presence_writer.close();
	}

	@Override
	public void run() throws IOException {
		Bus<Link> linkBus = new Bus<Link>();
		Bus<LinkEvent> linkEventBus = new Bus<LinkEvent>();
		linkBus.addListener(linkListener());
		linkEventBus.addListener(linkEventListener());
		links_reader.setBus(linkEventBus);
		links_reader.setStateBus(linkBus);
		Trace links = links_reader.trace();
		Runner runner = new Runner(links.maxUpdateInterval(), links.minTime(), links.maxTime());
		runner.addGenerator(links_reader);
		runner.run();
		presence_writer.setInitState(links.minTime(), ids);
		presence_writer.setProperty(Trace.maxTimeKey, links.maxTime());
		presence_writer.setProperty(Trace.ticsPerSecondKey, links.ticsPerSecond());
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
