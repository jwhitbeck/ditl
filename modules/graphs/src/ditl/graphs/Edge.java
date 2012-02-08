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

public final class Edge implements Comparable<Edge> {
	
	final Integer _from;
	final Integer _to;
	
	public Edge(Integer from, Integer to){
		_from = from;
		_to = to;
	}
	
	public Integer from(){
		return _from;
	}
	
	public Integer to(){
		return _to;
	}
	
	public final static class Factory implements ItemFactory<Edge> {
		@Override
		public Edge fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				Integer from = Integer.parseInt(elems[0]);
				Integer to = Integer.parseInt(elems[1]);
				return new Edge(from,to);
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
	
	public Edge reverse(){
		return new Edge(_to,_from);
	}
	
	public Link link(){
		return new Link(_from,_to);
	}
	
	@Override
	public boolean equals(Object o){
		Edge l = (Edge)o;
		return (l._from.equals(_from)) && (l._to.equals(_to));
	}
	
	@Override
	public String toString(){
		return _from+" "+_to;
	}
	
	public static final class InternalGroupMatcher implements Matcher<Edge> {
		private Set<Integer> _group;
		public InternalGroupMatcher(Set<Integer> group){ _group = group;}
		@Override
		public boolean matches(Edge item) {
			return _group.contains(item._from) && _group.contains(item._to);
		}
	}

	@Override
	public int compareTo(Edge oe) {
		int c = _from.compareTo(oe._from);
		if ( c != 0 ) return c;
		return _to.compareTo(oe._to);
	}
	
}
