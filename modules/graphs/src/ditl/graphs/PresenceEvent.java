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

import java.util.Set;

import ditl.*;

public final class PresenceEvent {

	public final static boolean IN = true;
	public final static boolean OUT = false;
	
	private final Integer id;
	private final boolean in;
	
	public PresenceEvent(Integer i,  boolean isIn){
		id = i;
		in = isIn;
	}
	
	public Integer id(){
		return id;
	}
	
	public boolean isIn(){
		return in;
	}
	
	public Presence presence(){
		return new Presence(id);
	}
	
	public static ItemFactory<PresenceEvent> factory () {
		return new ItemFactory<PresenceEvent>() {

			@Override
			public PresenceEvent fromString(String s) {
				String[] elems = s.trim().split(" ");
				try {
					int id = Integer.parseInt(elems[0]);
					boolean in = elems[1].equals("IN");
					return new PresenceEvent(id,in);
				} catch ( Exception e ){
					System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
					return null;
				}
			}
			
		};
	}
	
	@Override
	public String toString(){
		return id+" "+(in? "IN" : "OUT");
	}
	
	public static Matcher<PresenceEvent> groupMatcher(final Set<Integer> group){
		return new Matcher<PresenceEvent>(){
			@Override
			public boolean matches(PresenceEvent item) {
				return group.contains(item.id);
			}
		};
	}

}
