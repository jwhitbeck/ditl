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



public final class BufferLinksConverter implements Converter, 
	LinkTrace.Handler, Listener<LinkEvent> {
	
	private LinkTrace buffered_links;
	private LinkTrace _links;
	private long before_b_time;
	private long after_b_time;
	private boolean _randomize;
	private Random rng = new Random();
	private StatefulWriter<LinkEvent,Link> buffer_writer;
	private Set<Link> init_state = new AdjacencySet.Links();
	private Map<Link,Integer> up_count = new AdjacencyMap.Links<Integer>();
	private Bus<LinkEvent> event_bus = new Bus<LinkEvent>();
	private boolean init_state_set = false;
	private long min_time;
	
	public BufferLinksConverter(LinkTrace bufferedLinks, LinkTrace links, 
			long beforeBufferTime, long afterBufferTime, boolean randomize){
		buffered_links = bufferedLinks;
		_links = links;
		before_b_time = beforeBufferTime;
		after_b_time = afterBufferTime;
		_randomize = randomize;
		min_time = _links.minTime();
	}


	@Override
	public void convert() throws IOException {
		buffer_writer = buffered_links.getWriter(); 
		StatefulReader<LinkEvent,Link> links_reader = _links.getReader();
		
		links_reader.stateBus().addListener(linkListener());
		links_reader.bus().addListener(linkEventListener());
		event_bus.addListener(this);
		
		Runner runner = new Runner(_links.maxUpdateInterval(), _links.minTime(), _links.maxTime());
		runner.addGenerator(links_reader);
		runner.run();
		
		event_bus.flush();
		
		buffer_writer.setPropertiesFromTrace(_links);
		buffer_writer.close();
		links_reader.close();
	}

	@Override
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>(){
			@Override
			public void handle(long time, Collection<LinkEvent> events) throws IOException {
				for ( LinkEvent event : events ){
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
	public Listener<Link> linkListener() {
		return new Listener<Link>(){
			@Override
			public void handle(long time, Collection<Link> events) {
				for ( Link l : events ){
					init_state.add(l);
					incrLinkCount(l);
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
	
	private int incrLinkCount(Link l){
		Integer i = up_count.get(l);
		if ( i==null ){
			up_count.put(l, 1);
			return 1;
		}
		up_count.put(l, i+1);
		return i+1;
	}
	
	private int decrLinkCount(Link l){
		Integer i = up_count.remove(l);
		if ( i > 1 ){
			up_count.put(l, i-1);
			return i-1;
		}
		return 0;
	}

	@Override
	public void handle(long time, Collection<LinkEvent> events) throws IOException {
		Deque<LinkEvent> down_links = new LinkedList<LinkEvent>();
		for ( LinkEvent lev : events ){
			Link l = lev.link();
			if ( time < min_time ){
				init_state.add(l);
				incrLinkCount(l);
			} else {
				if ( ! init_state_set ){
					buffer_writer.setInitState(min_time, init_state);
					init_state_set = true;
				}
				if ( lev.isUp() ){
					if ( incrLinkCount(l) == 1 ){ // link just came up
						buffer_writer.append(time, lev);
					}
				} else {
					down_links.addLast(lev);
				}
			}
		}
		if ( time >= min_time ){
			while ( ! down_links.isEmpty() ){
				LinkEvent dlev = down_links.poll();
				if ( decrLinkCount(dlev.link()) == 0 )
					buffer_writer.append(time, dlev);
			}
		}
	}
}
