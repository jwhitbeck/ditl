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
	Integer[] _members;
	
	public GroupEvent(Integer gid, int type){ // NEW and DELETE events
		_gid = gid;
		_type = type;
	}
	
	public GroupEvent(Integer gid, int type, Integer[] members){
		_gid = gid;
		_type = type;
		_members = members;
	}
	
	public GroupEvent(Integer gid, int type, Collection<Integer> members){
		_gid = gid;
		_type = type;
		_members = members.toArray(new Integer[]{});
	}
	
	public int type(){
		return _type;
	}
	
	public Integer gid(){
		return _gid;
	}
	
	public Integer[] members(){
		return _members;
	}
	
	public static ItemFactory<GroupEvent> factory(){
		return new ItemFactory<GroupEvent>(){
			@Override
			public GroupEvent fromString(String s) {
				String[] elems = s.trim().split(" ");
				try {
					String sType = elems[0];
					Integer gid = Integer.parseInt(elems[1]);
					if ( sType.equals(NEW_LABEL) ){
						return new GroupEvent(gid, NEW);
					} else if ( sType.equals(DELETE_LABEL) ){
						return new GroupEvent(gid, DELETE);
					} else {
						Set<Integer> members = new HashSet<Integer>();
						for ( String m : elems[2].split(Group.delim) ){
							members.add(Integer.parseInt(m));
						}
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
		};
	}
	
	@Override
	public String toString(){
		switch ( _type ){
		case NEW: return NEW_LABEL+" "+_gid;
		case JOIN: return JOIN_LABEL+" "+_gid+" "+Group.groupToString(_members);
		case LEAVE: return LEAVE_LABEL+" "+_gid+" "+Group.groupToString(_members);
		default: return DELETE_LABEL+" "+_gid;
		}
	}
}
