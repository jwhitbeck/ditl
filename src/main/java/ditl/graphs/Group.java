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

import java.util.*;

import ditl.*;

public class Group {
	
	Integer _gid;
	Set<Integer> _members;
	
	public Group(Integer gid){
		_gid = gid;
		_members = new HashSet<Integer>();
	}
	
	public Group(Integer gid, Set<Integer> members){
		_gid = gid;
		_members = members;
	}
	
	public Integer gid(){
		return _gid;
	}
	
	public int size(){
		return _members.size();
	}
	
	public final static class Factory implements ItemFactory<Group> {
		@Override
		public Group fromString(String s) {
			String[] elems = s.trim().split(" ", 2);
			try {
				Integer gid = Integer.parseInt(elems[0]);
				Set<Integer> members = GroupSpecification.parse(elems[1]);
				return new Group(gid, members);
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
				_members.add(m); 
			break;
		case GroupEvent.LEAVE: 
			for ( Integer m : event._members )
				_members.remove(m);
			break;
		}
	}
	
	@Override
	public String toString(){
		return _gid+" "+GroupSpecification.toString(_members);
	}
	
	@Override
	public int hashCode(){
		return _gid;
	}
	
	public Set<Integer> members(){
		return Collections.unmodifiableSet(_members);
	}
	
	public final static class GroupFilter implements Filter<Group> {
		private Set<Integer> _group;
		public GroupFilter(Set<Integer> group){ _group = group; }
		@Override
		public Group filter(Group item) {
			Group f_group = new Group(item._gid);
			for ( Integer i : item._members ){
				if ( _group.contains(i) )
					f_group._members.add(i);
			}
			if ( f_group._members.isEmpty() )
				return null;
			return f_group;
		}
		
	}
}
