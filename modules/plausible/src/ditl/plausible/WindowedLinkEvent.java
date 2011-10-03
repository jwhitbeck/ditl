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

import ditl.ItemFactory;
import ditl.graphs.Link;

public final class WindowedLinkEvent {
	
	public final static int UP = 0;
	public final static int DOWN = 1;
	public final static int PREV_UP = 2;
	public final static int PREV_DOWN = 3;
	public final static int NEXT_UP = 4;
	public final static int NEXT_DOWN = 5;
	
	private final static String _up = "UP";
	private final static String _down = "DOWN";
	private final static String prev_up = "PREVUP";
	private final static String prev_down = "PREVDOWN";
	private final static String next_up = "NEXTUP";
	private final static String next_down = "NEXTDOWN";
	
	final int _type;
	final Link _link;
	final long _value; 
	
	public WindowedLinkEvent(Link link, int type){
		_type = type;
		_link = link;
		_value = 0;
	}
	
	public WindowedLinkEvent(Link link, int type, long value){
		_type = type;
		_link = link;
		_value = value;
	}
	
	public int type(){
		return _type;
	}
	
	public Link link(){
		return _link;
	}
	
	@Override
	public String toString(){
		switch(_type){
		case UP        : return _link+" "+_up;
		case DOWN      : return _link+" "+_down;
		case PREV_UP   : return _link+" "+prev_up+" "+_value;
		case PREV_DOWN : return _link+" "+prev_down+" "+_value;
		case NEXT_UP   : return _link+" "+next_up+" "+_value;
		default        : return _link+" "+next_down+" "+_value;
		}
	}
	
	public static final class Factory implements ItemFactory<WindowedLinkEvent> {
		@Override
		public WindowedLinkEvent fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				Integer id1 = Integer.parseInt(elems[0]);
				Integer id2 = Integer.parseInt(elems[1]);
				Link l = new Link(id1,id2);
				WindowedLinkEvent wle = null;
				String type_str = elems[2];
				if ( type_str.equals(_up) )
					wle = new WindowedLinkEvent(l, WindowedLinkEvent.UP);
				else if ( type_str.equals(_down) )
					wle = new WindowedLinkEvent(l, WindowedLinkEvent.DOWN);
				else {
					long value = Long.parseLong(elems[3]);
					if ( type_str.equals(prev_up) )
						wle = new WindowedLinkEvent(l, WindowedLinkEvent.PREV_UP, value);
					else if ( type_str.equals(prev_down) )
						wle = new WindowedLinkEvent(l, WindowedLinkEvent.PREV_DOWN, value);
					else if ( type_str.equals(next_up) )
						wle = new WindowedLinkEvent(l, WindowedLinkEvent.NEXT_UP, value);
					else if ( type_str.equals(next_down) )
						wle = new WindowedLinkEvent(l, WindowedLinkEvent.NEXT_DOWN, value);
				}
				return wle;
				
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
}
