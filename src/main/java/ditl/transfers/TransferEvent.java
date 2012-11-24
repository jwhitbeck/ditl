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
import ditl.graphs.Edge;

public class TransferEvent {
	
	public final static int START = 0;
	public final static int COMPLETE = 1;
	public final static int ABORT = 2;
	
	Integer msg_id;
	int _type;
	long bytes_transferred;
	Integer _from;
	Integer _to;

	public TransferEvent(Integer msgId, Integer from, Integer to, int type){
		msg_id = msgId;
		_type = type;
		_from = from;
		_to = to;
	}
	
	public TransferEvent(Integer msgId, Integer from, Integer to, int type, long bytesTransferred){
		msg_id = msgId;
		_type = type;
		_from = from;
		_to = to;
		bytes_transferred = bytesTransferred;
	}
	
	public long bytesTransferred(){
		return bytes_transferred;
	}
	
	public int type(){
		return _type;
	}
	
	public Integer msgId(){
		return msg_id;
	}
	
	public Edge edge(){
		return new Edge(_from,_to);
	}
	
	@Override
	public String toString(){
		switch ( _type ){
		case START: return "START "+msg_id+" "+_from+" "+_to;
		case COMPLETE: return "COMPLETE "+msg_id+" "+_from+" "+_to+" "+bytes_transferred;
		default: return  "ABORT "+msg_id+" "+_from+" "+_to+" "+bytes_transferred;
		}
	}
	
	public static final class Factory implements ItemFactory<TransferEvent> {
		@Override
		public TransferEvent fromString(String s) {
			String[] elems = s.trim().split(" ");
			try {
				String typeString = elems[0];
				int type;
				if ( typeString.equals("START") ){
					type = START;
				} else if ( typeString.endsWith("COMPLETE") ){
					type = COMPLETE;
				} else {
					type = ABORT;
				}
				Integer msgId = Integer.parseInt(elems[1]);
				Integer from = Integer.parseInt(elems[2]);
				Integer to = Integer.parseInt(elems[3]);
				if ( type == START ){
					return new TransferEvent(msgId, from, to, type);
				} else {
					long bytesTransferred = Long.parseLong(elems[4]);
					return new TransferEvent(msgId, from, to, type, bytesTransferred);
				}
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
}
