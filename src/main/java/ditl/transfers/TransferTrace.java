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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ditl.Listener;
import ditl.PersistentMap;
import ditl.StateUpdater;
import ditl.StateUpdaterFactory;
import ditl.StatefulTrace;
import ditl.Store;
import ditl.Trace;
import ditl.graphs.AdjacencyMap;
import ditl.graphs.Arc;

@Trace.Type("transfers")
public class TransferTrace extends StatefulTrace<TransferEvent, Transfer> {

    public final static class Updater implements StateUpdater<TransferEvent, Transfer> {
        private final Map<Arc, Transfer> transfer_map = new AdjacencyMap.Arcs<Transfer>();
        private final Set<Transfer> transfers = new HashSet<Transfer>();

        @Override
        public void handleEvent(long time, TransferEvent event) {
            Transfer transfer;
            switch (event.type()) {
                case START:
                    transfer = new Transfer(event);
                    transfer_map.put(event.arc(), transfer);
                    transfers.add(transfer);
                    break;
                default:
                    final Arc a = event.arc();
                    transfer = transfer_map.get(a);
                    transfers.remove(transfer);
                    transfer_map.remove(a);
            }
        }

        @Override
        public void setState(Collection<Transfer> state) {
            transfer_map.clear();
            transfers.clear();
            for (final Transfer transfer : state) {
                transfer_map.put(transfer.arc(), transfer);
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
                new StateUpdaterFactory<TransferEvent, Transfer>() {
                    @Override
                    public StateUpdater<TransferEvent, Transfer> getNew() {
                        return new TransferTrace.Updater();
                    }
                });
    }

}
