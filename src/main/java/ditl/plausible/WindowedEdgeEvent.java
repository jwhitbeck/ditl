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

import ditl.ItemFactory;
import ditl.graphs.Edge;

public final class WindowedEdgeEvent {
	
	public final static int 
		UP = 0,
		DOWN = 1,
		PREV_UP = 2,
		PREV_DOWN = 3,
		NEXT_UP = 4,
		NEXT_DOWN = 5;
	
	private final static String 
		_up = "UP",
		_down = "DOWN",
		prev_up = "PREVUP",
		prev_down = "PREVDOWN",
		next_up = "NEXTUP",
		next_down = "NEXTDOWN";
	
	final int _type;
	final Edge _edge;
	final long _value; 
	
	public WindowedEdgeEvent(Edge edge, int type){
		_type = type;
		_edge = edge;
		_value = 0;
	}
	
	public WindowedEdgeEvent(Edge edge, int type, long value){
		_type = type;
		_edge = edge;
		_value = value;
	}
	
	public int type(){
		return _type;
	}
	
	public Edge edge(){
		return _edge;
	}
	
	@Override
	public String toString(){
		switch(_type){
		case UP        : return _edge+" "+_up;
		case DOWN      : return _edge+" "+_down;
		case PREV_UP   : return _edge+" "+prev_up+" "+_value;
		case PREV_DOWN : return _edge+" "+prev_down+" "+_value;
		case NEXT_UP   : return _edge+" "+next_up+" "+_value;
		default        : return _edge+" "+next_down+" "+_value;
		}
	}
	
	public static final class Factory implements ItemFactory<WindowedEdgeEvent> {
		@Override
		public WindowedEdgeEvent fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				Integer id1 = Integer.parseInt(elems[0]);
				Integer id2 = Integer.parseInt(elems[1]);
				Edge e = new Edge(id1,id2);
				WindowedEdgeEvent wle = null;
				String type_str = elems[2];
				if ( type_str.equals(_up) )
					wle = new WindowedEdgeEvent(e, WindowedEdgeEvent.UP);
				else if ( type_str.equals(_down) )
					wle = new WindowedEdgeEvent(e, WindowedEdgeEvent.DOWN);
				else {
					long value = Long.parseLong(elems[3]);
					if ( type_str.equals(prev_up) )
						wle = new WindowedEdgeEvent(e, WindowedEdgeEvent.PREV_UP, value);
					else if ( type_str.equals(prev_down) )
						wle = new WindowedEdgeEvent(e, WindowedEdgeEvent.PREV_DOWN, value);
					else if ( type_str.equals(next_up) )
						wle = new WindowedEdgeEvent(e, WindowedEdgeEvent.NEXT_UP, value);
					else if ( type_str.equals(next_down) )
						wle = new WindowedEdgeEvent(e, WindowedEdgeEvent.NEXT_DOWN, value);
				}
				return wle;
				
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
}
