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


import java.io.*;
import java.util.*;

import ditl.*;



public final class ContactTimesReport extends Report implements EdgeTrace.Handler {

	private boolean _contacts;
	private Map<Edge,Long> activeContacts = new AdjacencyMap.Edges<Long>();
	
	public ContactTimesReport(OutputStream out, boolean contacts) throws IOException {
		super(out);
		_contacts = contacts;
		appendComment("id1 | id2 | begin | end | duration");
	}
	
	public static final class Factory implements ReportFactory<ContactTimesReport> {
		private boolean _contacts;
		public Factory(boolean contacts){ _contacts = contacts;}
		@Override
		public ContactTimesReport getNew(OutputStream out) throws IOException {
			return new ContactTimesReport(out, _contacts);
		}
	}
	
	@Override
	public Listener<EdgeEvent> edgeEventListener(){
		return new Listener<EdgeEvent>() {
			@Override
			public void handle(long time, Collection<EdgeEvent> events) throws IOException {
				for ( EdgeEvent event : events ){
					Edge e = event.edge();
					if( event.isUp() == _contacts ){
						activeContacts.put(e, time);
					} else {
						Long b = activeContacts.get(e);
						if ( b != null ){
							activeContacts.remove(e);
							append(e+" "+b+" "+time+" "+(time-b));
						}
					}
				}
			}
		};
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new StatefulListener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> events) {
				if ( _contacts )
					for ( Edge e : events )
						activeContacts.put(e, time);
			}

			@Override
			public void reset() {
				activeContacts.clear();
			}
		};
	}
}
