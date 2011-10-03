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


import java.io.*;
import java.util.*;

import ditl.*;



public final class AnyContactTimesReport extends Report implements LinkTrace.Handler {

	private boolean _contacts;
	private Map<Integer,Integer> link_count = new HashMap<Integer,Integer>();
	private Map<Integer,Long> active_nodes = new HashMap<Integer,Long>();
	
	public AnyContactTimesReport(OutputStream out, boolean contacts) throws IOException {
		super(out);
		_contacts = contacts;
		appendComment("id1 | id2 | begin | end | duration");
	}
	
	public static final class Factory implements ReportFactory<AnyContactTimesReport> {
		private boolean _contacts;
		public Factory(boolean contacts){ _contacts = contacts;}
		@Override
		public AnyContactTimesReport getNew(OutputStream out) throws IOException {
			return new AnyContactTimesReport(out, _contacts);
		}
	}
	
	@Override
	public Listener<LinkEvent> linkEventListener(){
		return new Listener<LinkEvent>() {
			@Override
			public void handle(long time, Collection<LinkEvent> events) throws IOException {
				for ( LinkEvent event : events ){
					Link l = event.link();
					if( event.isUp() ){
						incr(time, l.id1, 1);
						incr(time, l.id2, 1);
					} else {
						incr(time, l.id1, -1);
						incr(time, l.id2, -1);
					}
				}
			}
		};
	}

	@Override
	public Listener<Link> linkListener() {
		return new StatefulListener<Link>(){
			@Override
			public void handle(long time, Collection<Link> events) throws IOException {
				if ( _contacts )
					for ( Link l : events ){
						incr(time, l.id1, 1);
						incr(time, l.id2, 1);
					}
			}

			@Override
			public void reset() {
				active_nodes.clear();
				link_count.clear();
			}
		};
	}
	
	private void incr(long time, Integer id, int diff) throws IOException {
		if ( ! link_count.containsKey(id) ){
			link_count.put(id, diff);
			if ( _contacts ) {
				active_nodes.put(id, time);
			} else {
				Long t = active_nodes.remove(id);
				if ( t != null )
					append(id+" "+t+" "+time+" "+(time-t));
			}
			
		} else {
			Integer c = link_count.get(id);
			c += diff;
			if ( c.equals(0) ){
				link_count.remove(id);
				if ( _contacts ){
					Long t = active_nodes.remove(id);
					append(id+" "+t+" "+time+" "+(time-t));
				} else {
					active_nodes.put(id, time);
				}
			} else {
				link_count.put(id, c);
			}
		}
	}
}
