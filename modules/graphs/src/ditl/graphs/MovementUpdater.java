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



public final class MovementUpdater implements StateUpdater<MovementEvent,Movement> {

	private Map<Integer,Movement> movement_map = new HashMap<Integer,Movement>();
	private Set<Movement> movements = new HashSet<Movement>();
	
	@Override
	public void setState(Collection<Movement> states) {
		movements.clear();
		movement_map.clear();
		for ( Movement m : states ){
			Movement mv = m.clone();
			movements.add(mv);
			movement_map.put(mv.id(), mv);
		}
	}

	@Override
	public Set<Movement> states() {
		return movements;
	}

	@Override
	public void handleEvent(long time, MovementEvent event) {
		Movement m;
		switch(event.type){
		case MovementEvent.IN: 
			m = event.origMovement();
			movements.add(m);
			movement_map.put(m.id(), m);
			break;
			
		case MovementEvent.OUT:
			m = movement_map.get(event.id());
			movements.remove(m);
			movement_map.remove(m.id());
			break;
			
		default:
			m = movement_map.get(event.id());
			m.handleEvent(time, event);
		}
	}
}
