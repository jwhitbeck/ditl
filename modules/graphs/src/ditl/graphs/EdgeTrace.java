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

public class EdgeTrace extends StatefulTrace<EdgeEvent, Edge> 
	implements StatefulTrace.Filterable<EdgeEvent, Edge>{
	
	public final static String type = "edges";
	public final static String defaultName = "edges";
	
	public final static class Updater implements StateUpdater<EdgeEvent, Edge> {

		private Set<Edge> edges = new TreeSet<Edge>(); 
		
		@Override
		public void setState(Collection<Edge> edgesState ) {
			edges.clear();
			for ( Edge e : edgesState )
				edges.add(e);
		}

		@Override
		public Set<Edge> states() {
			return edges;
		}

		@Override
		public void handleEvent(long time, EdgeEvent event) {
			if ( event.isUp() )
				edges.add(event.edge());
			else
				edges.remove(event.edge());
		}
	}
	
	public interface Handler {
		public Listener<Edge> edgeListener();
		public Listener<EdgeEvent> edgeEventListener();
	}

	public EdgeTrace(Store store, String name, PersistentMap info) throws IOException {
		super(store, name, info, new EdgeEvent.Factory(), new Edge.Factory(), 
				new StateUpdaterFactory<EdgeEvent,Edge>(){
					@Override
					public StateUpdater<EdgeEvent, Edge> getNew() {
						return new EdgeTrace.Updater();
					}
		});
	}

	@Override
	public Matcher<Edge> stateMatcher(Set<Integer> group) {
		return new Edge.InternalGroupMatcher(group);
	}

	@Override
	public Matcher<EdgeEvent> eventMatcher(Set<Integer> group) {
		return new EdgeEvent.InternalGroupMatcher(group);
	}
}
