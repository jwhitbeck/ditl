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
package ditl.graphs;

import java.util.Set;

import ditl.*;

public final class EdgeEvent {
	
	public final static boolean UP = true;
	public final static boolean DOWN = false;
	
	final Integer _to;
	final Integer _from;
	final boolean _up;
	
	public EdgeEvent(Integer from, Integer to, boolean up){
		_from = from;
		_to = to;
		_up = up;
	}
	
	public EdgeEvent(Edge e, boolean up){
		_from = e._from;
		_to = e._to;
		_up = up;
	}
	
	public Integer from(){
		return _from;
	}
	
	public Integer to(){
		return _to;
	}
	
	public boolean isUp(){
		return _up;
	}
	
	public Edge edge(){
		return new Edge(_from,_to);
	}
	
	public static final class Factory implements ItemFactory<EdgeEvent> {
		@Override
		public EdgeEvent fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				Integer from = Integer.parseInt(elems[0]);
				Integer to = Integer.parseInt(elems[1]);
				boolean up = elems[2].equals("UP");
				return new EdgeEvent(from,to,up);
				
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
	
	@Override
	public String toString(){
		return _from+" "+_to+" "+(_up? "UP" : "DOWN");
	}
	
	public static final class InternalGroupFilter implements Filter<EdgeEvent> {
		private Set<Integer> _group;
		public InternalGroupFilter(Set<Integer> group){ _group = group;}
		@Override
		public EdgeEvent filter(EdgeEvent item) {
			if ( _group.contains(item._from) && _group.contains(item._to) )
				return item;
			return null;
		}
	}

}
