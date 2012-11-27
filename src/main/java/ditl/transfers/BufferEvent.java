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
package ditl.transfers;

import ditl.ItemFactory;

public class BufferEvent {

	public final static int 
		IN = 0,
		ADD = 1,
		REMOVE = 2,
		OUT = 3;
	
	Integer _id;
	Integer msg_id;
	int _type;
	
	public BufferEvent(Integer id, int type){
		_id = id;
		_type = type;
	}
	
	public BufferEvent(Integer id, Integer msgId, int type){
		_id = id;
		msg_id = msgId;
		_type = type;
	}
	
	public int type(){
		return _type;
	}
	
	public Integer id(){
		return _id;
	}
	
	public Integer msgId(){
		return msg_id;
	}
	
	public static final class Factory implements ItemFactory<BufferEvent> {
		@Override
		public BufferEvent fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				Integer id = Integer.parseInt(elems[0]);
				int type;
				if ( elems[1].equals("IN") )
					type = IN;
				else if ( elems[1].equals("ADD") )
					type = ADD;
				else if ( elems[1].equals("REMOVE") )
					type = REMOVE;
				else
					type = OUT;
				
				if ( type == ADD || type == REMOVE ){
					Integer msgId = Integer.parseInt(elems[2]); 
					return new BufferEvent(id, msgId, type);
				} else {
					return new BufferEvent(id,type);
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
		case IN: return _id+" IN";
		case ADD: return _id+" ADD "+msg_id;
		case REMOVE: return _id+" REMOVE "+msg_id;
		default: return _id+" OUT";
		}
	}
}
