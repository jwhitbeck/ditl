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
package ditl.plausible;

import java.io.IOException;
import java.util.*;

import ditl.*;
import ditl.graphs.*;

class LinkTimeline {
	
	long prev_up = -Trace.INFINITY;
	long prev_down = -Trace.INFINITY;
	long next_up = Trace.INFINITY;
	long next_down = Trace.INFINITY;
	
	private long prev_up_tmp, prev_down_tmp, next_up_tmp, next_down_tmp;
	
	private final Link _link; 
	
	private TreeMap<Long,List<LinkEvent>> buffer = new TreeMap<Long,List<LinkEvent>>();
	private StatefulWriter<WindowedLinkEvent,WindowedLink> window_writer;
	
	public LinkTimeline(Link link, StatefulWriter<WindowedLinkEvent,WindowedLink> windowWriter){
		_link = link;
		window_writer = windowWriter;
		//window_writer.append(time, new WindowedLinkEvent(_link, WindowedLinkEvent.UP));
	}
	
	private void write_events_if_changed(long time) throws IOException {
		if ( prev_up_tmp != prev_up ){
			prev_up = prev_up_tmp;
			window_writer.queue(time, new WindowedLinkEvent(_link, WindowedLinkEvent.PREV_UP, prev_up));
		}
		if ( prev_down_tmp != prev_down ){
			prev_down = prev_down_tmp;
			window_writer.queue(time, new WindowedLinkEvent(_link, WindowedLinkEvent.PREV_DOWN, prev_down));
		}
		if ( next_up_tmp != next_up ){
			next_up = next_up_tmp;
			window_writer.queue(time, new WindowedLinkEvent(_link, WindowedLinkEvent.NEXT_UP, next_up));
		}
		if ( next_down_tmp != next_down ){
			next_down = next_down_tmp;
			window_writer.queue(time, new WindowedLinkEvent(_link, WindowedLinkEvent.NEXT_DOWN, next_down));
		}
	}
	
	private void update_tmp_values(long time) {
		
		prev_up_tmp = -Trace.INFINITY;
		prev_down_tmp = -Trace.INFINITY;
		next_up_tmp = Trace.INFINITY;
		next_down_tmp = Trace.INFINITY;
		
		for ( Map.Entry<Long, List<LinkEvent>> e : buffer.entrySet() ){
			long t = e.getKey();
			for ( LinkEvent lev : e.getValue() )
			if ( lev.isUp() ){
				if ( t <= time && t > prev_up_tmp ){
					prev_up_tmp = t;
				}
				else if ( t > time && t < next_up_tmp ){
					next_up_tmp = t;
				}
			} else {
				if ( t <= time && t > prev_down_tmp )
					prev_down_tmp = t;
				else if ( t > time && t < next_down_tmp )
					next_down_tmp = t;
			}
		}
	}
	
	public void pop(long time, long window) throws IOException {
		buffer.pollFirstEntry();
		if ( prev_up == time-window || prev_down == time-window )
			update(time);
	}
	
	public void queue(long time, LinkEvent linkEvent){
		if ( ! buffer.containsKey(time) )
			buffer.put(time, new LinkedList<LinkEvent>());
		buffer.get(time).add(linkEvent);
	}
	
	public void append(long time, long window, LinkEvent linkEvent) throws IOException{
		queue(time, linkEvent);
		if ( linkEvent.isUp() ){
			if (next_up == Trace.INFINITY ){
				next_up = time;
				window_writer.queue(time-window, new WindowedLinkEvent(_link, WindowedLinkEvent.NEXT_UP, next_up));
			}
		} else {
			if ( next_down == Trace.INFINITY ){
				next_down = time;
				window_writer.queue(time-window, new WindowedLinkEvent(_link, WindowedLinkEvent.NEXT_DOWN, next_down));
			}
		}
	}
	
	public void update(long time) throws IOException {
		update_tmp_values(time);
		write_events_if_changed(time);
	}
	
	public void expire(long time) throws IOException{
		window_writer.queue(time, new WindowedLinkEvent(_link,WindowedLinkEvent.DOWN));
	}
	
	public WindowedLink windowedLink(){
		WindowedLink wl = new WindowedLink(_link);
		wl.prev_up = prev_up;
		wl.prev_down = prev_down;
		wl.next_up = next_up;
		wl.next_down = next_down;
		return wl;
	}
	
}
