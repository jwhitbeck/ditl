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

import java.util.*;

import ditl.*;



public final class PresenceUpdater implements StateUpdater<PresenceEvent, Presence> {

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
