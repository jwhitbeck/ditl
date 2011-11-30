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
package ditl;

import java.util.*;

public class GroupSpecification {
	
	private final static String range_sep = ":";
	private final static String gap_sep = ",";
	private final static String empty = "E";

	public static Set<Integer> parse(String s){
		Set<Integer> group = new HashSet<Integer>();
		if ( ! s.equals(empty) ){
			String[] ranges = s.split(gap_sep);
			for ( String range : ranges ){
				String[] bounds = range.split(range_sep);
				if ( bounds.length == 1 )
					group.add( Integer.parseInt(bounds[0]) );
				else {
					for ( Integer i = Integer.parseInt(bounds[0]); i<=Integer.parseInt(bounds[1]); ++i){
						group.add(i);
					}
				}
			}
		}
		return group;
	}
	
	public static String toString(Set<Integer> group){
		if ( group.isEmpty() )
			return empty;
		List<Integer> list = new LinkedList<Integer>(group);
		Collections.sort(list);
		StringBuffer s = new StringBuffer();
		Iterator<Integer> i = list.iterator();
		Integer prev = null;
		Integer b = null;
		while ( i.hasNext() ){
			Integer n = i.next();
			if ( b == null )
				b = n;
			if ( prev != null ){
				if ( n - prev > 1 ){ // left range
					if ( prev.equals(b) ){ // singleton range 
						s.append(prev);
					} else {
						s.append(b+range_sep+prev);
					}
					b = n;
					s.append(gap_sep);
				}
			}
			if ( ! i.hasNext() ){
				if ( n.equals(b) ){ // singleton range 
					s.append(n);
				} else {
					s.append(b+range_sep+n);
				}
			}
			prev = n;
		}
		return s.toString();
	}
}
