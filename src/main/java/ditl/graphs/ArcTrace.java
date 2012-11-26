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

public class ArcTrace extends StatefulTrace<ArcEvent, Arc> 
	implements StatefulTrace.Filterable<ArcEvent, Arc>{
	
	public final static String type = "arcs";
	public final static String defaultName = "arcs";
	
	public final static class Updater implements StateUpdater<ArcEvent, Arc> {

		private Set<Arc> arcs = new AdjacencySet.Arcs(); 
		
		@Override
		public void setState(Collection<Arc> arcsState ) {
			arcs.clear();
			for ( Arc a : arcsState )
				arcs.add(a);
		}

		@Override
		public Set<Arc> states() {
			return arcs;
		}

		@Override
		public void handleEvent(long time, ArcEvent event) {
			if ( event.isUp() )
				arcs.add(event.arc());
			else
				arcs.remove(event.arc());
		}
	}
	
	public interface Handler {
		public Listener<Arc> arcListener();
		public Listener<ArcEvent> arcEventListener();
	}

	public ArcTrace(Store store, String name, PersistentMap info) throws IOException {
		super(store, name, info, new ArcEvent.Factory(), new Arc.Factory(), 
				new StateUpdaterFactory<ArcEvent,Arc>(){
					@Override
					public StateUpdater<ArcEvent, Arc> getNew() {
						return new ArcTrace.Updater();
					}
		});
	}

	@Override
	public Filter<Arc> stateFilter(Set<Integer> group) {
		return new Arc.InternalGroupFilter(group);
	}

	@Override
	public Filter<ArcEvent> eventFilter(Set<Integer> group) {
		return new ArcEvent.InternalGroupFilter(group);
	}

	@Override
	public void copyOverTraceInfo(Writer<ArcEvent> writer) {}
}
