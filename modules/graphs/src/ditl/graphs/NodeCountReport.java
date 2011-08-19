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

public final class NodeCountReport extends Report implements PresenceHandler {

	private long count;
	
	public NodeCountReport(OutputStream out) throws IOException {
		super(out);
		appendComment("time | node count");
	}
	
	public static ReportFactory<NodeCountReport> factory(){
		return new ReportFactory<NodeCountReport>(){
			@Override
			public NodeCountReport getNew(OutputStream out) throws IOException {
				return new NodeCountReport(out);
			}
		};
	}
	
	@Override
	public Listener<Presence> presenceListener(){
		return new StatefulListener<Presence>(){
			@Override
			public void handle(long time, Collection<Presence> events) throws IOException {
				count += events.size();
				append(time+" "+count);
			}

			@Override
			public void reset() {
				count = 0;
			}
		};
	}
	
	@Override
	public Listener<PresenceEvent> presenceEventListener(){
		return new Listener<PresenceEvent>() {
			@Override
			public void handle(long time, Collection<PresenceEvent> events) throws IOException {
				for ( PresenceEvent pev : events )
					count += (pev.isIn())? 1 : -1;
				append(time+" "+count);
			}
		};
	}
}
