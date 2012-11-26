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
package ditl.plausible;

import java.io.IOException;
import java.util.*;

import ditl.*;
import ditl.graphs.*;

public class WindowedEdgeTrace extends StatefulTrace<WindowedEdgeEvent,WindowedEdge>{
	
	public static final String type = "windowed edges";
	public static final String defaultName = "windowed_edges";
	
	public final static String windowLengthKey = "window length";

	public final static class Updater implements StateUpdater<WindowedEdgeEvent,WindowedEdge>{

		private Set<WindowedEdge> state = new WindowedEdgesSet();
		private Map<Edge,WindowedEdge> map = new AdjacencyMap.Edges<WindowedEdge>();
		
		@Override
		public void handleEvent(long time, WindowedEdgeEvent event) {
			Edge edge = event.edge();
			WindowedEdge wl;
			switch ( event.type() ){
			case WindowedEdgeEvent.UP:
				wl = new WindowedEdge(edge);
				state.add(wl);
				map.put(edge,wl);
				break;
			case WindowedEdgeEvent.DOWN:
				wl = map.get(edge);
				state.remove(wl);
				map.remove(edge);
				break;
			default:
				wl = map.get(edge);
				wl.handleEvent(event);
			}
		}

		@Override
		public void setState(Collection<WindowedEdge> states) {
			state.clear();
			map.clear();
			state.addAll(states);
			for ( WindowedEdge wl : states ){
				map.put(wl.edge(), wl);
			}
		}

		@Override
		public Set<WindowedEdge> states() {
			return state;
		}
	}
	
	public final static class WindowedEdgesMap extends AdjacencyMap<WindowedEdge,WindowedEdge> {
		@Override
		protected WindowedEdge newCouple(Integer id1, Integer id2) {
			throw new UnsupportedOperationException();
		}
	}
	
	public final static class WindowedEdgesSet extends AdjacencySet<WindowedEdge> {
		WindowedEdgesSet(){
			map = new WindowedEdgesMap();
		}
	}
	
	public interface Handler {
		public Listener<WindowedEdge> windowedEdgesListener();
		public Listener<WindowedEdgeEvent> windowedEdgesEventListener();
	}
	

	public WindowedEdgeTrace(Store store, String name, PersistentMap info) throws IOException {
		super(store, name, info, new WindowedEdgeEvent.Factory(), new WindowedEdge.Factory(), 
				new StateUpdaterFactory<WindowedEdgeEvent,WindowedEdge>(){
					@Override
					public StateUpdater<WindowedEdgeEvent, WindowedEdge> getNew() {
						return new WindowedEdgeTrace.Updater();
					}
		});
	}

	public long windowLength(){
		return Long.parseLong(getValue(windowLengthKey));
	}
}
