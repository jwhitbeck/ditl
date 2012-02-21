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

public class LinkTrace extends StatefulTrace<LinkEvent, Link> 
	implements StatefulTrace.Filterable<LinkEvent, Link>{
	
	public final static String type = "links";
	public final static String defaultName = "links";
	
	public final static class Updater implements StateUpdater<LinkEvent,Link> {
		private Set<Link> links = new AdjacencySet.Links();
		
		@Override
		public void setState(Collection<Link> contactsState ) {
			links.clear();
			for ( Link l : contactsState )
				links.add(l);
		}

		@Override
		public Set<Link> states() {
			return links;
		}

		@Override
		public void handleEvent(long time, LinkEvent event) {
			if ( event.isUp() ){
				links.add( event.link() );
			} else {
				links.remove( event.link() );
			}
		}
	}
	
	public interface Handler {
		public Listener<Link> linkListener();
		public Listener<LinkEvent> linkEventListener();
	}

	public LinkTrace(Store store, String name, PersistentMap info) throws IOException {
		super(store, name, info, new LinkEvent.Factory(), new Link.Factory(), 
				new StateUpdaterFactory<LinkEvent,Link>(){
					@Override
					public StateUpdater<LinkEvent, Link> getNew() {
						return new LinkTrace.Updater();
					}
		});
	}

	@Override
	public Filter<Link> stateFilter(Set<Integer> group) {
		return new Link.InternalGroupFilter(group);
	}

	@Override
	public Filter<LinkEvent> eventFilter(Set<Integer> group) {
		return new LinkEvent.InternalGroupFilter(group);
	}

	@Override
	public void fillFilteredTraceInfo(Writer<LinkEvent> writer) {}
}
