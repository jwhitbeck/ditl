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

public class GroupEvent {

	public final static int NEW = 0;
	public final static int JOIN = 1;
	public final static int LEAVE = 2;
	public final static int DELETE = 3;
	
	private final static String NEW_LABEL = "NEW";
	private final static String JOIN_LABEL = "JOIN";
	private final static String LEAVE_LABEL = "LEAVE";
	private final static String DELETE_LABEL = "DEL";
	
	int _type;
	Integer _gid;
	Set<Integer> _members;
	
	public GroupEvent(Integer gid, int type){ // NEW and DELETE events
		_gid = gid;
		_type = type;
	}
	
	public GroupEvent(Integer gid, int type, Integer[] members){
		_gid = gid;
		_type = type;
		_members = new HashSet<Integer>();
		for ( Integer i : members )
			_members.add(i);
	}
	
	public GroupEvent(Integer gid, int type, Set<Integer> members){
		_gid = gid;
		_type = type;
		_members = members;
	}
	
	public int type(){
		return _type;
	}
	
	public Integer gid(){
		return _gid;
	}
	
	public Set<Integer> members(){
		return Collections.unmodifiableSet(_members);
	}
	
	public final static class Factory implements ItemFactory<GroupEvent> {
		@Override
		public GroupEvent fromString(String s) {
			String[] elems = s.trim().split(" ",3);
			try {
				String sType = elems[0];
				Integer gid = Integer.parseInt(elems[1]);
				if ( sType.equals(NEW_LABEL) ){
					return new GroupEvent(gid, NEW);
				} else if ( sType.equals(DELETE_LABEL) ){
					return new GroupEvent(gid, DELETE);
				} else {
					Set<Integer> members = GroupSpecification.parse(elems[2]);
					if ( sType.equals(JOIN_LABEL) ){
						return new GroupEvent(gid, JOIN, members);
					} else {
						return new GroupEvent(gid, LEAVE, members);
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
		switch ( _type ){
		case NEW: return NEW_LABEL+" "+_gid;
		case JOIN: return JOIN_LABEL+" "+_gid+" "+GroupSpecification.toString(_members);
		case LEAVE: return LEAVE_LABEL+" "+_gid+" "+GroupSpecification.toString(_members);
		default: return DELETE_LABEL+" "+_gid;
		}
	}
	
	public final static class GroupFilter implements Filter<GroupEvent> {
		private Set<Integer> _group;
		public GroupFilter(Set<Integer> group){ _group = group; }
		@Override
		public GroupEvent filter(GroupEvent item) {
			Set<Integer> f_members = new HashSet<Integer>();
			if ( item._type == JOIN || item._type == LEAVE ){
				for ( Integer i : item._members ){
					if ( _group.contains(i) )
						f_members.add(i);
				}
				if ( f_members.isEmpty() )
					return null;
				return new GroupEvent( item._gid, item._type, f_members);
			} 
			return item;
		}
	}
}
