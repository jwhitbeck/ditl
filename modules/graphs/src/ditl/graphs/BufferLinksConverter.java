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



public final class BufferLinksConverter implements Converter, 
	LinkTrace.Handler, Incrementable {
	
	private LinkTrace buffered_links;
	private LinkTrace _links;
	private long b_time;
	private boolean _randomize;
	private Random rng = new Random();
	private long cur_time;
	private StatefulWriter<LinkEvent,Link> buffer_writer;
	private Set<Link> init_state = new HashSet<Link>();
	private boolean flushed = false;
	private long min_time;
	
	public BufferLinksConverter(LinkTrace bufferedLinks, LinkTrace links, 
			long bufferTime, boolean randomize){
		buffered_links = bufferedLinks;
		_links = links;
		b_time = bufferTime;
		_randomize = randomize;
		min_time = _links.minTime();
	}


	@Override
	public void convert() throws IOException {
		buffer_writer = buffered_links.getWriter(_links.snapshotInterval()); 
		StatefulReader<LinkEvent,Link> links_reader = _links.getReader();
		
		Bus<Link> linkBus = new Bus<Link>();
		Bus<LinkEvent> linkEventBus = new Bus<LinkEvent>();
		linkBus.addListener(linkListener());
		linkEventBus.addListener(linkEventListener());
		links_reader.setBus(linkEventBus);
		links_reader.setStateBus(linkBus);
		Runner runner = new Runner(_links.maxUpdateInterval(), _links.minTime(), _links.maxTime());
		runner.addGenerator(links_reader);
		runner.add(this);
		runner.run();
		
		buffer_writer.flush();
		buffer_writer.setProperty(Trace.ticsPerSecondKey, _links.ticsPerSecond());
		buffer_writer.close();
		links_reader.close();
	}

	@Override
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>(){
			@Override
			public void handle(long time, Collection<LinkEvent> events) {
				for ( LinkEvent event : events ){
					if ( event.isUp() ){
						long b = begin(time);
						if ( b <= min_time )
							init_state.add(event.link());
						else
							buffer_writer.queue(b, event);
					} else {
						buffer_writer.queue(end(time), event);
					}
				}
			}
		};
	}

	@Override
	public Listener<Link> linkListener() {
		return new Listener<Link>(){
			@Override
			public void handle(long time, Collection<Link> events) {
				init_state.addAll(events);
			}
		};
	}
	
	private long rand(){
		return Math.abs(rng.nextLong()) % b_time;
	}
	
	private long begin(long time){
		if ( _randomize )
			return time - rand();
		return time - b_time;
	}
	
	private long end(long time){
		if ( _randomize )
			return time + rand();
		return time + b_time;
	}

	@Override
	public void incr(long dt) throws IOException {
		if ( cur_time > min_time+b_time ){
			if ( ! flushed ){
				buffer_writer.setInitState(min_time, init_state);
				flushed = true;
			}
			buffer_writer.flush(cur_time-b_time);
		}
		cur_time += dt;
	}

	@Override
	public void seek(long time) throws IOException {
		cur_time = time;
	}
}
