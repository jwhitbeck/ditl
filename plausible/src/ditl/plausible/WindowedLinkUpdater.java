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
package ditl.plausible;

import java.util.*;

import ditl.StateUpdater;
import ditl.graphs.Link;

public class WindowedLinkUpdater implements StateUpdater<WindowedLinkEvent,WindowedLink>{

	private Set<WindowedLink> state = new HashSet<WindowedLink>();
	private Map<Link,WindowedLink> map = new HashMap<Link,WindowedLink>();
	
	@Override
	public void handleEvent(long time, WindowedLinkEvent event) {
		Link link = event.link();
		WindowedLink wl;
		switch ( event.type() ){
		case WindowedLinkEvent.UP:
			wl = new WindowedLink(link);
			state.add(wl);
			map.put(link,wl);
			break;
		case WindowedLinkEvent.DOWN:
			wl = map.get(link);
			state.remove(wl);
			map.remove(link);
			break;
		default:
			wl = map.get(link);
			wl.handleEvent(event);
		}
	}

	@Override
	public void setState(Collection<WindowedLink> states) {
		state.clear();
		map.clear();
		state.addAll(states);
		for ( WindowedLink wl : states ){
			map.put(wl.link(), wl);
		}
	}

	@Override
	public Set<WindowedLink> states() {
		return state;
	}

}
