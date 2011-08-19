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
package ditl.transfers;

import java.util.*;

import ditl.ItemFactory;

public class Buffer implements Iterable<Integer> {
	
	public static String delim = ":";
	
	Integer _id;
	Set<Integer> msg_ids = new HashSet<Integer>();
	
	public Buffer(Integer id){
		_id = id;
	}
	
	public Integer id(){
		return _id;
	}
	
	@Override
	public int hashCode(){
		return _id;
	}
	
	@Override 
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(_id+" ");
		if ( ! msg_ids.isEmpty() )
			for ( Iterator<Integer> i = msg_ids.iterator(); i.hasNext(); ){
				Integer msgId = i.next();
				buffer.append(msgId);
				if ( i.hasNext() )
					buffer.append(delim);
			}
		else buffer.append("-");
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object o){
		Buffer b = (Buffer)o;
		return _id.equals(b._id);
	}
	
	public static ItemFactory<Buffer> factory(){
		return new ItemFactory<Buffer>(){
			@Override
			public Buffer fromString(String s) {
				String[] elems = s.trim().split(" ");
				try {
					Integer id = Integer.parseInt(elems[0]);
					Buffer b = new Buffer(id);
					if ( ! elems[1].equals("-") )
						for ( String m : elems[1].split(delim))
							b.msg_ids.add(Integer.parseInt(m));
					return b;					
				} catch ( Exception e ){
					System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
					return null;
				}
			}
		};
	}

	@Override
	public Iterator<Integer> iterator() {
		return msg_ids.iterator();
	}
}
