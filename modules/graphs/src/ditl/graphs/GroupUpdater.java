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

public final class GroupUpdater implements StateUpdater<GroupEvent, Group> {

	private Map<Integer,Group> group_map = new HashMap<Integer,Group>();
	private Set<Group> groups = new HashSet<Group>();
	
	@Override
	public void setState(Collection<Group> groupState ) {
		groups.clear();
		group_map.clear();
		for ( Group g : groupState ){
			groups.add(g);
			group_map.put(g._gid, g);
		}
	}

	@Override
	public Set<Group> states() {
		return groups;
	}

	@Override
	public void handleEvent(long time, GroupEvent event) {
		Group g;
		Integer gid = event._gid;
		switch ( event._type ){
		case GroupEvent.NEW: 
			g = new Group(gid);
			groups.add(g);
			group_map.put(gid, g);
			break;
		case GroupEvent.JOIN:
			g = group_map.get(gid);
			g.handleEvent(event);
			break;
		case GroupEvent.LEAVE:
			g = group_map.get(gid);
			g.handleEvent(event);
			break;
		case GroupEvent.DELETE:
			g = group_map.get(gid);
			groups.remove(g);
			group_map.remove(gid);
		}
	}
}
