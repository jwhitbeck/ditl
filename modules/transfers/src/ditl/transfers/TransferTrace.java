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
import java.util.*;

import ditl.*;
import ditl.graphs.Edge;

public class TransferTrace extends StatefulTrace<TransferEvent, Transfer> {
	
	public final static String type = "transfers";
	public final static String defaultName = "transfers";
	
	public final static class Updater implements StateUpdater<TransferEvent,Transfer>{
		private Map<Edge,Transfer> transfer_map = new TreeMap<Edge,Transfer>();
		private Set<Transfer> transfers = new HashSet<Transfer>();
		
		@Override
		public void handleEvent(long time, TransferEvent event) {
			Transfer transfer;
			switch ( event.type() ){
			case TransferEvent.START: 
				transfer = new Transfer(event);
				transfer_map.put(event.edge(), transfer);
				transfers.add(transfer);
				break;
			default:
				Edge e = event.edge();
				transfer = transfer_map.get(e);
				transfers.remove(transfer);
				transfer_map.remove(e);
			}
		}

		@Override
		public void setState(Collection<Transfer> state) {
			transfer_map.clear();
			transfers.clear();
			for ( Transfer transfer : state ){
				transfer_map.put(transfer.edge(), transfer);
				transfers.add(transfer);
			}
		}

		@Override
		public Set<Transfer> states() {
			return transfers;
		}
	}
	
	public interface Handler {
		Listener<TransferEvent> transferEventListener();
		Listener<Transfer> transferListener();
	}
	
	public TransferTrace(Store store, String name, PersistentMap info) throws IOException {
		super(store, name, info, new TransferEvent.Factory(), new Transfer.Factory(), 
				new StateUpdaterFactory<TransferEvent,Transfer>(){
					@Override
					public StateUpdater<TransferEvent, Transfer> getNew() {
						return new TransferTrace.Updater();
					}
		});
	}

}
