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

import java.io.IOException;

import ditl.*;

public class MessageStore {
	
	public final static String defaultMessagesName = "messages";
	public final static String defaultTransfersName = "transfers";
	public final static String defaultBuffersName = "buffers";
	
	public final static String messageType = "messages";
	public final static String transferType = "transfers";
	public final static String bufferType = "buffers";
	
	public final static int defaultMessageReaderPriority = 50;
	
	protected Store _store;
	protected WritableStore writable_store = null;
	
	public MessageStore(Store store){
		_store = store;
		if ( store instanceof WritableStore ){
			writable_store = (WritableStore)store;
		}
	}
	
	public StatefulReader<MessageEvent,Message> getMessageReader(Trace trace) throws IOException {
		return _store.getStatefulReader(trace, MessageEvent.factory(), Message.factory(), new MessageUpdater());
	}
	
	public StatefulReader<BufferEvent,Buffer> getBufferReader(Trace trace) throws IOException {
		return _store.getStatefulReader(trace, BufferEvent.factory(), Buffer.factory(), new BufferUpdater());
	}
	
	public StatefulReader<TransferEvent,Transfer> getTransferReader(Trace trace) throws IOException {
		return _store.getStatefulReader(trace, TransferEvent.factory(), Transfer.factory(), new TransferUpdater());
	}
	
	public StatefulWriter<MessageEvent,Message> getMessageWriter(String name, long snapInterval) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		StatefulWriter<MessageEvent,Message> writer = writable_store.getStatefulWriter(name, new MessageUpdater(), snapInterval);
		writer.setProperty(Trace.typeKey, messageType);
		writer.setProperty(Trace.defaultPriorityKey, defaultMessageReaderPriority);
		return writer;
	}
	
	public StatefulWriter<BufferEvent,Buffer> getBufferWriter(String name, long snapInterval) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		StatefulWriter<BufferEvent,Buffer> writer = writable_store.getStatefulWriter(name, new BufferUpdater(), snapInterval);
		writer.setProperty(Trace.typeKey, bufferType);
		return writer;
	}
	
	public StatefulWriter<TransferEvent,Transfer> getTransferWriter(String name, long snapInterval) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		StatefulWriter<TransferEvent,Transfer> writer = writable_store.getStatefulWriter(name, new TransferUpdater(), snapInterval);
		writer.setProperty(Trace.typeKey, transferType);
		return writer;
	}
}
