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



public final class GroupSizeReport extends Report implements GroupTrace.Handler {

	private GroupTrace.Updater updater = new GroupTrace.Updater();
	
	public GroupSizeReport(OutputStream out) throws IOException {
		super(out);
		appendComment("time | group size distribution");
	}
	
	public static final class Factory implements ReportFactory<GroupSizeReport> {
		@Override
		public GroupSizeReport getNew(OutputStream out) throws IOException {
			return new GroupSizeReport(out);
		}
	}
	
	private void update(long time) throws IOException {
		StringBuffer buffer = new StringBuffer();
		buffer.append(time);
		for ( Group g : updater.states() )
			buffer.append(" "+g.size());
		append(buffer.toString());
	}

	@Override
	public Listener<GroupEvent> groupEventListener() {
		return new Listener<GroupEvent>(){
			@Override
			public void handle(long time, Collection<GroupEvent> events)
					throws IOException {
				for ( GroupEvent gev : events )
					updater.handleEvent(time, gev);
				update(time);
			}
		};
	}

	@Override
	public Listener<Group> groupListener() {
		return new StatefulListener<Group>(){
			@Override
			public void reset() {
				updater.setState(Collections.<Group>emptySet());
			}

			@Override
			public void handle(long time, Collection<Group> events)
					throws IOException {
				updater.setState(events);
				update(time);
			}
		};
	}
	
	
}
