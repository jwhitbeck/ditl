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

import java.io.*;
import java.util.*;

import ditl.*;

public class MessageTransferReport extends Report 
	implements TransferTrace.Handler {

	private Map<Integer,Long> bytes_completed = new LinkedHashMap<Integer,Long>();
	private Map<Integer,Integer> n_completed = new LinkedHashMap<Integer,Integer>();
	private Map<Integer,Long> bytes_aborted = new LinkedHashMap<Integer,Long>();
	private Map<Integer,Integer> n_aborted = new LinkedHashMap<Integer,Integer>();
	
	public MessageTransferReport(OutputStream out) throws IOException {
		super(out);
		appendComment("msgId | N Completed | Bytes completed | N Aborted | Bytes aborted");
	}

	public static final class Factory implements ReportFactory<MessageTransferReport> {
		@Override
		public MessageTransferReport getNew(OutputStream out)
				throws IOException {
			return new MessageTransferReport(out);
		}
	}
	
	@Override
	public void finish() throws IOException {
		StringBuffer buffer;
		for ( Integer msgId : n_completed.keySet() ){
			buffer = new StringBuffer();
			buffer.append(msgId+" ");
			buffer.append(n_completed.get(msgId)+" ");
			buffer.append(bytes_completed.get(msgId)+" ");
			buffer.append(n_aborted.get(msgId)+" ");
			buffer.append(bytes_aborted.get(msgId)+" ");
			append(buffer.toString());
		}
		super.finish();
	}
	
	@Override
	public Listener<TransferEvent> transferEventListener() {
		return new Listener<TransferEvent>(){
			@Override
			public void handle(long time, Collection<TransferEvent> events){
				for ( TransferEvent event : events ){
					Integer msgId = event.msgId();
					Integer n;
					Long l;
					switch ( event.type() ){
					case TransferEvent.START:
						if ( ! n_completed.containsKey(msgId) ){
							bytes_completed.put(msgId, 0L);
							n_completed.put(msgId, 0);
							bytes_aborted.put(msgId, 0L);
							n_aborted.put(msgId, 0);
						}
						break;
					case TransferEvent.ABORT:
						n = n_aborted.get(msgId);
						if ( n != null ){ // ignore those for which we haven't registered a start event
							n_aborted.put(msgId, n+1);
							l = bytes_aborted.get(msgId);
							bytes_aborted.put(msgId, l+event.bytesTransferred());
						}
						break;
					case TransferEvent.COMPLETE:
						n = n_completed.get(msgId);
						if ( n != null ){ // ignore those for which we haven't registered a start event
							n_completed.put(msgId, n+1);
							l = bytes_completed.get(msgId);
							bytes_completed.put(msgId, l+event.bytesTransferred());
						}
						break;
					}
				}
			}
		};
	}

	@Override
	public Listener<Transfer> transferListener() {
		return null;
	}
}
