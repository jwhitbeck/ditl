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

public final class TransitTimesReport extends Report implements PresenceTrace.Handler {

	private Map<Integer,Long> entry_times = new HashMap<Integer,Long>();
	
	public TransitTimesReport(OutputStream out) throws IOException {
		super(out);
	}
	
	public static final class Factory implements ReportFactory<TransitTimesReport> {
		@Override
		public TransitTimesReport getNew(OutputStream out) throws IOException {
			return new TransitTimesReport(out); 
		}
	}
	
	@Override
	public Listener<PresenceEvent> presenceEventListener() {
		return new Listener<PresenceEvent>(){
			@Override
			public void handle(long time, Collection<PresenceEvent> events) throws IOException {
				for ( PresenceEvent pev : events ){
					if ( pev.isIn() ) {
						entry_times.put(pev.id(), time);
					} else {
						long transit_time = time - entry_times.get(pev.id());
						append(transit_time);
						entry_times.remove(pev.id());
					}
				}
			}
		};
	}

	@Override
	public Listener<Presence> presenceListener() {
		return new StatefulListener<Presence>(){
			@Override
			public void handle(long time, Collection<Presence> events) {
				for ( Presence p : events )
					entry_times.put(p.id(), time);
			}

			@Override
			public void reset() {
				entry_times.clear();
			}
		};
	}
}
