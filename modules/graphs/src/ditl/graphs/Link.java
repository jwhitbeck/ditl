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

public final class Link {
	
	final Integer id1;
	final Integer id2;
	
	public Link(Integer i1, Integer i2){
		if ( i1 < i2 ){
			id1 = i1;
			id2 = i2;
		} else {
			id1 = i2;
			id2 = i1;
		}
	}
	
	public Integer id1(){
		return id1;
	}
	
	public Integer id2(){
		return id2;
	}
	
	public static ItemFactory<Link> factory () {
		return new ItemFactory<Link>() {

			@Override
			public Link fromString(String s) {
				String[] elems = s.trim().split(" ");
				try {
					Integer id1 = Integer.parseInt(elems[0]);
					Integer id2 = Integer.parseInt(elems[1]);
					return new Link(id1,id2);
					
				} catch ( Exception e ){
					System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
					return null;
				}
			}
		};
	}
	
	public boolean hasVertex(Integer id){
		return ( id.equals(id1) || id.equals(id2) );
	}
	
	@Override
	public boolean equals(Object o){
		Link ct = (Link)o;
		return (ct.id1.equals(id1)) && (ct.id2.equals(id2));
	}
	
	@Override
	public int hashCode(){
		return GraphStore.maxNumNodes*id1+id2;
	}
	
	@Override
	public String toString(){
		return id1+" "+id2;
	}
	
	public static Matcher<Link> internalLinkMatcher(final Set<Integer> group){
		return new Matcher<Link>(){
			@Override
			public boolean matches(Link item) {
				return group.contains(item.id1) && group.contains(item.id2);
			}
		};
	}

}
