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

public final class MovementEvent {
	
	public enum Type { IN, OUT, NEW_DEST }
	
	Integer id;
	Point dest;
	double speed;
	Type type;
	
	public MovementEvent(Integer i){
		id = i;
		type = Type.OUT;
	}
	
	public MovementEvent(Integer i, Point d){
		id = i;
		type = Type.IN;
		dest = d;
	}
	
	public MovementEvent(Integer i, double sp, Point d) {
		id = i;
		dest = d;
		speed = sp;
		type = Type.NEW_DEST;
	}
	
	public Integer id(){
		return id;
	}
	
	public Type type(){
		return type;
	}
	
	public Point dest(){
		return dest;
	}
	
	public Movement origMovement(){
		return new Movement(id, dest);
	}
	
	public static final class Factory implements ItemFactory<MovementEvent> {
		@Override
		public MovementEvent fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				Integer id = Integer.parseInt(elems[0]);
				if ( elems[1].equals(Type.OUT.toString()) ){
					return new MovementEvent(id);
				} else {
					double x = Double.parseDouble(elems[2]);
					double y = Double.parseDouble(elems[3]);
					if ( elems[1].equals(Type.IN.toString()) ){
						return new MovementEvent(id,new Point(x,y));
					} else {
						double sp = Double.parseDouble(elems[1]);
						return new MovementEvent(id, sp, new Point(x,y));
					}
				}
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
	
	@Override
	public String toString(){
		switch ( type ){
		case IN: return id+" IN "+dest;
		case OUT: return id+" OUT";
		default: return id+" "+speed+" "+dest; 
		}
	}
	
	public String ns2String(double time, double mul){
		return "$ns_ at "+time*mul+ " \"$node_("+id+") setdest "+dest+" "+speed/mul+"\"\n";
	}
	
	public static final class GroupFilter implements Filter<MovementEvent> {
		private Set<Integer> _group;
		public GroupFilter(Set<Integer> group){ _group = group;}
		@Override
		public MovementEvent filter(MovementEvent item) { 
			if ( _group.contains(item.id) )
				return item;
			return null;
		}
	}
}
