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

public final class LinkEvent {
	
	public final static boolean UP = true;
	public final static boolean DOWN = false;
	
	final Integer id1;
	final Integer id2;
	final boolean _up;
	
	public LinkEvent(Integer i1, Integer i2, boolean up){
		if ( i1 < i2 ){
			id1 = i1;
			id2 = i2;
		} else {
			id1 = i2;
			id2 = i1;
		}
		_up = up;
	}
	
	public LinkEvent(Link link, boolean up){
		id1 = link.id1;
		id2 = link.id2;
		_up = up;
	}
	
	public Integer id1(){
		return id1;
	}
	
	public Integer id2(){
		return id2;
	}
	
	public boolean isUp(){
		return _up;
	}
	
	public Link link(){
		return new Link(id1,id2);
	}
	
	public static ItemFactory<LinkEvent> factory () {
		return new ItemFactory<LinkEvent>() {

			@Override
			public LinkEvent fromString(String s) {
				String[] elems = s.trim().split(" ");
				try {
					Integer id1 = Integer.parseInt(elems[0]);
					Integer id2 = Integer.parseInt(elems[1]);
					boolean up = elems[2].equals("UP");
					return new LinkEvent(id1,id2,up);
					
				} catch ( Exception e ){
					System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
					return null;
				}
			}
			
		};
	}
	
	@Override
	public String toString(){
		return id1+" "+id2+" "+(_up? "UP" : "DOWN");
	}
	
	public static Matcher<LinkEvent> internalLinkEventMatcher(final Set<Integer> group){
		return new Matcher<LinkEvent>(){
			@Override
			public boolean matches(LinkEvent item) {
				return group.contains(item.id1) && group.contains(item.id2);
			}
		};
	}

}
