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



public final class StrictLinksToPresenceConverter implements Converter, LinkTrace.Handler{
	
	private PresenceTrace _presence;
	private LinkTrace _links;
	
	private Map<Integer, Long> exit_times = new HashMap<Integer, Long>();
	private Set<Integer> seen_nodes = new HashSet<Integer>();
	private StatefulWriter<PresenceEvent,Presence> presence_writer;
	
	public StrictLinksToPresenceConverter(PresenceTrace presence, LinkTrace links){
		_presence = presence;
		_links = links;
	}

	@Override
	public void convert() throws IOException {
		presence_writer = _presence.getWriter(_links.snapshotInterval()); 
		StatefulReader<LinkEvent,Link> links_reader = _links.getReader();
		
		links_reader.stateBus().addListener(linkListener());
		links_reader.bus().addListener(linkEventListener());
		
		Runner runner = new Runner(_links.maxUpdateInterval(), _links.minTime(), _links.maxTime());
		runner.addGenerator(links_reader);
		runner.run();

		for ( Map.Entry<Integer, Long> e : exit_times.entrySet() ){
			PresenceEvent pev = new PresenceEvent(e.getKey(), PresenceEvent.OUT);
			presence_writer.queue(e.getValue(), pev);
		}
		presence_writer.flush();
		
		presence_writer.setProperty(Trace.maxTimeKey, _links.maxTime());
		presence_writer.setProperty(Trace.ticsPerSecondKey, _links.ticsPerSecond());
		presence_writer.close();
		links_reader.close();
	}

	@Override
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>(){
			@Override
			public void handle(long time, Collection<LinkEvent> events) {
				for ( LinkEvent event : events ){
					if ( event.isUp() ){
						if ( ! seen_nodes.contains(event.id1()) ){
							presence_writer.queue(time, new PresenceEvent(event.id1(), PresenceEvent.IN));
							seen_nodes.add(event.id1());
						}
						if ( ! seen_nodes.contains(event.id2()) ){
							presence_writer.queue(time, new PresenceEvent(event.id2(), PresenceEvent.IN));
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
	public Listener<Link> linkListener() {
		return new Listener<Link>(){
			@Override
			public void handle(long time, Collection<Link> events) throws IOException{
				Set<Presence> init_nodes = new HashSet<Presence>();
				for ( Link l : events){
					init_nodes.add(new Presence(l.id1()));
					init_nodes.add(new Presence(l.id2()));
					seen_nodes.add(l.id1());
					seen_nodes.add(l.id2());
				}
				presence_writer.setInitState(time, init_nodes);
			}
		};
	}
}
