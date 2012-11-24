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

import ditl.*;
import ditl.graphs.*;

public final class WindowedLink implements Couple {
	
	long prev_up = -Trace.INFINITY;
	long prev_down = -Trace.INFINITY;
	long next_up = Trace.INFINITY;
	long next_down = Trace.INFINITY;
	
	final Link _link;
	
	public WindowedLink(Link link){
		_link = link;
	}
	
	public Link link(){
		return _link;
	}
	
	public Integer id1(){ 
		return _link.id1();
	}
	
	public Integer id2(){ 
		return _link.id2();
	}
	
	public long minUpTime(long t){
		return Math.min(next_down-t, t-prev_up);
	}
	
	public long minDownTime(long t){
		return Math.min(next_up-t, t-prev_down);
	}
	
	@Override
	public boolean equals(Object o){
		WindowedLink wl = (WindowedLink)o;
		return wl._link.equals(_link);
	}
	
	@Override
	public String toString(){
		return _link+" "+prev_up+" "+prev_down+" "+next_up+" "+next_down;
	}
	
	public static final class Factory implements ItemFactory<WindowedLink> {
		@Override
		public WindowedLink fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				Integer id1 = Integer.parseInt(elems[0]);
				Integer id2 = Integer.parseInt(elems[1]);
				WindowedLink wl = new WindowedLink(new Link(id1,id2));
				wl.prev_up = Long.parseLong(elems[2]);
				wl.prev_down = Long.parseLong(elems[3]);
				wl.next_up = Long.parseLong(elems[4]);
				wl.next_down = Long.parseLong(elems[5]);
				return wl;
				
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
	
	public void handleEvent(WindowedLinkEvent wle){
		switch( wle._type ){
		case WindowedLinkEvent.PREV_UP: prev_up = wle._value; break;
		case WindowedLinkEvent.PREV_DOWN: prev_down = wle._value; break;
		case WindowedLinkEvent.NEXT_UP: next_up = wle._value; break;
		case WindowedLinkEvent.NEXT_DOWN: next_down = wle._value; break;
		}
	}
}
