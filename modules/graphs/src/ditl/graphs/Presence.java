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

public final class Presence {

	private final Integer id;
	
	public Presence(Integer i){
		id = i;
	}
	
	public Integer id(){
		return id;
	}
	
	public static ItemFactory<Presence> factory () {
		return new ItemFactory<Presence>() {

			@Override
			public Presence fromString(String s) {
				String[] elems = s.trim().split(" ");
				try {
					Integer id = Integer.parseInt(elems[1]);
					return new Presence(id);
					
				} catch ( Exception e ){
					System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
					return null;
				}
			}
			
		};
	}
	
	@Override
	public int hashCode(){
		return id;
	}
	
	@Override
	public boolean equals(Object o){
		Presence p = (Presence)o;
		return p.id.equals(id);
	}
	
	@Override
	public String toString(){
		return "p "+id;
	}
	
	public static Matcher<Presence> groupMatcher(final Set<Integer> group){
		return new Matcher<Presence>(){
			@Override
			public boolean matches(Presence item) {
				return group.contains(item.id);
			}
		};
	}
}
