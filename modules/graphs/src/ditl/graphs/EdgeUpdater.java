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



public final class EdgeUpdater implements StateUpdater<EdgeEvent, Edge> {

	private Set<Edge> edges = new HashSet<Edge>(); 
	
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
