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

@Trace.Type("presence")
public class PresenceTrace extends StatefulTrace<PresenceEvent, Presence> 
	implements StatefulTrace.Filterable<PresenceEvent, Presence>{
	
	public final static int defaultPriority = 10;
	
	public final static class Updater implements StateUpdater<PresenceEvent,Presence> {
		private Set<Presence> currentIds = new HashSet<Presence>();
		
		@Override
		public void setState(Collection<Presence> presenceState ) {
			currentIds.clear();
			for ( Presence p : presenceState )
				currentIds.add(p);
		}

		@Override
		public Set<Presence> states() {
			return currentIds;
		}

		@Override
		public void handleEvent(long time, PresenceEvent event) {
			if ( event.isIn() ){
				currentIds.add( event.presence() );
			} else {
				currentIds.remove( event.presence() );
			}
		}
	}
	
	public interface Handler {
		public Listener<Presence> presenceListener();
		public Listener<PresenceEvent> presenceEventListener();
	}
	
	public PresenceTrace(Store store, String name, PersistentMap info) throws IOException {
		super(store, name, info, new PresenceEvent.Factory(), new Presence.Factory(), 
				new StateUpdaterFactory<PresenceEvent,Presence>(){
					@Override
					public StateUpdater<PresenceEvent, Presence> getNew() {
						return new PresenceTrace.Updater();
					}
		});
		info.setIfUnset(Trace.defaultPriorityKey, defaultPriority);
	}

	@Override
	public Filter<Presence> stateFilter(Set<Integer> group) {
		return new Presence.GroupFilter(group);
	}

	@Override
	public Filter<PresenceEvent> eventFilter(Set<Integer> group) {
		return new PresenceEvent.GroupFilter(group);
	}

	@Override
	public void copyOverTraceInfo(Writer<PresenceEvent> writer) {}
}
