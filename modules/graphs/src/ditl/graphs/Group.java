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

import java.util.*;

import ditl.ItemFactory;

public class Group implements Cloneable {
	
	final static String delim = ":";
	
	Integer _gid;
	Set<Integer> members = new HashSet<Integer>();
	
	public Group(Integer gid){
		_gid = gid;
	}
	
	public Integer gid(){
		return _gid;
	}
	
	public int size(){
		return members.size();
	}
	
	public final static class Factory implements ItemFactory<Group> {
		@Override
		public Group fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				Integer id = Integer.parseInt(elems[0]);
				Group g = new Group(id);
				for ( String m : elems[1].split(delim) ){
					g.members.add(Integer.parseInt(m));
				}
				return g;
				
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
	
	public void handleEvent(GroupEvent event){
		switch ( event._type ){
		case GroupEvent.JOIN:  
			for ( Integer m : event._members )
				members.add(m); 
			break;
		case GroupEvent.LEAVE: 
			for ( Integer m : event._members )
				members.remove(m);
			break;
		}
	}
	
	public static String groupToString(Object[] group){
		StringBuffer buffer = new StringBuffer();
		for ( int i = 0; i<group.length; ++i){
			buffer.append(group[i]);
			if ( i < group.length-1 )
				buffer.append(delim);
		}
		return buffer.toString();
	}
	
	@Override
	public String toString(){
		return _gid+" "+groupToString(members.toArray());
	}
	
	@Override
	public int hashCode(){
		return _gid;
	}
	
	@Override
	public Group clone(){
		Group g = new Group(_gid);
		g.members = new HashSet<Integer>(members);
		return g;
	}
	
	public Set<Integer> members(){
		return Collections.unmodifiableSet(members);
	}
}
