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



public final class LinkUpdater implements StateUpdater<LinkEvent, Link> {

	private Set<Link> links = new HashSet<Link>();
	
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
